# =====================================================================
# XingYun 初始测试数据生成器（确定性、可重复执行）
# 作用：扫描本脚本即可看清全部初始数据的生成规则；运行后产出 02_xingyun_data.sql
# 数据规模：3角色 / 1超管 / 5讲师 / 20学员 / 6一级+18二级分类 / 12课程 /
#           96课时 / 96视频 / 12课件 / 12测验48题 / 12作业 / 进度订单证书等
# 所有账号初始密码统一为：123456（下方为其 BCrypt 密文）
# =====================================================================
$ErrorActionPreference = "Stop"
$outFile = Join-Path $PSScriptRoot "02_xingyun_data.sql"
$sb = New-Object System.Collections.Generic.List[string]
function Add-Line([string]$s){ $sb.Add($s) | Out-Null }

$hash = '$2a$10$/6PxQH7.p1l2hJ.XHZwSruZ6utXGNQfqeEaaBRoh5BvvHipANuoaG'  # BCrypt(123456)

Add-Line "-- ====================================================================="
Add-Line "-- XingYun 初始测试数据脚本（由 gen_data.ps1 生成，可直接重复执行）"
Add-Line "-- ====================================================================="
Add-Line "SET NAMES utf8mb4;"
Add-Line "SET FOREIGN_KEY_CHECKS=0;"
Add-Line "USE xingyun;"
Add-Line "-- 先清空业务表，保证可重复导入"
$tbls = @('course_favorite','certificate','course_qa','course_comment','user_coupon','coupon','course_order','assignment_submission','assignment','quiz_answer','wrong_question','quiz_question','quiz','play_record','learn_progress','course_ware','video_resource','course_lesson','course','course_category','sys_user_role','sys_role','sys_user','announcement')
foreach($t in $tbls){ Add-Line "TRUNCATE TABLE $t;" }
Add-Line ""

# ---------- 1. 角色 ----------
Add-Line "-- 1. 三种角色"
Add-Line "INSERT INTO sys_role(id,role_code,role_name,description) VALUES"
Add-Line " (1,'STUDENT','学员','在平台选课、学习、测验、作业、考取证书的普通用户'),"
Add-Line " (2,'TEACHER','认证讲师','创建并管理自己的课程、课时、作业批改与答疑'),"
Add-Line " (3,'ADMIN','平台管理员','拥有用户、课程审核、订单、运营配置等全部权限');"
Add-Line ""

# ---------- 2. 用户 ----------
Add-Line "-- 2. 用户：1 超管 + 5 讲师 + 20 学员，密码均为 123456"
$users = New-Object System.Collections.Generic.List[string]
$users.Add(" (1,'admin','$hash','超级管理员','平台管理员',1,'/upload/avatar/admin.png','admin@xingyun.com','13800000000',3,NULL,'平台超级管理员，拥有全部权限',0,0,0,NOW())") | Out-Null
$teacherNames = @(
 @('teacher1','张磊','男','资深Java全栈讲师','10年企业级开发与教学经验，擅长SpringBoot微服务与全栈项目落地'),
 @('teacher2','李婷','女','前端与移动开发讲师','精通Vue3、小程序与跨端开发，授课细腻注重实战'),
 @('teacher3','王博','男','AI算法讲师','人工智能博士，专注机器学习、深度学习工程落地'),
 @('teacher4','赵琳','女','数据科学讲师','资深数据分析师，擅长SQL、数据分析与大数据技术栈'),
 @('teacher5','陈浩','男','设计与职场讲师','资深UI/平面设计师，兼授职场效率与项目管理课程')
)
$tid = 2
foreach($t in $teacherNames){
  $gender = if($t[2] -eq '男'){1}else{2}
  $users.Add(" ($tid,'$($t[0])','$hash','$($t[1])','$($t[1])',$gender,'/upload/avatar/$($t[0]).png','$($t[0])@xingyun.com','1380000000$tid',2,'$($t[3])','$($t[4])',0,0,0,NOW())") | Out-Null
  $tid++
}
for($i=1;$i -le 20;$i++){
  $uid = 6 + $i
  $acc = ('student{0:d2}' -f $i)
  $nick = "学员{0:d2}号" -f $i
  $phoneNum = '139{0:d8}' -f $i
  $integral = Get-Random -Maximum 500
  $users.Add(" ($uid,'$acc','$hash','$nick','测试学员$i',0,'/upload/avatar/$acc.png','$acc@xingyun.com','$phoneNum',1,NULL,'热爱学习的星云学员',0,$integral,0,NOW())") | Out-Null
}
Add-Line "INSERT INTO sys_user(id,username,password,nickname,real_name,gender,avatar,email,phone,user_type,title,intro,study_minutes,integral,status,last_login_time) VALUES"
Add-Line (($users -join ",`r`n") + ";")
Add-Line ""

