# =====================================================================
# 占位图片生成器（内联 C# + System.Drawing，无需第三方软件，不占 C 盘）
# 作用：为示例数据补齐 12 张课程封面(course1..12.jpg) 与 26 张头像
#       (admin/teacher1..5/student01..20 .png)，消除 <img> 的 404。
# 重新生成：powershell -ExecutionPolicy Bypass -File .\gen_placeholders.ps1
# 说明：用 C# 而非纯 PowerShell 调用 GDI+，规避 PS 数组传参/重载歧义。
# =====================================================================
Add-Type -AssemblyName System.Drawing

$code = @"
using System;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Drawing.Imaging;

public static class XyImg
{
    static readonly int[,] Pal = new int[,] {
        {46,111,224, 56,161,219}, {103,194,58, 56,176,120},
        {245,108,108, 230,162,60}, {144,89,222, 90,120,230},
        {24,144,255, 0,188,212}, {250,173,20, 245,124,60},
        {82,196,26, 115,209,161}, {235,47,150, 150,80,220},
        {19,194,194, 60,140,220}, {250,84,84, 200,60,120},
        {70,120,230, 120,90,230}, {255,146,0, 255,206,84}
    };

    public static void Cover(string path, int index)
    {
        int w = 640, h = 360, k = (index - 1) % 12;
        Color c1 = Color.FromArgb(255, Pal[k,0], Pal[k,1], Pal[k,2]);
        Color c2 = Color.FromArgb(255, Pal[k,3], Pal[k,4], Pal[k,5]);
        using (Bitmap bmp = new Bitmap(w, h))
        using (Graphics g = Graphics.FromImage(bmp))
        {
            g.SmoothingMode = SmoothingMode.AntiAlias;
            using (LinearGradientBrush br = new LinearGradientBrush(new Rectangle(0,0,w,h), c1, c2, 45f))
                g.FillRectangle(br, 0, 0, w, h);
            StringFormat sf = new StringFormat();
            sf.Alignment = StringAlignment.Center; sf.LineAlignment = StringAlignment.Center;
            using (Font fBig = new Font("Segoe UI", 58, FontStyle.Bold))
            using (Font fSm  = new Font("Segoe UI", 20, FontStyle.Regular))
            {
                g.DrawString(string.Format("COURSE {0:D2}", index), fBig, Brushes.White, new RectangleF(0,110,w,110), sf);
                g.DrawString("XingYun Online Education", fSm, Brushes.White, new RectangleF(0,215,w,60), sf);
            }
            bmp.Save(path, ImageFormat.Jpeg);
        }
    }

    public static void Avatar(string path, string text, int r, int gg, int b)
    {
        int s = 120;
        using (Bitmap bmp = new Bitmap(s, s))
        using (Graphics g = Graphics.FromImage(bmp))
        {
            g.SmoothingMode = SmoothingMode.AntiAlias;
            using (SolidBrush br = new SolidBrush(Color.FromArgb(255, r, gg, b)))
                g.FillEllipse(br, 0, 0, s, s);
            StringFormat sf = new StringFormat();
            sf.Alignment = StringAlignment.Center; sf.LineAlignment = StringAlignment.Center;
            using (Font f = new Font("Segoe UI", 40, FontStyle.Bold))
                g.DrawString(text, f, Brushes.White, new RectangleF(0,0,s,s), sf);
            bmp.Save(path, ImageFormat.Png);
        }
    }
}
"@
Add-Type -TypeDefinition $code -ReferencedAssemblies System.Drawing

$root = "F:\AI_full-stack_project\XingYun\xingyun-server\upload"
$coverDir = Join-Path $root "cover"
$avatarDir = Join-Path $root "avatar"
New-Item -ItemType Directory -Force -Path $coverDir | Out-Null
New-Item -ItemType Directory -Force -Path $avatarDir | Out-Null

# 1) 课程封面
for ($i = 1; $i -le 12; $i++) {
  [XyImg]::Cover((Join-Path $coverDir ("course{0}.jpg" -f $i)), $i)
}
Write-Host "covers generated: 12"

# 2) 头像：admin 红、teacher 橙、student 蓝（编号做轻微色差）
[XyImg]::Avatar((Join-Path $avatarDir "admin.png"), "A", 230, 70, 70)
for ($i = 1; $i -le 5; $i++) {
  [XyImg]::Avatar((Join-Path $avatarDir ("teacher{0}.png" -f $i)), ("T{0}" -f $i), 240, 150, 40)
}
for ($i = 1; $i -le 20; $i++) {
  $gg = 120 + (($i % 6) * 18)
  [XyImg]::Avatar((Join-Path $avatarDir ("student{0:D2}.png" -f $i)), ("S{0:D2}" -f $i), 64, $gg, 220)
}
Write-Host "avatars generated: 26"
Write-Host "DONE"
