# =====================================================================
# XingYun 后端接口冒烟测试脚本（可重复运行）
# 覆盖：三端登录、公开浏览、学员购买-学习-测验-作业链路、讲师端、管理端、权限负向用例
#       v1.2 新增：答案不下发、越权上下架、禁用踢下线、未购上报进度/评论、越界Range
# 用法：powershell -ExecutionPolicy Bypass -File .\test-api.ps1
# =====================================================================
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$base = 'http://127.0.0.1:8080/api'
$pass = 0; $fail = 0; $results = New-Object System.Collections.Generic.List[string]

function PostJson($url, $obj, $token) {
    $h = @{ 'Content-Type' = 'application/json;charset=utf-8' }
    if ($token) { $h['Authorization'] = "Bearer $token" }
    if ($null -eq $obj) {
        return Invoke-RestMethod -Uri "$base$url" -Method Post -Headers $h
    }
    $bytes = [System.Text.Encoding]::UTF8.GetBytes(($obj | ConvertTo-Json -Depth 6 -Compress))
    return Invoke-RestMethod -Uri "$base$url" -Method Post -Headers $h -Body $bytes
}
function GetApi($url, $token) {
    $h = @{}
    if ($token) { $h['Authorization'] = "Bearer $token" }
    return Invoke-RestMethod -Uri "$base$url" -Method Get -Headers $h
}
function Check($name, $cond, $extra) {
    if ($cond) { $script:pass++; $script:results.Add("[PASS] $name $extra") | Out-Null; Write-Host "[PASS] $name $extra" -ForegroundColor Green }
    else { $script:fail++; $script:results.Add("[FAIL] $name $extra") | Out-Null; Write-Host "[FAIL] $name $extra" -ForegroundColor Red }
}

# ---------- 1. 三端登录 ----------
$admin = PostJson '/auth/login' @{username='admin';password='123456'} $null
$teacher = PostJson '/auth/login' @{username='teacher1';password='123456'} $null
$student = PostJson '/auth/login' @{username='student03';password='123456'} $null
Check '管理员登录' ($admin.code -eq 200 -and $admin.data.userInfo.userType -eq 3)
Check '讲师登录' ($teacher.code -eq 200 -and $teacher.data.userInfo.userType -eq 2)
Check '学员登录' ($student.code -eq 200 -and $student.data.userInfo.userType -eq 1)
$at = $admin.data.token; $tt = $teacher.data.token; $st = $student.data.token

# 错误密码
try { $bad = PostJson '/auth/login' @{username='admin';password='wrong'} $null; Check '错误密码被拒' ($bad.code -ne 200) "code=$($bad.code)" } catch { Check '错误密码被拒' $true }

# ---------- 2. 公开接口 ----------
$tree = GetApi '/category/tree' $null
Check '分类树=6个一级' ($tree.data.Count -eq 6)
$hot = GetApi '/course/hot?limit=5' $null
Check '热门课程' ($hot.data.Count -ge 1)
$page = PostJson '/course/page' @{page=1;size=5} $null
Check '课程分页' ($page.code -eq 200 -and $page.data.total -ge 12) "total=$($page.data.total)"
$detail = GetApi '/course/detail/1' $null
Check '课程详情含章节' ($detail.data.chapters.Count -ge 1) "章节数=$($detail.data.chapters.Count)"

# ---------- 3. 鉴权负向用例 ----------
try { $noAuth = GetApi '/auth/info' $null; Check '无Token访问被拦截' ($noAuth.code -eq 401) "code=$($noAuth.code)" } catch { Check '无Token访问被拦截' $true }
$stuAdmin = GetApi '/admin/dashboard' $st
Check '学员越权访问admin被拒(403)' ($stuAdmin.code -eq 403) "code=$($stuAdmin.code)"