# ---------- 3. 用户角色关联 ----------
Add-Line "-- 3. 用户角色关联"
$ur = New-Object System.Collections.Generic.List[string]
$ur.Add(" (1,1,3,NOW())") | Out-Null   # admin->ADMIN
for($i=2;$i -le 6;$i++){ $ur.Add(" ($i,$i,2,NOW())") | Out-Null } # teacher->TEACHER
for($i=7;$i -le 26;$i++){ $urid=$i+20; $ur.Add(" ($urid,$i,1,NOW())") | Out-Null } # student->STUDENT, 关联id错开
Add-Line "INSERT INTO sys_user_role(id,user_id,role_id,create_time) VALUES"
Add-Line (($ur -join ",`r`n") + ";")
Add-Line ""

# ---------- 4. 分类：6一级 + 18二级 ----------
Add-Line "-- 4. 课程分类（6 个一级，每个 3 个二级，共 24 条）"
$cat1 = @('编程开发','AI与数据','设计创作','职场技能','升学考试','兴趣通识')
$cat2 = @(
 @('Java后端','前端开发','移动开发'),
 @('人工智能','数据分析','大数据技术'),
 @('UI设计','平面设计','影视剪辑'),
 @('办公效率','项目管理','沟通表达'),
 @('计算机考研','等级考试','专升本'),
 @('英语口语','摄影摄像','投资理财')
)
$catLines = New-Object System.Collections.Generic.List[string]
for($i=0;$i -lt 6;$i++){
  $parentCat = $i*4 + 1
  $catLines.Add(" ($parentCat,'$($cat1[$i])',0,1,NULL,$($i+1),0,NOW(),NOW(),0)") | Out-Null
  for($j=0;$j -lt 3;$j++){
    $cid2 = $parentCat + $j + 1
    $catLines.Add(" ($cid2,'$($cat2[$i][$j])',$parentCat,2,NULL,$($j+1),0,NOW(),NOW(),0)") | Out-Null
  }
}
Add-Line "INSERT INTO course_category(id,name,parent_id,level,icon,sort,status,create_time,update_time,is_deleted) VALUES"
Add-Line (($catLines -join ",`r`n") + ";")
Add-Line ""

