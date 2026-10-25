# 重新打包并重启 XingYun 后端（Windows PowerShell，开发期辅助脚本）
# 用法：在 xingyun-server 目录下执行  powershell -ExecutionPolicy Bypass -File .\restart-server.ps1
$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

Write-Host "[1/3] 停止旧的后端进程..." -ForegroundColor Cyan
Get-CimInstance Win32_Process -Filter "Name='java.exe'" |
  Where-Object { $_.CommandLine -match 'xingyun-server.jar' } |
  ForEach-Object { Stop-Process -Id $_.ProcessId -Force; Write-Host "  已停止 PID=$($_.ProcessId)" }
Start-Sleep -Seconds 2

Write-Host "[2/3] 打包..." -ForegroundColor Cyan
mvn -q package '-Dmaven.test.skip=true'

Write-Host "[3/3] 启动..." -ForegroundColor Cyan
Remove-Item run.out.log,run.err.log -ErrorAction SilentlyContinue
Start-Process -FilePath "java" -ArgumentList "-jar","target\xingyun-server.jar" `
  -RedirectStandardOutput "run.out.log" -RedirectStandardError "run.err.log" -WindowStyle Hidden

# 等待端口就绪
$ok = $false
for ($i = 0; $i -lt 40; $i++) {
  try {
    $c = New-Object System.Net.Sockets.TcpClient
    $c.Connect('127.0.0.1', 8080)
    if ($c.Connected) { $c.Close(); $ok = $true; break }
  } catch { Start-Sleep -Milliseconds 1000 }
}
if ($ok) { Write-Host "后端已启动：http://localhost:8080/api" -ForegroundColor Green }
else { Write-Host "启动失败，请查看 run.out.log" -ForegroundColor Red }