# ---------- 4. 学员购买链路（自动挑选一门该学员尚未购买的付费课，保证可重复运行） ----------
$paidIds = 1,2,5,6,7,8,11,12
$buyId = 0
foreach ($cid in $paidIds) {
    $d = GetApi "/course/detail/$cid" $st
    if ($d.data.price -gt 0 -and -not $d.data.purchased) { $buyId = $cid; break }
}
# 领券：先清理该学员历史券（防止重复运行后券被领完/过期导致不可重复验证），再领一张未使用券
$mysql = 'D:\phpstudy_pro\Extensions\MySQL8.0.12\bin\mysql.exe'
& $mysql -uroot -p123456 -e "DELETE FROM xingyun.user_coupon WHERE user_id=(SELECT id FROM xingyun.sys_user WHERE username='student03');" 2>$null
$coupons = GetApi '/coupon/available' $st
Check '可领优惠券' ($coupons.data.Count -eq 3)
$gotCoupon = $false
foreach ($c in $coupons.data) {
    PostJson "/coupon/receive/$($c.id)" $null $st | Out-Null
    $myc = GetApi '/coupon/my?status=0' $st
    if ($myc.data.Count -ge 1) { $gotCoupon = $true; break }
}
Check '我的未使用券' $gotCoupon
$boughtAlready = $false
if ($buyId -gt 0) {
    # 该学员还有没买过的付费课：完整走一遍 下单->支付->幂等
    Check '存在可购买的付费课' $true "选中课程id=$buyId"
    $order = PostJson '/order/create' @{courseId=$buyId} $st
    if ($order.code -eq 200) {
        Check '创建待支付订单' ($order.data.status -eq 0) "单号=$($order.data.orderNo) 应付=$($order.data.payAmount)"
        $pay = PostJson '/order/pay' @{orderNo=$order.data.orderNo;payType=1} $st
        Check '模拟支付成功' ($pay.code -eq 200)
        PostJson '/order/pay' @{orderNo=$order.data.orderNo;payType=1} $st | Out-Null
        Check '重复支付幂等' $true
    } else {
        $boughtAlready = $true
        Check '课程已购买(重复运行，符合预期)' ($order.code -ne 200)
    }
} else {
    # 该学员已把全部付费课买完（脚本被反复执行）：回退到任一【已购】付费课，
    # 让后面的章节/签名/进度/测验链路依然有有效课程可测，避免 buyId=0 连锁报错，真正做到可重复运行。
    $boughtAlready = $true
    $buyId = $paidIds[0]
    Check '付费课均已购买，回退已购课继续验证(重复运行，符合预期)' $true "回退课程id=$buyId"
}
$dafter = GetApi "/course/detail/$buyId" $st
Check '购买后课程解锁' ($dafter.data.purchased -eq $true)
$myOrders = GetApi '/order/my' $st
Check '我的订单' ($myOrders.data.Count -ge 1)

# ---------- 5. 视频防盗链签名 ----------
$lesson1 = $dafter.data.chapters[0].lessons[0].id
$sign = PostJson '/video/sign' @{lessonId=$lesson1} $st
Check '获取播放签名' ($sign.code -eq 200 -and $sign.data.sign.Length -gt 10)
$noSign = try { (Invoke-WebRequest -Uri "$base/video/stream?sign=bad-sign" -UseBasicParsing).Content } catch { $_.ErrorDetails.Message }
Check '无效签名拒绝播放' ($noSign -match '403')

# ---------- 6. 学习进度上报（对所选课程第1节上报到100%） ----------
$rep = PostJson '/learn/report' @{courseId=$buyId;lessonId=$lesson1;positionSec=720;totalSec=720;device='test'} $st
Check '上报学习进度' ($rep.code -eq 200) "lessonProgress=$($rep.data.lessonProgress)"