# ---------- 5. 12门课程 ----------
Add-Line "-- 5. 12 门示例课程（免费课全开放，付费课由后端控制前3节试看）"
# 字段: 名称, 一级, 二级, 讲师, 价格(0免费), 原价, 难度, 标签, 简介, 推荐
$courses = @(
 @('Java零基础到SpringBoot全栈实战',1,2,2,199,399,1,'热门,后端,就业','从语法到SpringBoot项目，打通Java后端全链路',1),
 @('Vue3 + Element Plus 前端实战',1,3,3,159,299,1,'前端,Vue3,实战','组合式API+企业级后台项目，手把手带你做前端',1),
 @('微信小程序开发入门',1,4,3,0,0,1,'免费,小程序,入门','零基础开发你的第一个微信小程序，全程免费开放',0),
 @('Python人工智能入门',5,6,4,0,0,1,'免费,AI,Python','免费体验AI魅力，用Python开启人工智能大门',1),
 @('机器学习算法精讲',5,6,4,299,499,2,'AI,算法,进阶','经典监督/无监督算法推导与sklearn实战',0),
 @('MySQL数据分析实战',5,8,5,129,259,1,'数据库,分析,SQL','从增删改查到复杂分析查询，数据人必备',0),
 @('Hadoop + Spark 大数据入门',5,7,5,259,459,2,'大数据,Spark','分布式存储与计算核心技术入门到实践',0),
 @('UI界面设计从入门到精通',9,10,6,139,269,1,'设计,UI,作品','设计规范、组件、Figma实战，产出作品集',0),
 @('Photoshop平面设计综合案例',9,11,6,0,0,1,'免费,PS,设计','免费学PS，海报、抠图、修图案例全覆盖',0),
 @('职场Excel高效办公',13,14,2,0,0,1,'免费,Excel,办公','函数、透视表、快捷键，让效率翻倍',1),
 @('PMP项目管理实战课',13,15,4,199,399,2,'项目管理,PMP','五大过程组十大知识域，备考与实战并重',0),
 @('英语口语零基础突破',21,22,5,99,199,1,'英语,口语','场景化口语训练，告别哑巴英语',0)
)
$courseLines = New-Object System.Collections.Generic.List[string]
$lessonCount = 8
for($i=0;$i -lt 12;$i++){
  $c = $courses[$i]; $id = $i+1
  $isFree = if([double]$c[4] -eq 0){1}else{0}
  # 每课时固定 720 秒（12分钟），总时长=8*720=5760
  $totalDur = $lessonCount*720
  $studentCnt = 100 + (Get-Random -Maximum 900)
  $viewCnt = $studentCnt*3 + (Get-Random -Maximum 500)
  $desc = "本课程《$($c[0])》$($c[8])。课程共 $lessonCount 节，配套课件、随堂测验、作业与结业证书，学完可自动生成电子证书。"
  $courseLines.Add(" ($id,'$($c[0])','/upload/cover/course$id.jpg',$($c[1]),$($c[2]),$($c[3]),'$($c[8])','$desc',$($c[4]),$($c[5]),$isFree,$($c[6]),$lessonCount,$totalDur,$studentCnt,$viewCnt,4.8,'$($c[7])',$($c[9]),2,NULL,1,$($c[3]),$($c[3]),NOW(),NOW(),0)") | Out-Null
}
Add-Line "INSERT INTO course(id,course_name,cover,category_id,category2_id,teacher_id,intro,description,price,original_price,is_free,difficulty,lesson_count,total_duration,student_count,view_count,score,tags,is_recommend,audit_status,reject_reason,status,create_by,update_by,create_time,update_time,is_deleted) VALUES"
Add-Line (($courseLines -join ",`r`n") + ";")
Add-Line ""

# ---------- 6. 视频资源 96 条 ----------
Add-Line "-- 6. 视频资源（每课时 1 个，共 96 个；真实播放走防盗链签名接口）"
$videoLines = New-Object System.Collections.Generic.List[string]
for($i=1;$i -le 96;$i++){
  $courseNo = [int]([math]::Floor(($i-1)/8))+1
  $lessonNo = (($i-1)%8)+1
  $vname = ("课程{0}-第{1}节" -f $courseNo,$lessonNo)
  $size = 18*1024*1024 + $i*1024*1024
  $videoLine = " ($i,'$vname','video/c{0:d2}l{1:d2}.mp4',$size,720,NULL,'md5demoL{1:d2}C{0:d2}',0,$courseNo,NOW(),NOW(),0)" -f $courseNo,$lessonNo
  $videoLines.Add($videoLine) | Out-Null
}
Add-Line "INSERT INTO video_resource(id,video_name,file_path,file_size,duration,cover,md5,play_count,create_by,create_time,update_time,is_deleted) VALUES"
Add-Line (($videoLines -join ",`r`n") + ";")
Add-Line ""

# ---------- 7. 课时 96 节 ----------
Add-Line "-- 7. 课时（每门课 8 节，分两章；前 3 节标记为免费试看）"
$lessonLines = New-Object System.Collections.Generic.List[string]
$lid = 0
for($c=1;$c -le 12;$c++){
  for($j=1;$j -le 8;$j++){
    $lid++
    $chapter = if($j -le 4){"第一章 基础入门"}else{"第二章 进阶实战"}
    $lname = ("第{0}节 核心知识点{0}" -f $j)
    $trial = if($j -le 3){1}else{0}
    $lessonLines.Add(" ($lid,$c,'$chapter','$lname',$lid,720,$j,$trial,1,NOW(),NOW(),0)") | Out-Null
  }
}
Add-Line "INSERT INTO course_lesson(id,course_id,chapter,lesson_name,video_id,duration,sort,is_free_trial,status,create_time,update_time,is_deleted) VALUES"
Add-Line (($lessonLines -join ",`r`n") + ";")
Add-Line ""

# ---------- 8. 课件 12 ----------
Add-Line "-- 8. 课件（每门课 1 份讲义）"
$wareLines = New-Object System.Collections.Generic.List[string]
for($i=1;$i -le 12;$i++){
  $size = 2*1024*1024 + $i*1024*1024
  $wareLines.Add(" ($i,$i,0,'课程$i 配套讲义PDF','ware/course$i.pdf',$size,'pdf',0,NOW(),NOW(),0)") | Out-Null
}
Add-Line "INSERT INTO course_ware(id,course_id,lesson_id,ware_name,file_path,file_size,file_type,download_count,create_time,update_time,is_deleted) VALUES"
Add-Line (($wareLines -join ",`r`n") + ";")
Add-Line ""

# ---------- 9. 测验 12 + 题目 48 ----------
Add-Line "-- 9. 随堂测验（每门课 1 套，挂第4节，每套4题）"
$quizLines = New-Object System.Collections.Generic.List[string]
for($i=1;$i -le 12;$i++){
  $lessonId = ($i-1)*8 + 4
  $quizLines.Add(" ($i,$i,$lessonId,'课程$i 阶段小测',4,100,60,1,NOW(),NOW(),0)") | Out-Null
}
Add-Line "INSERT INTO quiz(id,course_id,lesson_id,quiz_name,question_count,total_score,pass_score,status,create_time,update_time,is_deleted) VALUES"
Add-Line (($quizLines -join ",`r`n") + ";")
Add-Line ""

Add-Line "-- 10. 测验题目（每套 4 题：3 单选 + 1 判断，每题 25 分）"
$qLines = New-Object System.Collections.Generic.List[string]
$qid = 0
for($q=1;$q -le 12;$q++){
  $qid++; $qLines.Add(" ($qid,$q,1,'本课程的核心学习方法是什么？','只看视频不练习','跟着视频敲代码并完成测验作业','跳过基础直接看进阶','只背面试题','B',25,'理论必须结合动手实践',1,NOW(),0)") | Out-Null
  $qid++; $qLines.Add(" ($qid,$q,1,'遇到报错最合理的第一步是？','立刻放弃','复制报错信息阅读并定位','重装系统','无视报错','A',25,'先阅读报错信息、定位堆栈，是排错第一步',2,NOW(),0)") | Out-Null
  $qid++; $qLines.Add(" ($qid,$q,1,'学习进度达到多少会自动生成结业证书？','50%','80%','100%','不需要学习','C',25,'全部课时学完(100%)自动生成证书',3,NOW(),0)") | Out-Null
  $qid++; $qLines.Add(" ($qid,$q,3,'付费课程前3节可以免费试看。','正确','错误',NULL,NULL,'TRUE',25,'平台规则：付费课支持前3节免费试看',4,NOW(),0)") | Out-Null
}
Add-Line "INSERT INTO quiz_question(id,quiz_id,question_type,content,option_a,option_b,option_c,option_d,answer,score,analysis,sort,create_time,is_deleted) VALUES"
Add-Line (($qLines -join ",`r`n") + ";")
Add-Line ""

# ---------- 11. 作业 12 ----------
Add-Line "-- 11. 作业（每门课 1 次，挂第8节）"
$aLines = New-Object System.Collections.Generic.List[string]
for($i=1;$i -le 12;$i++){
  $lessonId = ($i-1)*8 + 8
  $teacher = $courses[$i-1][3]
  $deadline = "DATE_ADD(NOW(), INTERVAL 30 DAY)"
  $aLines.Add(" ($i,$i,$lessonId,$teacher,'课程$i 结业作业','结合本课程所学，完成一个综合实践并提交总结，要求步骤完整、截图清晰。',NULL,100,$deadline,NOW(),NOW(),0)") | Out-Null
}
Add-Line "INSERT INTO assignment(id,course_id,lesson_id,teacher_id,title,content,attachment,total_score,deadline,create_time,update_time,is_deleted) VALUES"
Add-Line (($aLines -join ",`r`n") + ";")
Add-Line ""

# ---------- 12. 优惠券 3 ----------
Add-Line "-- 12. 优惠券（满减/立减/折扣 三种）"
Add-Line "INSERT INTO coupon(id,coupon_name,coupon_type,threshold,discount_value,total_count,receive_count,valid_days,status,create_time,update_time,is_deleted) VALUES"
Add-Line " (1,'满100减20新人券',1,100.00,20.00,1000,0,30,1,NOW(),NOW(),0),"
Add-Line " (2,'无门槛立减10元券',2,0.00,10.00,500,0,15,1,NOW(),NOW(),0),"
Add-Line " (3,'全场85折券',3,0.00,8.50,300,0,7,1,NOW(),NOW(),0);"
Add-Line ""