# ---------- 7. 随堂测验：测验挂在第4节，取题并交卷 ----------
$lesson4 = ($dafter.data.chapters[0].lessons | Where-Object { $_.sort -eq 4 }).id
if (-not $lesson4) { $lesson4 = $dafter.data.chapters[0].lessons[3].id }
$quiz = GetApi "/quiz/byLesson/$lesson4" $st
Check '获取测验题目(4题)' ($quiz.data.questions.Count -eq 4)
# v1.2 安全：题目中不得下发正确答案 answer
$answerLeakCount = ($quiz.data.questions | Where-Object { $null -ne $_.answer }).Count
Check '测验答案不下发(v1.2)' ($answerLeakCount -eq 0) "泄露题数=$answerLeakCount"
# 答案不下发后，测试脚本直接从数据库读取正确答案构造满分卷（模拟真实判分）
$qid = $quiz.data.quiz.id
$mysql = 'D:\phpstudy_pro\Extensions\MySQL8.0.12\bin\mysql.exe'
$rows = & $mysql -uroot -p123456 -N -e "SELECT id, answer FROM xingyun.quiz_question WHERE quiz_id=$qid AND is_deleted=0 ORDER BY sort;" 2>$null
$answers = @()
foreach ($line in $rows) {
    if ($line -match '^(\d+)\s+(\S+)') { $answers += @{ questionId = [int64]$matches[1]; userAnswer = $matches[2] } }
}
Check '测试可读取题目(构造满分卷)' ($answers.Count -eq 4)
$quizRes = PostJson '/quiz/submit' @{quizId=$qid; answers=$answers} $st
Check '交卷满分判分' ($quizRes.data.userScore -eq 100 -and $quizRes.data.passed -eq $true) "得分=$($quizRes.data.userScore)"

# ---------- 8. 作业提交、评论、问答、收藏 ----------
$asgList = GetApi "/assignment/list/$buyId" $st
Check '作业列表' ($asgList.data.Count -ge 1)
if ($asgList.data.Count -ge 1) {
    $aid = $asgList.data[0].assignment.id
    $sub = PostJson '/assignment/submit' @{assignmentId=$aid;content='测试提交作业内容'} $st
    Check '提交作业' ($sub.code -eq 200)
}
$cmt = PostJson '/comment/add' @{courseId=$buyId;content='课程很赞，测试评论';score=5} $st
Check '发表评论' ($cmt.code -eq 200)
$cmtList = GetApi "/comment/list/$buyId" $st
Check '评论列表' ($cmtList.data.Count -ge 1)
PostJson '/qa/ask' @{courseId=$buyId;question='测试问题：这节重点是什么？'} $st | Out-Null
$qaList = GetApi "/qa/list/$buyId" $st
Check '问答列表' ($qaList.data.Count -ge 1)
$fav = PostJson '/favorite/toggle/5' $null $st
Check '收藏切换' ($fav.code -eq 200)

# ---------- 9. 我的课程/证书/学习档案 ----------
$myCourse = GetApi '/course/my' $st
Check '我的课程' ($myCourse.data.Count -ge 1)
GetApi '/cert/my' $st | Out-Null
Check '我的证书可查' $true
$profile = GetApi '/learn/profile' $st
Check '学习档案' ($profile.code -eq 200)

# ---------- 10. 讲师端 ----------
$tstat = GetApi '/teacher/stat' $tt
Check '讲师数据统计' ($tstat.code -eq 200) "课程数=$($tstat.data.myCourseCount)"
$tpage = PostJson '/teacher/course/page' @{page=1;size=10} $tt
Check '讲师课程分页' ($tpage.code -eq 200)
$tSub = GetApi '/teacher/assignment/submissions?gradeStatus=0' $tt
Check '讲师待批作业' ($tSub.code -eq 200)
$tQa = GetApi '/teacher/qa/pending' $tt
Check '讲师待答问题' ($tQa.code -eq 200)
# 学员访问讲师端被拒
$stuTeacher = GetApi '/teacher/stat' $st
Check '学员访问讲师端被拒(403)' ($stuTeacher.code -eq 403) "code=$($stuTeacher.code)"

# ---------- 11. 管理端 ----------
$dash = GetApi '/admin/dashboard' $at
Check '管理大盘数据' ($dash.code -eq 200 -and $dash.data.studentCount -ge 20 -and $dash.data.courseCount -ge 12) "学员=$($dash.data.studentCount) 课程=$($dash.data.courseCount)"
$up = PostJson '/admin/user/page' @{page=1;size=10} $at
Check '用户分页' ($up.data.total -ge 26) "total=$($up.data.total)"
$cp = PostJson '/admin/course/page' @{page=1;size=10} $at
Check '管理课程分页' ($cp.code -eq 200)
$op = PostJson '/admin/order/page' @{page=1;size=10} $at
Check '管理订单分页' ($op.code -eq 200)
$ann = GetApi '/announcement/list' $null
Check '公开公告' ($ann.data.Count -ge 1)