# ---------- 13. 订单（学员购买付费课，已支付） ----------
Add-Line "-- 13. 课程订单（演示已购关系；免费课无需订单）"
# 购买关系: 学员->课程列表
$buys = @{
 7=@(1,2,5); 8=@(1,6); 9=@(2,8); 10=@(5,7,11); 11=@(1,12); 12=@(2); 13=@(1); 14=@(8,11)
}
$oLines = New-Object System.Collections.Generic.List[string]
$oid=0; $seq=0
foreach($uid in ($buys.Keys | Sort-Object)){
  foreach($courseId in $buys[$uid]){
    $oid++
    $price = [double]$courses[$courseId-1][4]
    $seq++
    $orderNo = "XY20260901{0:d6}" -f $seq
    $oLines.Add(" ($oid,'$orderNo',$uid,$courseId,$price,0.00,$price,NULL,1,1,DATE_SUB(NOW(),INTERVAL $oid DAY),NOW(),NOW(),0)") | Out-Null
  }
}
Add-Line "INSERT INTO course_order(id,order_no,user_id,course_id,original_amount,discount_amount,pay_amount,coupon_id,pay_type,status,pay_time,create_time,update_time,is_deleted) VALUES"
Add-Line (($oLines -join ",`r`n") + ";")
Add-Line ""

# ---------- 14. 用户优惠券 ----------
Add-Line "-- 14. 部分学员领取优惠券"
$ucLines = New-Object System.Collections.Generic.List[string]
$ucid=0
foreach($pair in @(@(7,1),@(8,1),@(9,2),@(10,3),@(11,2),@(12,1),@(13,3))){
  $ucid++
  $ucLines.Add(" ($ucid,$($pair[0]),$($pair[1]),0,NULL,NOW(),DATE_ADD(NOW(),INTERVAL 30 DAY),NULL)") | Out-Null
}
Add-Line "INSERT INTO user_coupon(id,user_id,coupon_id,status,order_id,receive_time,expire_time,use_time) VALUES"
Add-Line (($ucLines -join ",`r`n") + ";")
Add-Line ""

# ---------- 15. 学习进度 + 证书 ----------
Add-Line "-- 15. 学习进度（已完成课程全部100%并生成证书；学习中课程只学到部分课时）"
$pLines = New-Object System.Collections.Generic.List[string]
$certLines = New-Object System.Collections.Generic.List[string]
$certNo=0
function Gen-FullProgress([int]$uid,[int]$courseId,[System.Collections.Generic.List[string]]$pLines){
  for($j=1;$j -le 8;$j++){
    $lessonId=($courseId-1)*8+$j
    $pLines.Add(" (0,$uid,$courseId,$lessonId,100,720,720,1,DATE_SUB(NOW(),INTERVAL 1 HOUR),DATE_SUB(NOW(),INTERVAL 2 HOUR),NOW(),NOW(),0)") | Out-Null
  }
}
function Gen-DoingProgress([int]$uid,[int]$courseId,[System.Collections.Generic.List[string]]$pLines){
  foreach($j in 1..3){
    $lessonId=($courseId-1)*8+$j
    if($j -le 2){ $pLines.Add(" (0,$uid,$courseId,$lessonId,100,720,720,1,DATE_SUB(NOW(),INTERVAL 3 HOUR),DATE_SUB(NOW(),INTERVAL 5 HOUR),NOW(),NOW(),0)") | Out-Null }
    else { $pLines.Add(" (0,$uid,$courseId,$lessonId,45,324,324,0,NULL,NOW(),NOW(),NOW(),0)") | Out-Null }
  }
}
# 已完成（发证）：7->1,8->1,9->2,8->免费课4
Gen-FullProgress 7 1 $pLines
Gen-FullProgress 8 1 $pLines
Gen-FullProgress 9 2 $pLines
Gen-FullProgress 8 4 $pLines
# 进行中
Gen-DoingProgress 7 2 $pLines
Gen-DoingProgress 10 5 $pLines
Gen-DoingProgress 7 3 $pLines
# 去掉占位id 0，让其自增：改为不显式写id。这里直接输出（id列用NULL）
$pLines2 = $pLines | ForEach-Object { $_ -replace '^\(0,','(NULL,' }
Add-Line "INSERT INTO learn_progress(id,user_id,course_id,lesson_id,progress,position_seconds,learned_seconds,status,finish_time,last_learn_time,create_time,update_time,is_deleted) VALUES"
Add-Line (($pLines2 -join ",`r`n") + ";")
Add-Line ""
Add-Line "-- 16. 结业证书（与上方已完成课程对应）"
$certDefs = @(@(7,1),@(8,1),@(9,2),@(8,4))
$cLines = New-Object System.Collections.Generic.List[string]
$ci=0
foreach($d in $certDefs){
  $ci++; $certNo='XY202609{0:d4}' -f $ci
  $cname = "《$($courses[$d[1]-1][0])》结业证书"
  $cLines.Add(" ($ci,'$certNo',$($d[0]),$($d[1]),'$cname',DATE_SUB(NOW(),INTERVAL 1 HOUR),1,NOW(),0)") | Out-Null
}
Add-Line "INSERT INTO certificate(id,cert_no,user_id,course_id,certificate_name,issue_time,status,create_time,is_deleted) VALUES"
Add-Line (($cLines -join ",`r`n") + ";")
Add-Line ""

# ---------- 17. 播放流水 ----------
Add-Line "-- 17. 播放打卡流水（示例）"
$prLines = New-Object System.Collections.Generic.List[string]
for($i=1;$i -le 8;$i++){
  $uid = 6+($i%6)+1
  $lessonId = $i
  $prLines.Add(" (0,$uid,1,$lessonId,$i*90,'Chrome/Windows',NOW())") | Out-Null
}
Add-Line "INSERT INTO play_record(id,user_id,course_id,lesson_id,position_sec,device,create_time) VALUES"
Add-Line ((($prLines | ForEach-Object { $_ -replace '^\(0,','(NULL,' }) -join ",`r`n") + ";")
Add-Line ""

# ---------- 18. 作业提交 ----------
Add-Line "-- 18. 作业提交与批改（部分待批、部分已批）"
$subs = @(
 @(1,7,1,'已完成课程1综合实践，详见文档总结。',1,92,'完成度高，继续保持。',2),
 @(2,8,1,'课程1作业提交，包含截图与代码。',1,88,'思路清晰，注意命名规范。',2),
 @(3,9,2,'课程2前端页面实战作业。',0,$null,$null,$null,2),
 @(4,10,5,'机器学习小项目作业。',1,85,'模型评估部分可再深入。',4),
 @(5,7,3,'小程序入门练习作业。',0,$null,$null,$null,3)
)
$sLines = New-Object System.Collections.Generic.List[string]
$si=0
foreach($s in $subs){
  $si++
  $courseId=$s[2]
  $grade = if($s[4] -eq 1){ "'"+$s[5]+"'","'"+$s[6]+"'",$s[7] } else { 'NULL','NULL','NULL' }
  if($s[4] -eq 1){
    $sLines.Add(" ($si,$($s[1]),$courseId,$($s[2]),'$($s[3])',NULL,DATE_SUB(NOW(),INTERVAL 2 DAY),$($s[4]),$($s[5]),'$($s[6])',$($s[7]),DATE_SUB(NOW(),INTERVAL 1 DAY),NOW(),0)") | Out-Null
  } else {
    $sLines.Add(" ($si,$($s[1]),$courseId,$($s[2]),'$($s[3])',NULL,DATE_SUB(NOW(),INTERVAL 2 DAY),$($s[4]),NULL,NULL,NULL,NULL,NOW(),0)") | Out-Null
  }
}
Add-Line "INSERT INTO assignment_submission(id,user_id,course_id,assignment_id,content,attachment,submit_time,grade_status,score,teacher_comment,teacher_id,grade_time,update_time,is_deleted) VALUES"
Add-Line (($sLines -join ",`r`n") + ";")
Add-Line ""