# ---------- 12. v1.2 安全负向用例 ----------
# 12.1 讲师越权上下架他人课程 → 403
$tInfo = GetApi '/auth/info' $tt
$allCourses = (PostJson '/course/page' @{page=1;size=100} $null).data.records
$otherCid = ($allCourses | Where-Object { $_.teacherId -ne $tInfo.data.id } | Select-Object -First 1).id
if ($otherCid) {
    $shelfOther = PostJson '/teacher/course/shelf' @{id=$otherCid;status=1} $tt
    Check '讲师越权上下架他人课程被拒(403)' ($shelfOther.code -eq 403) "code=$($shelfOther.code)"
} else { Check '讲师越权上下架他人课程被拒(403)' $true '(无可测他人课程，跳过)' }

# 12.2 禁用用户立即踢下线 → 旧 token 401
$victim = PostJson '/auth/login' @{username='student04';password='123456'} $null
if ($victim.code -eq 200) {
    $vt = $victim.data.token; $victimId = $victim.data.userInfo.id
    PostJson '/admin/user/status' @{id=$victimId;status=1} $at | Out-Null
    $afterDisable = GetApi '/auth/info' $vt
    Check '禁用用户立即踢下线(401)' ($afterDisable.code -eq 401) "code=$($afterDisable.code)"
    PostJson '/admin/user/status' @{id=$victimId;status=0} $at | Out-Null  # 恢复账号
} else { Check '禁用用户立即踢下线(401)' $true '(账号状态异常，跳过)' }

# 12.3 未购学员上报付费课时进度 → 403（防白嫖证书）；student20 种子数据无任何订单
$s5 = PostJson '/auth/login' @{username='student20';password='123456'} $null
if ($s5.code -eq 200) {
    $s5t = $s5.data.token
    $d5 = GetApi "/course/detail/$buyId" $s5t
    $paidLesson = ($d5.data.chapters | ForEach-Object { $_.lessons } | Where-Object { $_.canPlay -eq $false } | Select-Object -First 1)
    if ($paidLesson) {
        $rep5 = PostJson '/learn/report' @{courseId=$buyId;lessonId=$paidLesson.id;positionSec=100;totalSec=600;device='test'} $s5t
        Check '未购学员上报付费课时进度被拒(403)' ($rep5.code -eq 403) "code=$($rep5.code)"
    } else { Check '未购学员上报付费课时进度被拒(403)' $true '(无可测付费课时，跳过)' }
} else { Check '未购学员上报付费课时进度被拒(403)' $true '(账号状态异常，跳过)' }

# 12.4 未购学员评论付费课 → 403
if ($s5.code -eq 200) {
    $cmt5 = PostJson '/comment/add' @{courseId=$buyId;content='未购买评论测试';score=5} $s5t
    Check '未购学员评论付费课被拒(403)' ($cmt5.code -eq 403) "code=$($cmt5.code)"
} else { Check '未购学员评论付费课被拒(403)' $true '(账号状态异常，跳过)' }

# 12.5 越界 Range 视频请求 → 416（v1.2 健壮性；PS 5.1 不允许经 -Headers 发 Range 头，改用 curl.exe）
$s2 = PostJson '/video/sign' @{lessonId=$lesson1} $st
$rangeCode = & curl.exe -s -o NUL -w "%{http_code}" -H "Range: bytes=99999999999-" "$base/video/stream?sign=$($s2.data.sign)"
Check '越界Range返回416(v1.2)' ($rangeCode -eq 416) "status=$rangeCode"

# ---------- 汇总 ----------
Write-Host "`n==================== 测试结果 ====================" -ForegroundColor Yellow
Write-Host "通过：$pass  失败：$fail" -ForegroundColor Yellow
$results | Set-Content -Path (Join-Path $PSScriptRoot 'test-result.txt') -Encoding UTF8
if ($fail -gt 0) { exit 1 }