# ---------- 19. 评论 ----------
Add-Line "-- 19. 课程评论（每门课 2 条）"
$cmLines = New-Object System.Collections.Generic.List[string]
$cmi=0
for($c=1;$c -le 12;$c++){
  $cmi++; $u1=6+(($c*2-1)%20)+1
  $cmLines.Add(" ($cmi,$c,$u1,0,'老师讲得非常清晰，零基础也能跟上，强烈推荐！',5,1,NOW(),0)") | Out-Null
  $cmi++; $u2=6+(($c*2)%20)+1
  $cmLines.Add(" ($cmi,$c,$u2,0,'课程内容很扎实，配套资料齐全，性价比高。',4,1,NOW(),0)") | Out-Null
}
Add-Line "INSERT INTO course_comment(id,course_id,user_id,parent_id,content,score,status,create_time,is_deleted) VALUES"
Add-Line (($cmLines -join ",`r`n") + ";")
Add-Line ""

# ---------- 20. 问答 ----------
Add-Line "-- 20. 课程问答（一半已回复，一半待回复）"
$qaLines = New-Object System.Collections.Generic.List[string]
$qa=0
for($c=1;$c -le 12;$c++){
  $qa++; $u=6+($c%20)+1
  if($c % 2 -eq 0){
    $teacher=$courses[$c-1][3]
    $qaLines.Add(" ($qa,$c,0,$u,'请问第$c门课这一节的重点该如何复习？',$teacher,'建议结合随堂测验与课件，把每个知识点动手实践一遍。',DATE_SUB(NOW(),INTERVAL 1 DAY),1,NOW(),NOW(),0)") | Out-Null
  } else {
    $qaLines.Add(" ($qa,$c,0,$u,'这里没太听懂，能再讲讲吗？',NULL,NULL,NULL,0,NOW(),NOW(),0)") | Out-Null
  }
}
Add-Line "INSERT INTO course_qa(id,course_id,lesson_id,user_id,question,reply_user_id,reply_content,reply_time,status,create_time,update_time,is_deleted) VALUES"
Add-Line (($qaLines -join ",`r`n") + ";")
Add-Line ""

# ---------- 21. 收藏 ----------
Add-Line "-- 21. 课程收藏"
$favLines = New-Object System.Collections.Generic.List[string]
$f=0
foreach($pair in @(@(7,2),@(7,5),@(8,2),@(9,1),@(10,1),@(11,4),@(12,3))){
  $f++; $favLines.Add(" ($f,$($pair[0]),$($pair[1]),NOW())") | Out-Null
}
Add-Line "INSERT INTO course_favorite(id,user_id,course_id,create_time) VALUES"
Add-Line (($favLines -join ",`r`n") + ";")
Add-Line ""

# ---------- 22. 公告 ----------
Add-Line "-- 22. 平台公告"
Add-Line "INSERT INTO announcement(id,title,content,type,status,publisher_id,publish_time,create_time,update_time,is_deleted) VALUES"
Add-Line " (1,'欢迎来到星云在线教育平台','平台Slogan：知识点亮星云。新用户注册即可免费学习全部免费课程，付费课程支持前3节免费试看。',1,1,1,NOW(),NOW(),NOW(),0),"
Add-Line " (2,'关于结业证书自动生成的说明','当你完成某门课程全部课时（学习进度100%）后，系统将自动生成电子结业证书，可在「学习档案-我的证书」中查看下载。',1,1,1,NOW(),NOW(),NOW(),0),"
Add-Line " (3,'新人优惠券发放通知','本月新注册学员可在个人中心领取新人优惠券，购买付费课程时自动抵扣，数量有限先到先得。',2,1,1,NOW(),NOW(),NOW(),0),"
Add-Line " (4,'文明学习与评论规范','请大家在课程评论与问答区文明交流，违规内容将被屏蔽，共同营造良好学习氛围。',1,1,1,NOW(),NOW(),NOW(),0);"
Add-Line ""
Add-Line "SET FOREIGN_KEY_CHECKS=1;"
Add-Line "SELECT CONCAT('初始数据导入完成：用户',(SELECT COUNT(*) FROM sys_user),' 课程',(SELECT COUNT(*) FROM course),' 课时',(SELECT COUNT(*) FROM course_lesson)) AS result_msg;"

# 写文件（UTF8 无 BOM）
$text = [string]::Join("`r`n", $sb)
[System.IO.File]::WriteAllText($outFile, $text, (New-Object System.Text.UTF8Encoding($false)))
Write-Host "generated: $outFile  lines=$($sb.Count)"
