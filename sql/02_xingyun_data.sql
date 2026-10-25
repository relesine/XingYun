-- =====================================================================
-- XingYun 初始测试数据脚本（由 gen_data.ps1 生成，可直接重复执行）
-- =====================================================================
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS=0;
USE xingyun;
-- 先清空业务表，保证可重复导入
TRUNCATE TABLE course_favorite;
TRUNCATE TABLE certificate;
TRUNCATE TABLE course_qa;
TRUNCATE TABLE course_comment;
TRUNCATE TABLE user_coupon;
TRUNCATE TABLE coupon;
TRUNCATE TABLE course_order;
TRUNCATE TABLE assignment_submission;
TRUNCATE TABLE assignment;
TRUNCATE TABLE quiz_answer;
TRUNCATE TABLE wrong_question;
TRUNCATE TABLE quiz_question;
TRUNCATE TABLE quiz;
TRUNCATE TABLE play_record;
TRUNCATE TABLE learn_progress;
TRUNCATE TABLE course_ware;
TRUNCATE TABLE video_resource;
TRUNCATE TABLE course_lesson;
TRUNCATE TABLE course;
TRUNCATE TABLE course_category;
TRUNCATE TABLE sys_user_role;
TRUNCATE TABLE sys_role;
TRUNCATE TABLE sys_user;
TRUNCATE TABLE announcement;

-- 1. 三种角色
INSERT INTO sys_role(id,role_code,role_name,description) VALUES
 (1,'STUDENT','学员','在平台选课、学习、测验、作业、考取证书的普通用户'),
 (2,'TEACHER','认证讲师','创建并管理自己的课程、课时、作业批改与答疑'),
 (3,'ADMIN','平台管理员','拥有用户、课程审核、订单、运营配置等全部权限');

-- 2. 用户：1 超管 + 5 讲师 + 20 学员，密码均为 123456
INSERT INTO sys_user(id,username,password,nickname,real_name,gender,avatar,email,phone,user_type,title,intro,study_minutes,integral,status,last_login_time) VALUES
 (1,'admin','$2a$10$/6PxQH7.p1l2hJ.XHZwSruZ6utXGNQfqeEaaBRoh5BvvHipANuoaG','超级管理员','平台管理员',1,'/upload/avatar/admin.png','admin@xingyun.com','13800000000',3,NULL,'平台超级管理员，拥有全部权限',0,0,0,NOW()),
 (2,'teacher1','$2a$10$/6PxQH7.p1l2hJ.XHZwSruZ6utXGNQfqeEaaBRoh5BvvHipANuoaG','张磊','张磊',1,'/upload/avatar/teacher1.png','teacher1@xingyun.com','13800000002',2,'资深Java全栈讲师','10年企业级开发与教学经验，擅长SpringBoot微服务与全栈项目落地',0,0,0,NOW()),
 (3,'teacher2','$2a$10$/6PxQH7.p1l2hJ.XHZwSruZ6utXGNQfqeEaaBRoh5BvvHipANuoaG','李婷','李婷',2,'/upload/avatar/teacher2.png','teacher2@xingyun.com','13800000003',2,'前端与移动开发讲师','精通Vue3、小程序与跨端开发，授课细腻注重实战',0,0,0,NOW()),
 (4,'teacher3','$2a$10$/6PxQH7.p1l2hJ.XHZwSruZ6utXGNQfqeEaaBRoh5BvvHipANuoaG','王博','王博',1,'/upload/avatar/teacher3.png','teacher3@xingyun.com','13800000004',2,'AI算法讲师','人工智能博士，专注机器学习、深度学习工程落地',0,0,0,NOW()),
 (5,'teacher4','$2a$10$/6PxQH7.p1l2hJ.XHZwSruZ6utXGNQfqeEaaBRoh5BvvHipANuoaG','赵琳','赵琳',2,'/upload/avatar/teacher4.png','teacher4@xingyun.com','13800000005',2,'数据科学讲师','资深数据分析师，擅长SQL、数据分析与大数据技术栈',0,0,0,NOW()),
 (6,'teacher5','$2a$10$/6PxQH7.p1l2hJ.XHZwSruZ6utXGNQfqeEaaBRoh5BvvHipANuoaG','陈浩','陈浩',1,'/upload/avatar/teacher5.png','teacher5@xingyun.com','13800000006',2,'设计与职场讲师','资深UI/平面设计师，兼授职场效率与项目管理课程',0,0,0,NOW()),
 (7,'student01','$2a$10$/6PxQH7.p1l2hJ.XHZwSruZ6utXGNQfqeEaaBRoh5BvvHipANuoaG','学员01号','测试学员1',0,'/upload/avatar/student01.png','student01@xingyun.com','13900000001',1,NULL,'热爱学习的星云学员',0,466,0,NOW()),
 (8,'student02','$2a$10$/6PxQH7.p1l2hJ.XHZwSruZ6utXGNQfqeEaaBRoh5BvvHipANuoaG','学员02号','测试学员2',0,'/upload/avatar/student02.png','student02@xingyun.com','13900000002',1,NULL,'热爱学习的星云学员',0,274,0,NOW()),
 (9,'student03','$2a$10$/6PxQH7.p1l2hJ.XHZwSruZ6utXGNQfqeEaaBRoh5BvvHipANuoaG','学员03号','测试学员3',0,'/upload/avatar/student03.png','student03@xingyun.com','13900000003',1,NULL,'热爱学习的星云学员',0,333,0,NOW()),
 (10,'student04','$2a$10$/6PxQH7.p1l2hJ.XHZwSruZ6utXGNQfqeEaaBRoh5BvvHipANuoaG','学员04号','测试学员4',0,'/upload/avatar/student04.png','student04@xingyun.com','13900000004',1,NULL,'热爱学习的星云学员',0,311,0,NOW()),
 (11,'student05','$2a$10$/6PxQH7.p1l2hJ.XHZwSruZ6utXGNQfqeEaaBRoh5BvvHipANuoaG','学员05号','测试学员5',0,'/upload/avatar/student05.png','student05@xingyun.com','13900000005',1,NULL,'热爱学习的星云学员',0,469,0,NOW()),
 (12,'student06','$2a$10$/6PxQH7.p1l2hJ.XHZwSruZ6utXGNQfqeEaaBRoh5BvvHipANuoaG','学员06号','测试学员6',0,'/upload/avatar/student06.png','student06@xingyun.com','13900000006',1,NULL,'热爱学习的星云学员',0,64,0,NOW()),
 (13,'student07','$2a$10$/6PxQH7.p1l2hJ.XHZwSruZ6utXGNQfqeEaaBRoh5BvvHipANuoaG','学员07号','测试学员7',0,'/upload/avatar/student07.png','student07@xingyun.com','13900000007',1,NULL,'热爱学习的星云学员',0,41,0,NOW()),
 (14,'student08','$2a$10$/6PxQH7.p1l2hJ.XHZwSruZ6utXGNQfqeEaaBRoh5BvvHipANuoaG','学员08号','测试学员8',0,'/upload/avatar/student08.png','student08@xingyun.com','13900000008',1,NULL,'热爱学习的星云学员',0,452,0,NOW()),
 (15,'student09','$2a$10$/6PxQH7.p1l2hJ.XHZwSruZ6utXGNQfqeEaaBRoh5BvvHipANuoaG','学员09号','测试学员9',0,'/upload/avatar/student09.png','student09@xingyun.com','13900000009',1,NULL,'热爱学习的星云学员',0,416,0,NOW()),
 (16,'student10','$2a$10$/6PxQH7.p1l2hJ.XHZwSruZ6utXGNQfqeEaaBRoh5BvvHipANuoaG','学员10号','测试学员10',0,'/upload/avatar/student10.png','student10@xingyun.com','13900000010',1,NULL,'热爱学习的星云学员',0,157,0,NOW()),
 (17,'student11','$2a$10$/6PxQH7.p1l2hJ.XHZwSruZ6utXGNQfqeEaaBRoh5BvvHipANuoaG','学员11号','测试学员11',0,'/upload/avatar/student11.png','student11@xingyun.com','13900000011',1,NULL,'热爱学习的星云学员',0,292,0,NOW()),
 (18,'student12','$2a$10$/6PxQH7.p1l2hJ.XHZwSruZ6utXGNQfqeEaaBRoh5BvvHipANuoaG','学员12号','测试学员12',0,'/upload/avatar/student12.png','student12@xingyun.com','13900000012',1,NULL,'热爱学习的星云学员',0,481,0,NOW()),
 (19,'student13','$2a$10$/6PxQH7.p1l2hJ.XHZwSruZ6utXGNQfqeEaaBRoh5BvvHipANuoaG','学员13号','测试学员13',0,'/upload/avatar/student13.png','student13@xingyun.com','13900000013',1,NULL,'热爱学习的星云学员',0,254,0,NOW()),
 (20,'student14','$2a$10$/6PxQH7.p1l2hJ.XHZwSruZ6utXGNQfqeEaaBRoh5BvvHipANuoaG','学员14号','测试学员14',0,'/upload/avatar/student14.png','student14@xingyun.com','13900000014',1,NULL,'热爱学习的星云学员',0,48,0,NOW()),
 (21,'student15','$2a$10$/6PxQH7.p1l2hJ.XHZwSruZ6utXGNQfqeEaaBRoh5BvvHipANuoaG','学员15号','测试学员15',0,'/upload/avatar/student15.png','student15@xingyun.com','13900000015',1,NULL,'热爱学习的星云学员',0,43,0,NOW()),
 (22,'student16','$2a$10$/6PxQH7.p1l2hJ.XHZwSruZ6utXGNQfqeEaaBRoh5BvvHipANuoaG','学员16号','测试学员16',0,'/upload/avatar/student16.png','student16@xingyun.com','13900000016',1,NULL,'热爱学习的星云学员',0,412,0,NOW()),
 (23,'student17','$2a$10$/6PxQH7.p1l2hJ.XHZwSruZ6utXGNQfqeEaaBRoh5BvvHipANuoaG','学员17号','测试学员17',0,'/upload/avatar/student17.png','student17@xingyun.com','13900000017',1,NULL,'热爱学习的星云学员',0,117,0,NOW()),
 (24,'student18','$2a$10$/6PxQH7.p1l2hJ.XHZwSruZ6utXGNQfqeEaaBRoh5BvvHipANuoaG','学员18号','测试学员18',0,'/upload/avatar/student18.png','student18@xingyun.com','13900000018',1,NULL,'热爱学习的星云学员',0,304,0,NOW()),
 (25,'student19','$2a$10$/6PxQH7.p1l2hJ.XHZwSruZ6utXGNQfqeEaaBRoh5BvvHipANuoaG','学员19号','测试学员19',0,'/upload/avatar/student19.png','student19@xingyun.com','13900000019',1,NULL,'热爱学习的星云学员',0,217,0,NOW()),
 (26,'student20','$2a$10$/6PxQH7.p1l2hJ.XHZwSruZ6utXGNQfqeEaaBRoh5BvvHipANuoaG','学员20号','测试学员20',0,'/upload/avatar/student20.png','student20@xingyun.com','13900000020',1,NULL,'热爱学习的星云学员',0,149,0,NOW());

-- 3. 用户角色关联
INSERT INTO sys_user_role(id,user_id,role_id,create_time) VALUES
 (1,1,3,NOW()),
 (2,2,2,NOW()),
 (3,3,2,NOW()),
 (4,4,2,NOW()),
 (5,5,2,NOW()),
 (6,6,2,NOW()),
 (27,7,1,NOW()),
 (28,8,1,NOW()),
 (29,9,1,NOW()),
 (30,10,1,NOW()),
 (31,11,1,NOW()),
 (32,12,1,NOW()),
 (33,13,1,NOW()),
 (34,14,1,NOW()),
 (35,15,1,NOW()),
 (36,16,1,NOW()),
 (37,17,1,NOW()),
 (38,18,1,NOW()),
 (39,19,1,NOW()),
 (40,20,1,NOW()),
 (41,21,1,NOW()),
 (42,22,1,NOW()),
 (43,23,1,NOW()),
 (44,24,1,NOW()),
 (45,25,1,NOW()),
 (46,26,1,NOW());

-- 4. 课程分类（6 个一级，每个 3 个二级，共 24 条）
INSERT INTO course_category(id,name,parent_id,level,icon,sort,status,create_time,update_time,is_deleted) VALUES
 (1,'编程开发',0,1,NULL,1,0,NOW(),NOW(),0),
 (2,'Java后端',1,2,NULL,1,0,NOW(),NOW(),0),
 (3,'前端开发',1,2,NULL,2,0,NOW(),NOW(),0),
 (4,'移动开发',1,2,NULL,3,0,NOW(),NOW(),0),
 (5,'AI与数据',0,1,NULL,2,0,NOW(),NOW(),0),
 (6,'人工智能',5,2,NULL,1,0,NOW(),NOW(),0),
 (7,'数据分析',5,2,NULL,2,0,NOW(),NOW(),0),
 (8,'大数据技术',5,2,NULL,3,0,NOW(),NOW(),0),
 (9,'设计创作',0,1,NULL,3,0,NOW(),NOW(),0),
 (10,'UI设计',9,2,NULL,1,0,NOW(),NOW(),0),
 (11,'平面设计',9,2,NULL,2,0,NOW(),NOW(),0),
 (12,'影视剪辑',9,2,NULL,3,0,NOW(),NOW(),0),
 (13,'职场技能',0,1,NULL,4,0,NOW(),NOW(),0),
 (14,'办公效率',13,2,NULL,1,0,NOW(),NOW(),0),
 (15,'项目管理',13,2,NULL,2,0,NOW(),NOW(),0),
 (16,'沟通表达',13,2,NULL,3,0,NOW(),NOW(),0),
 (17,'升学考试',0,1,NULL,5,0,NOW(),NOW(),0),
 (18,'计算机考研',17,2,NULL,1,0,NOW(),NOW(),0),
 (19,'等级考试',17,2,NULL,2,0,NOW(),NOW(),0),
 (20,'专升本',17,2,NULL,3,0,NOW(),NOW(),0),
 (21,'兴趣通识',0,1,NULL,6,0,NOW(),NOW(),0),
 (22,'英语口语',21,2,NULL,1,0,NOW(),NOW(),0),
 (23,'摄影摄像',21,2,NULL,2,0,NOW(),NOW(),0),
 (24,'投资理财',21,2,NULL,3,0,NOW(),NOW(),0);

-- 5. 12 门示例课程（免费课全开放，付费课由后端控制前3节试看）
INSERT INTO course(id,course_name,cover,category_id,category2_id,teacher_id,intro,description,price,original_price,is_free,difficulty,lesson_count,total_duration,student_count,view_count,score,tags,is_recommend,audit_status,reject_reason,status,create_by,update_by,create_time,update_time,is_deleted) VALUES
 (1,'Java零基础到SpringBoot全栈实战','/upload/cover/course1.jpg',1,2,2,'从语法到SpringBoot项目，打通Java后端全链路','本课程《Java零基础到SpringBoot全栈实战》从语法到SpringBoot项目，打通Java后端全链路。课程共 8 节，配套课件、随堂测验、作业与结业证书，学完可自动生成电子证书。',199,399,0,1,8,5760,774,2800,4.8,'热门,后端,就业',1,2,NULL,1,2,2,NOW(),NOW(),0),
 (2,'Vue3 + Element Plus 前端实战','/upload/cover/course2.jpg',1,3,3,'组合式API+企业级后台项目，手把手带你做前端','本课程《Vue3 + Element Plus 前端实战》组合式API+企业级后台项目，手把手带你做前端。课程共 8 节，配套课件、随堂测验、作业与结业证书，学完可自动生成电子证书。',159,299,0,1,8,5760,669,2416,4.8,'前端,Vue3,实战',1,2,NULL,1,3,3,NOW(),NOW(),0),
 (3,'微信小程序开发入门','/upload/cover/course3.jpg',1,4,3,'零基础开发你的第一个微信小程序，全程免费开放','本课程《微信小程序开发入门》零基础开发你的第一个微信小程序，全程免费开放。课程共 8 节，配套课件、随堂测验、作业与结业证书，学完可自动生成电子证书。',0,0,1,1,8,5760,405,1706,4.8,'免费,小程序,入门',0,2,NULL,1,3,3,NOW(),NOW(),0),
 (4,'Python人工智能入门','/upload/cover/course4.jpg',5,6,4,'免费体验AI魅力，用Python开启人工智能大门','本课程《Python人工智能入门》免费体验AI魅力，用Python开启人工智能大门。课程共 8 节，配套课件、随堂测验、作业与结业证书，学完可自动生成电子证书。',0,0,1,1,8,5760,499,1705,4.8,'免费,AI,Python',1,2,NULL,1,4,4,NOW(),NOW(),0),
 (5,'机器学习算法精讲','/upload/cover/course5.jpg',5,6,4,'经典监督/无监督算法推导与sklearn实战','本课程《机器学习算法精讲》经典监督/无监督算法推导与sklearn实战。课程共 8 节，配套课件、随堂测验、作业与结业证书，学完可自动生成电子证书。',299,499,0,2,8,5760,773,2581,4.8,'AI,算法,进阶',0,2,NULL,1,4,4,NOW(),NOW(),0),
 (6,'MySQL数据分析实战','/upload/cover/course6.jpg',5,8,5,'从增删改查到复杂分析查询，数据人必备','本课程《MySQL数据分析实战》从增删改查到复杂分析查询，数据人必备。课程共 8 节，配套课件、随堂测验、作业与结业证书，学完可自动生成电子证书。',129,259,0,1,8,5760,871,2773,4.8,'数据库,分析,SQL',0,2,NULL,1,5,5,NOW(),NOW(),0),
 (7,'Hadoop + Spark 大数据入门','/upload/cover/course7.jpg',5,7,5,'分布式存储与计算核心技术入门到实践','本课程《Hadoop + Spark 大数据入门》分布式存储与计算核心技术入门到实践。课程共 8 节，配套课件、随堂测验、作业与结业证书，学完可自动生成电子证书。',259,459,0,2,8,5760,122,478,4.8,'大数据,Spark',0,2,NULL,1,5,5,NOW(),NOW(),0),
 (8,'UI界面设计从入门到精通','/upload/cover/course8.jpg',9,10,6,'设计规范、组件、Figma实战，产出作品集','本课程《UI界面设计从入门到精通》设计规范、组件、Figma实战，产出作品集。课程共 8 节，配套课件、随堂测验、作业与结业证书，学完可自动生成电子证书。',139,269,0,1,8,5760,616,2094,4.8,'设计,UI,作品',0,2,NULL,1,6,6,NOW(),NOW(),0),
 (9,'Photoshop平面设计综合案例','/upload/cover/course9.jpg',9,11,6,'免费学PS，海报、抠图、修图案例全覆盖','本课程《Photoshop平面设计综合案例》免费学PS，海报、抠图、修图案例全覆盖。课程共 8 节，配套课件、随堂测验、作业与结业证书，学完可自动生成电子证书。',0,0,1,1,8,5760,287,1309,4.8,'免费,PS,设计',0,2,NULL,1,6,6,NOW(),NOW(),0),
 (10,'职场Excel高效办公','/upload/cover/course10.jpg',13,14,2,'函数、透视表、快捷键，让效率翻倍','本课程《职场Excel高效办公》函数、透视表、快捷键，让效率翻倍。课程共 8 节，配套课件、随堂测验、作业与结业证书，学完可自动生成电子证书。',0,0,1,1,8,5760,908,3078,4.8,'免费,Excel,办公',1,2,NULL,1,2,2,NOW(),NOW(),0),
 (11,'PMP项目管理实战课','/upload/cover/course11.jpg',13,15,4,'五大过程组十大知识域，备考与实战并重','本课程《PMP项目管理实战课》五大过程组十大知识域，备考与实战并重。课程共 8 节，配套课件、随堂测验、作业与结业证书，学完可自动生成电子证书。',199,399,0,2,8,5760,907,3193,4.8,'项目管理,PMP',0,2,NULL,1,4,4,NOW(),NOW(),0),
 (12,'英语口语零基础突破','/upload/cover/course12.jpg',21,22,5,'场景化口语训练，告别哑巴英语','本课程《英语口语零基础突破》场景化口语训练，告别哑巴英语。课程共 8 节，配套课件、随堂测验、作业与结业证书，学完可自动生成电子证书。',99,199,0,1,8,5760,343,1331,4.8,'英语,口语',0,2,NULL,1,5,5,NOW(),NOW(),0);

-- 6. 视频资源（每课时 1 个，共 96 个；真实播放走防盗链签名接口）
INSERT INTO video_resource(id,video_name,file_path,file_size,duration,cover,md5,play_count,create_by,create_time,update_time,is_deleted) VALUES
 (1,'课程1-第1节','video/c01l01.mp4',19922944,720,NULL,'md5demoL01C01',0,1,NOW(),NOW(),0),
 (2,'课程1-第2节','video/c01l02.mp4',20971520,720,NULL,'md5demoL02C01',0,1,NOW(),NOW(),0),
 (3,'课程1-第3节','video/c01l03.mp4',22020096,720,NULL,'md5demoL03C01',0,1,NOW(),NOW(),0),
 (4,'课程1-第4节','video/c01l04.mp4',23068672,720,NULL,'md5demoL04C01',0,1,NOW(),NOW(),0),
 (5,'课程1-第5节','video/c01l05.mp4',24117248,720,NULL,'md5demoL05C01',0,1,NOW(),NOW(),0),
 (6,'课程1-第6节','video/c01l06.mp4',25165824,720,NULL,'md5demoL06C01',0,1,NOW(),NOW(),0),
 (7,'课程1-第7节','video/c01l07.mp4',26214400,720,NULL,'md5demoL07C01',0,1,NOW(),NOW(),0),
 (8,'课程1-第8节','video/c01l08.mp4',27262976,720,NULL,'md5demoL08C01',0,1,NOW(),NOW(),0),
 (9,'课程2-第1节','video/c02l01.mp4',28311552,720,NULL,'md5demoL01C02',0,2,NOW(),NOW(),0),
 (10,'课程2-第2节','video/c02l02.mp4',29360128,720,NULL,'md5demoL02C02',0,2,NOW(),NOW(),0),
 (11,'课程2-第3节','video/c02l03.mp4',30408704,720,NULL,'md5demoL03C02',0,2,NOW(),NOW(),0),
 (12,'课程2-第4节','video/c02l04.mp4',31457280,720,NULL,'md5demoL04C02',0,2,NOW(),NOW(),0),
 (13,'课程2-第5节','video/c02l05.mp4',32505856,720,NULL,'md5demoL05C02',0,2,NOW(),NOW(),0),
 (14,'课程2-第6节','video/c02l06.mp4',33554432,720,NULL,'md5demoL06C02',0,2,NOW(),NOW(),0),
 (15,'课程2-第7节','video/c02l07.mp4',34603008,720,NULL,'md5demoL07C02',0,2,NOW(),NOW(),0),
 (16,'课程2-第8节','video/c02l08.mp4',35651584,720,NULL,'md5demoL08C02',0,2,NOW(),NOW(),0),
 (17,'课程3-第1节','video/c03l01.mp4',36700160,720,NULL,'md5demoL01C03',0,3,NOW(),NOW(),0),
 (18,'课程3-第2节','video/c03l02.mp4',37748736,720,NULL,'md5demoL02C03',0,3,NOW(),NOW(),0),
 (19,'课程3-第3节','video/c03l03.mp4',38797312,720,NULL,'md5demoL03C03',0,3,NOW(),NOW(),0),
 (20,'课程3-第4节','video/c03l04.mp4',39845888,720,NULL,'md5demoL04C03',0,3,NOW(),NOW(),0),
 (21,'课程3-第5节','video/c03l05.mp4',40894464,720,NULL,'md5demoL05C03',0,3,NOW(),NOW(),0),
 (22,'课程3-第6节','video/c03l06.mp4',41943040,720,NULL,'md5demoL06C03',0,3,NOW(),NOW(),0),
 (23,'课程3-第7节','video/c03l07.mp4',42991616,720,NULL,'md5demoL07C03',0,3,NOW(),NOW(),0),
 (24,'课程3-第8节','video/c03l08.mp4',44040192,720,NULL,'md5demoL08C03',0,3,NOW(),NOW(),0),
 (25,'课程4-第1节','video/c04l01.mp4',45088768,720,NULL,'md5demoL01C04',0,4,NOW(),NOW(),0),
 (26,'课程4-第2节','video/c04l02.mp4',46137344,720,NULL,'md5demoL02C04',0,4,NOW(),NOW(),0),
 (27,'课程4-第3节','video/c04l03.mp4',47185920,720,NULL,'md5demoL03C04',0,4,NOW(),NOW(),0),
 (28,'课程4-第4节','video/c04l04.mp4',48234496,720,NULL,'md5demoL04C04',0,4,NOW(),NOW(),0),
 (29,'课程4-第5节','video/c04l05.mp4',49283072,720,NULL,'md5demoL05C04',0,4,NOW(),NOW(),0),
 (30,'课程4-第6节','video/c04l06.mp4',50331648,720,NULL,'md5demoL06C04',0,4,NOW(),NOW(),0),
 (31,'课程4-第7节','video/c04l07.mp4',51380224,720,NULL,'md5demoL07C04',0,4,NOW(),NOW(),0),
 (32,'课程4-第8节','video/c04l08.mp4',52428800,720,NULL,'md5demoL08C04',0,4,NOW(),NOW(),0),
 (33,'课程5-第1节','video/c05l01.mp4',53477376,720,NULL,'md5demoL01C05',0,5,NOW(),NOW(),0),
 (34,'课程5-第2节','video/c05l02.mp4',54525952,720,NULL,'md5demoL02C05',0,5,NOW(),NOW(),0),
 (35,'课程5-第3节','video/c05l03.mp4',55574528,720,NULL,'md5demoL03C05',0,5,NOW(),NOW(),0),
 (36,'课程5-第4节','video/c05l04.mp4',56623104,720,NULL,'md5demoL04C05',0,5,NOW(),NOW(),0),
 (37,'课程5-第5节','video/c05l05.mp4',57671680,720,NULL,'md5demoL05C05',0,5,NOW(),NOW(),0),
 (38,'课程5-第6节','video/c05l06.mp4',58720256,720,NULL,'md5demoL06C05',0,5,NOW(),NOW(),0),
 (39,'课程5-第7节','video/c05l07.mp4',59768832,720,NULL,'md5demoL07C05',0,5,NOW(),NOW(),0),
 (40,'课程5-第8节','video/c05l08.mp4',60817408,720,NULL,'md5demoL08C05',0,5,NOW(),NOW(),0),
 (41,'课程6-第1节','video/c06l01.mp4',61865984,720,NULL,'md5demoL01C06',0,6,NOW(),NOW(),0),
 (42,'课程6-第2节','video/c06l02.mp4',62914560,720,NULL,'md5demoL02C06',0,6,NOW(),NOW(),0),
 (43,'课程6-第3节','video/c06l03.mp4',63963136,720,NULL,'md5demoL03C06',0,6,NOW(),NOW(),0),
 (44,'课程6-第4节','video/c06l04.mp4',65011712,720,NULL,'md5demoL04C06',0,6,NOW(),NOW(),0),
 (45,'课程6-第5节','video/c06l05.mp4',66060288,720,NULL,'md5demoL05C06',0,6,NOW(),NOW(),0),
 (46,'课程6-第6节','video/c06l06.mp4',67108864,720,NULL,'md5demoL06C06',0,6,NOW(),NOW(),0),
 (47,'课程6-第7节','video/c06l07.mp4',68157440,720,NULL,'md5demoL07C06',0,6,NOW(),NOW(),0),
 (48,'课程6-第8节','video/c06l08.mp4',69206016,720,NULL,'md5demoL08C06',0,6,NOW(),NOW(),0),
 (49,'课程7-第1节','video/c07l01.mp4',70254592,720,NULL,'md5demoL01C07',0,7,NOW(),NOW(),0),
 (50,'课程7-第2节','video/c07l02.mp4',71303168,720,NULL,'md5demoL02C07',0,7,NOW(),NOW(),0),
 (51,'课程7-第3节','video/c07l03.mp4',72351744,720,NULL,'md5demoL03C07',0,7,NOW(),NOW(),0),
 (52,'课程7-第4节','video/c07l04.mp4',73400320,720,NULL,'md5demoL04C07',0,7,NOW(),NOW(),0),
 (53,'课程7-第5节','video/c07l05.mp4',74448896,720,NULL,'md5demoL05C07',0,7,NOW(),NOW(),0),
 (54,'课程7-第6节','video/c07l06.mp4',75497472,720,NULL,'md5demoL06C07',0,7,NOW(),NOW(),0),
 (55,'课程7-第7节','video/c07l07.mp4',76546048,720,NULL,'md5demoL07C07',0,7,NOW(),NOW(),0),
 (56,'课程7-第8节','video/c07l08.mp4',77594624,720,NULL,'md5demoL08C07',0,7,NOW(),NOW(),0),
 (57,'课程8-第1节','video/c08l01.mp4',78643200,720,NULL,'md5demoL01C08',0,8,NOW(),NOW(),0),
 (58,'课程8-第2节','video/c08l02.mp4',79691776,720,NULL,'md5demoL02C08',0,8,NOW(),NOW(),0),
 (59,'课程8-第3节','video/c08l03.mp4',80740352,720,NULL,'md5demoL03C08',0,8,NOW(),NOW(),0),
 (60,'课程8-第4节','video/c08l04.mp4',81788928,720,NULL,'md5demoL04C08',0,8,NOW(),NOW(),0),
 (61,'课程8-第5节','video/c08l05.mp4',82837504,720,NULL,'md5demoL05C08',0,8,NOW(),NOW(),0),
 (62,'课程8-第6节','video/c08l06.mp4',83886080,720,NULL,'md5demoL06C08',0,8,NOW(),NOW(),0),
 (63,'课程8-第7节','video/c08l07.mp4',84934656,720,NULL,'md5demoL07C08',0,8,NOW(),NOW(),0),
 (64,'课程8-第8节','video/c08l08.mp4',85983232,720,NULL,'md5demoL08C08',0,8,NOW(),NOW(),0),
 (65,'课程9-第1节','video/c09l01.mp4',87031808,720,NULL,'md5demoL01C09',0,9,NOW(),NOW(),0),
 (66,'课程9-第2节','video/c09l02.mp4',88080384,720,NULL,'md5demoL02C09',0,9,NOW(),NOW(),0),
 (67,'课程9-第3节','video/c09l03.mp4',89128960,720,NULL,'md5demoL03C09',0,9,NOW(),NOW(),0),
 (68,'课程9-第4节','video/c09l04.mp4',90177536,720,NULL,'md5demoL04C09',0,9,NOW(),NOW(),0),
 (69,'课程9-第5节','video/c09l05.mp4',91226112,720,NULL,'md5demoL05C09',0,9,NOW(),NOW(),0),
 (70,'课程9-第6节','video/c09l06.mp4',92274688,720,NULL,'md5demoL06C09',0,9,NOW(),NOW(),0),
 (71,'课程9-第7节','video/c09l07.mp4',93323264,720,NULL,'md5demoL07C09',0,9,NOW(),NOW(),0),
 (72,'课程9-第8节','video/c09l08.mp4',94371840,720,NULL,'md5demoL08C09',0,9,NOW(),NOW(),0),
 (73,'课程10-第1节','video/c10l01.mp4',95420416,720,NULL,'md5demoL01C10',0,10,NOW(),NOW(),0),
 (74,'课程10-第2节','video/c10l02.mp4',96468992,720,NULL,'md5demoL02C10',0,10,NOW(),NOW(),0),
 (75,'课程10-第3节','video/c10l03.mp4',97517568,720,NULL,'md5demoL03C10',0,10,NOW(),NOW(),0),
 (76,'课程10-第4节','video/c10l04.mp4',98566144,720,NULL,'md5demoL04C10',0,10,NOW(),NOW(),0),
 (77,'课程10-第5节','video/c10l05.mp4',99614720,720,NULL,'md5demoL05C10',0,10,NOW(),NOW(),0),
 (78,'课程10-第6节','video/c10l06.mp4',100663296,720,NULL,'md5demoL06C10',0,10,NOW(),NOW(),0),
 (79,'课程10-第7节','video/c10l07.mp4',101711872,720,NULL,'md5demoL07C10',0,10,NOW(),NOW(),0),
 (80,'课程10-第8节','video/c10l08.mp4',102760448,720,NULL,'md5demoL08C10',0,10,NOW(),NOW(),0),
 (81,'课程11-第1节','video/c11l01.mp4',103809024,720,NULL,'md5demoL01C11',0,11,NOW(),NOW(),0),
 (82,'课程11-第2节','video/c11l02.mp4',104857600,720,NULL,'md5demoL02C11',0,11,NOW(),NOW(),0),
 (83,'课程11-第3节','video/c11l03.mp4',105906176,720,NULL,'md5demoL03C11',0,11,NOW(),NOW(),0),
 (84,'课程11-第4节','video/c11l04.mp4',106954752,720,NULL,'md5demoL04C11',0,11,NOW(),NOW(),0),
 (85,'课程11-第5节','video/c11l05.mp4',108003328,720,NULL,'md5demoL05C11',0,11,NOW(),NOW(),0),
 (86,'课程11-第6节','video/c11l06.mp4',109051904,720,NULL,'md5demoL06C11',0,11,NOW(),NOW(),0),
 (87,'课程11-第7节','video/c11l07.mp4',110100480,720,NULL,'md5demoL07C11',0,11,NOW(),NOW(),0),
 (88,'课程11-第8节','video/c11l08.mp4',111149056,720,NULL,'md5demoL08C11',0,11,NOW(),NOW(),0),
 (89,'课程12-第1节','video/c12l01.mp4',112197632,720,NULL,'md5demoL01C12',0,12,NOW(),NOW(),0),
 (90,'课程12-第2节','video/c12l02.mp4',113246208,720,NULL,'md5demoL02C12',0,12,NOW(),NOW(),0),
 (91,'课程12-第3节','video/c12l03.mp4',114294784,720,NULL,'md5demoL03C12',0,12,NOW(),NOW(),0),
 (92,'课程12-第4节','video/c12l04.mp4',115343360,720,NULL,'md5demoL04C12',0,12,NOW(),NOW(),0),
 (93,'课程12-第5节','video/c12l05.mp4',116391936,720,NULL,'md5demoL05C12',0,12,NOW(),NOW(),0),
 (94,'课程12-第6节','video/c12l06.mp4',117440512,720,NULL,'md5demoL06C12',0,12,NOW(),NOW(),0),
 (95,'课程12-第7节','video/c12l07.mp4',118489088,720,NULL,'md5demoL07C12',0,12,NOW(),NOW(),0),
 (96,'课程12-第8节','video/c12l08.mp4',119537664,720,NULL,'md5demoL08C12',0,12,NOW(),NOW(),0);

-- 7. 课时（每门课 8 节，分两章；前 3 节标记为免费试看）
INSERT INTO course_lesson(id,course_id,chapter,lesson_name,video_id,duration,sort,is_free_trial,status,create_time,update_time,is_deleted) VALUES
 (1,1,'第一章 基础入门','第1节 核心知识点1',1,720,1,1,1,NOW(),NOW(),0),
 (2,1,'第一章 基础入门','第2节 核心知识点2',2,720,2,1,1,NOW(),NOW(),0),
 (3,1,'第一章 基础入门','第3节 核心知识点3',3,720,3,1,1,NOW(),NOW(),0),
 (4,1,'第一章 基础入门','第4节 核心知识点4',4,720,4,0,1,NOW(),NOW(),0),
 (5,1,'第二章 进阶实战','第5节 核心知识点5',5,720,5,0,1,NOW(),NOW(),0),
 (6,1,'第二章 进阶实战','第6节 核心知识点6',6,720,6,0,1,NOW(),NOW(),0),
 (7,1,'第二章 进阶实战','第7节 核心知识点7',7,720,7,0,1,NOW(),NOW(),0),
 (8,1,'第二章 进阶实战','第8节 核心知识点8',8,720,8,0,1,NOW(),NOW(),0),
 (9,2,'第一章 基础入门','第1节 核心知识点1',9,720,1,1,1,NOW(),NOW(),0),
 (10,2,'第一章 基础入门','第2节 核心知识点2',10,720,2,1,1,NOW(),NOW(),0),
 (11,2,'第一章 基础入门','第3节 核心知识点3',11,720,3,1,1,NOW(),NOW(),0),
 (12,2,'第一章 基础入门','第4节 核心知识点4',12,720,4,0,1,NOW(),NOW(),0),
 (13,2,'第二章 进阶实战','第5节 核心知识点5',13,720,5,0,1,NOW(),NOW(),0),
 (14,2,'第二章 进阶实战','第6节 核心知识点6',14,720,6,0,1,NOW(),NOW(),0),
 (15,2,'第二章 进阶实战','第7节 核心知识点7',15,720,7,0,1,NOW(),NOW(),0),
 (16,2,'第二章 进阶实战','第8节 核心知识点8',16,720,8,0,1,NOW(),NOW(),0),
 (17,3,'第一章 基础入门','第1节 核心知识点1',17,720,1,1,1,NOW(),NOW(),0),
 (18,3,'第一章 基础入门','第2节 核心知识点2',18,720,2,1,1,NOW(),NOW(),0),
 (19,3,'第一章 基础入门','第3节 核心知识点3',19,720,3,1,1,NOW(),NOW(),0),
 (20,3,'第一章 基础入门','第4节 核心知识点4',20,720,4,0,1,NOW(),NOW(),0),
 (21,3,'第二章 进阶实战','第5节 核心知识点5',21,720,5,0,1,NOW(),NOW(),0),
 (22,3,'第二章 进阶实战','第6节 核心知识点6',22,720,6,0,1,NOW(),NOW(),0),
 (23,3,'第二章 进阶实战','第7节 核心知识点7',23,720,7,0,1,NOW(),NOW(),0),
 (24,3,'第二章 进阶实战','第8节 核心知识点8',24,720,8,0,1,NOW(),NOW(),0),
 (25,4,'第一章 基础入门','第1节 核心知识点1',25,720,1,1,1,NOW(),NOW(),0),
 (26,4,'第一章 基础入门','第2节 核心知识点2',26,720,2,1,1,NOW(),NOW(),0),
 (27,4,'第一章 基础入门','第3节 核心知识点3',27,720,3,1,1,NOW(),NOW(),0),
 (28,4,'第一章 基础入门','第4节 核心知识点4',28,720,4,0,1,NOW(),NOW(),0),
 (29,4,'第二章 进阶实战','第5节 核心知识点5',29,720,5,0,1,NOW(),NOW(),0),
 (30,4,'第二章 进阶实战','第6节 核心知识点6',30,720,6,0,1,NOW(),NOW(),0),
 (31,4,'第二章 进阶实战','第7节 核心知识点7',31,720,7,0,1,NOW(),NOW(),0),
 (32,4,'第二章 进阶实战','第8节 核心知识点8',32,720,8,0,1,NOW(),NOW(),0),
 (33,5,'第一章 基础入门','第1节 核心知识点1',33,720,1,1,1,NOW(),NOW(),0),
 (34,5,'第一章 基础入门','第2节 核心知识点2',34,720,2,1,1,NOW(),NOW(),0),
 (35,5,'第一章 基础入门','第3节 核心知识点3',35,720,3,1,1,NOW(),NOW(),0),
 (36,5,'第一章 基础入门','第4节 核心知识点4',36,720,4,0,1,NOW(),NOW(),0),
 (37,5,'第二章 进阶实战','第5节 核心知识点5',37,720,5,0,1,NOW(),NOW(),0),
 (38,5,'第二章 进阶实战','第6节 核心知识点6',38,720,6,0,1,NOW(),NOW(),0),
 (39,5,'第二章 进阶实战','第7节 核心知识点7',39,720,7,0,1,NOW(),NOW(),0),
 (40,5,'第二章 进阶实战','第8节 核心知识点8',40,720,8,0,1,NOW(),NOW(),0),
 (41,6,'第一章 基础入门','第1节 核心知识点1',41,720,1,1,1,NOW(),NOW(),0),
 (42,6,'第一章 基础入门','第2节 核心知识点2',42,720,2,1,1,NOW(),NOW(),0),
 (43,6,'第一章 基础入门','第3节 核心知识点3',43,720,3,1,1,NOW(),NOW(),0),
 (44,6,'第一章 基础入门','第4节 核心知识点4',44,720,4,0,1,NOW(),NOW(),0),
 (45,6,'第二章 进阶实战','第5节 核心知识点5',45,720,5,0,1,NOW(),NOW(),0),
 (46,6,'第二章 进阶实战','第6节 核心知识点6',46,720,6,0,1,NOW(),NOW(),0),
 (47,6,'第二章 进阶实战','第7节 核心知识点7',47,720,7,0,1,NOW(),NOW(),0),
 (48,6,'第二章 进阶实战','第8节 核心知识点8',48,720,8,0,1,NOW(),NOW(),0),
 (49,7,'第一章 基础入门','第1节 核心知识点1',49,720,1,1,1,NOW(),NOW(),0),
 (50,7,'第一章 基础入门','第2节 核心知识点2',50,720,2,1,1,NOW(),NOW(),0),
 (51,7,'第一章 基础入门','第3节 核心知识点3',51,720,3,1,1,NOW(),NOW(),0),
 (52,7,'第一章 基础入门','第4节 核心知识点4',52,720,4,0,1,NOW(),NOW(),0),
 (53,7,'第二章 进阶实战','第5节 核心知识点5',53,720,5,0,1,NOW(),NOW(),0),
 (54,7,'第二章 进阶实战','第6节 核心知识点6',54,720,6,0,1,NOW(),NOW(),0),
 (55,7,'第二章 进阶实战','第7节 核心知识点7',55,720,7,0,1,NOW(),NOW(),0),
 (56,7,'第二章 进阶实战','第8节 核心知识点8',56,720,8,0,1,NOW(),NOW(),0),
 (57,8,'第一章 基础入门','第1节 核心知识点1',57,720,1,1,1,NOW(),NOW(),0),
 (58,8,'第一章 基础入门','第2节 核心知识点2',58,720,2,1,1,NOW(),NOW(),0),
 (59,8,'第一章 基础入门','第3节 核心知识点3',59,720,3,1,1,NOW(),NOW(),0),
 (60,8,'第一章 基础入门','第4节 核心知识点4',60,720,4,0,1,NOW(),NOW(),0),
 (61,8,'第二章 进阶实战','第5节 核心知识点5',61,720,5,0,1,NOW(),NOW(),0),
 (62,8,'第二章 进阶实战','第6节 核心知识点6',62,720,6,0,1,NOW(),NOW(),0),
 (63,8,'第二章 进阶实战','第7节 核心知识点7',63,720,7,0,1,NOW(),NOW(),0),
 (64,8,'第二章 进阶实战','第8节 核心知识点8',64,720,8,0,1,NOW(),NOW(),0),
 (65,9,'第一章 基础入门','第1节 核心知识点1',65,720,1,1,1,NOW(),NOW(),0),
 (66,9,'第一章 基础入门','第2节 核心知识点2',66,720,2,1,1,NOW(),NOW(),0),
 (67,9,'第一章 基础入门','第3节 核心知识点3',67,720,3,1,1,NOW(),NOW(),0),
 (68,9,'第一章 基础入门','第4节 核心知识点4',68,720,4,0,1,NOW(),NOW(),0),
 (69,9,'第二章 进阶实战','第5节 核心知识点5',69,720,5,0,1,NOW(),NOW(),0),
 (70,9,'第二章 进阶实战','第6节 核心知识点6',70,720,6,0,1,NOW(),NOW(),0),
 (71,9,'第二章 进阶实战','第7节 核心知识点7',71,720,7,0,1,NOW(),NOW(),0),
 (72,9,'第二章 进阶实战','第8节 核心知识点8',72,720,8,0,1,NOW(),NOW(),0),
 (73,10,'第一章 基础入门','第1节 核心知识点1',73,720,1,1,1,NOW(),NOW(),0),
 (74,10,'第一章 基础入门','第2节 核心知识点2',74,720,2,1,1,NOW(),NOW(),0),
 (75,10,'第一章 基础入门','第3节 核心知识点3',75,720,3,1,1,NOW(),NOW(),0),
 (76,10,'第一章 基础入门','第4节 核心知识点4',76,720,4,0,1,NOW(),NOW(),0),
 (77,10,'第二章 进阶实战','第5节 核心知识点5',77,720,5,0,1,NOW(),NOW(),0),
 (78,10,'第二章 进阶实战','第6节 核心知识点6',78,720,6,0,1,NOW(),NOW(),0),
 (79,10,'第二章 进阶实战','第7节 核心知识点7',79,720,7,0,1,NOW(),NOW(),0),
 (80,10,'第二章 进阶实战','第8节 核心知识点8',80,720,8,0,1,NOW(),NOW(),0),
 (81,11,'第一章 基础入门','第1节 核心知识点1',81,720,1,1,1,NOW(),NOW(),0),
 (82,11,'第一章 基础入门','第2节 核心知识点2',82,720,2,1,1,NOW(),NOW(),0),
 (83,11,'第一章 基础入门','第3节 核心知识点3',83,720,3,1,1,NOW(),NOW(),0),
 (84,11,'第一章 基础入门','第4节 核心知识点4',84,720,4,0,1,NOW(),NOW(),0),
 (85,11,'第二章 进阶实战','第5节 核心知识点5',85,720,5,0,1,NOW(),NOW(),0),
 (86,11,'第二章 进阶实战','第6节 核心知识点6',86,720,6,0,1,NOW(),NOW(),0),
 (87,11,'第二章 进阶实战','第7节 核心知识点7',87,720,7,0,1,NOW(),NOW(),0),
 (88,11,'第二章 进阶实战','第8节 核心知识点8',88,720,8,0,1,NOW(),NOW(),0),
 (89,12,'第一章 基础入门','第1节 核心知识点1',89,720,1,1,1,NOW(),NOW(),0),
 (90,12,'第一章 基础入门','第2节 核心知识点2',90,720,2,1,1,NOW(),NOW(),0),
 (91,12,'第一章 基础入门','第3节 核心知识点3',91,720,3,1,1,NOW(),NOW(),0),
 (92,12,'第一章 基础入门','第4节 核心知识点4',92,720,4,0,1,NOW(),NOW(),0),
 (93,12,'第二章 进阶实战','第5节 核心知识点5',93,720,5,0,1,NOW(),NOW(),0),
 (94,12,'第二章 进阶实战','第6节 核心知识点6',94,720,6,0,1,NOW(),NOW(),0),
 (95,12,'第二章 进阶实战','第7节 核心知识点7',95,720,7,0,1,NOW(),NOW(),0),
 (96,12,'第二章 进阶实战','第8节 核心知识点8',96,720,8,0,1,NOW(),NOW(),0);

-- 8. 课件（每门课 1 份讲义）
INSERT INTO course_ware(id,course_id,lesson_id,ware_name,file_path,file_size,file_type,download_count,create_time,update_time,is_deleted) VALUES
 (1,1,0,'课程1 配套讲义PDF','ware/course1.pdf',3145728,'pdf',0,NOW(),NOW(),0),
 (2,2,0,'课程2 配套讲义PDF','ware/course2.pdf',4194304,'pdf',0,NOW(),NOW(),0),
 (3,3,0,'课程3 配套讲义PDF','ware/course3.pdf',5242880,'pdf',0,NOW(),NOW(),0),
 (4,4,0,'课程4 配套讲义PDF','ware/course4.pdf',6291456,'pdf',0,NOW(),NOW(),0),
 (5,5,0,'课程5 配套讲义PDF','ware/course5.pdf',7340032,'pdf',0,NOW(),NOW(),0),
 (6,6,0,'课程6 配套讲义PDF','ware/course6.pdf',8388608,'pdf',0,NOW(),NOW(),0),
 (7,7,0,'课程7 配套讲义PDF','ware/course7.pdf',9437184,'pdf',0,NOW(),NOW(),0),
 (8,8,0,'课程8 配套讲义PDF','ware/course8.pdf',10485760,'pdf',0,NOW(),NOW(),0),
 (9,9,0,'课程9 配套讲义PDF','ware/course9.pdf',11534336,'pdf',0,NOW(),NOW(),0),
 (10,10,0,'课程10 配套讲义PDF','ware/course10.pdf',12582912,'pdf',0,NOW(),NOW(),0),
 (11,11,0,'课程11 配套讲义PDF','ware/course11.pdf',13631488,'pdf',0,NOW(),NOW(),0),
 (12,12,0,'课程12 配套讲义PDF','ware/course12.pdf',14680064,'pdf',0,NOW(),NOW(),0);

-- 9. 随堂测验（每门课 1 套，挂第4节，每套4题）
INSERT INTO quiz(id,course_id,lesson_id,quiz_name,question_count,total_score,pass_score,status,create_time,update_time,is_deleted) VALUES
 (1,1,4,'课程1 阶段小测',4,100,60,1,NOW(),NOW(),0),
 (2,2,12,'课程2 阶段小测',4,100,60,1,NOW(),NOW(),0),
 (3,3,20,'课程3 阶段小测',4,100,60,1,NOW(),NOW(),0),
 (4,4,28,'课程4 阶段小测',4,100,60,1,NOW(),NOW(),0),
 (5,5,36,'课程5 阶段小测',4,100,60,1,NOW(),NOW(),0),
 (6,6,44,'课程6 阶段小测',4,100,60,1,NOW(),NOW(),0),
 (7,7,52,'课程7 阶段小测',4,100,60,1,NOW(),NOW(),0),
 (8,8,60,'课程8 阶段小测',4,100,60,1,NOW(),NOW(),0),
 (9,9,68,'课程9 阶段小测',4,100,60,1,NOW(),NOW(),0),
 (10,10,76,'课程10 阶段小测',4,100,60,1,NOW(),NOW(),0),
 (11,11,84,'课程11 阶段小测',4,100,60,1,NOW(),NOW(),0),
 (12,12,92,'课程12 阶段小测',4,100,60,1,NOW(),NOW(),0);

-- 10. 测验题目（每套 4 题：3 单选 + 1 判断，每题 25 分）
INSERT INTO quiz_question(id,quiz_id,question_type,content,option_a,option_b,option_c,option_d,answer,score,analysis,sort,create_time,is_deleted) VALUES
 (1,1,1,'本课程的核心学习方法是什么？','只看视频不练习','跟着视频敲代码并完成测验作业','跳过基础直接看进阶','只背面试题','B',25,'理论必须结合动手实践',1,NOW(),0),
 (2,1,1,'遇到报错最合理的第一步是？','立刻放弃','复制报错信息阅读并定位','重装系统','无视报错','A',25,'先阅读报错信息、定位堆栈，是排错第一步',2,NOW(),0),
 (3,1,1,'学习进度达到多少会自动生成结业证书？','50%','80%','100%','不需要学习','C',25,'全部课时学完(100%)自动生成证书',3,NOW(),0),
 (4,1,3,'付费课程前3节可以免费试看。','正确','错误',NULL,NULL,'TRUE',25,'平台规则：付费课支持前3节免费试看',4,NOW(),0),
 (5,2,1,'本课程的核心学习方法是什么？','只看视频不练习','跟着视频敲代码并完成测验作业','跳过基础直接看进阶','只背面试题','B',25,'理论必须结合动手实践',1,NOW(),0),
 (6,2,1,'遇到报错最合理的第一步是？','立刻放弃','复制报错信息阅读并定位','重装系统','无视报错','A',25,'先阅读报错信息、定位堆栈，是排错第一步',2,NOW(),0),
 (7,2,1,'学习进度达到多少会自动生成结业证书？','50%','80%','100%','不需要学习','C',25,'全部课时学完(100%)自动生成证书',3,NOW(),0),
 (8,2,3,'付费课程前3节可以免费试看。','正确','错误',NULL,NULL,'TRUE',25,'平台规则：付费课支持前3节免费试看',4,NOW(),0),
 (9,3,1,'本课程的核心学习方法是什么？','只看视频不练习','跟着视频敲代码并完成测验作业','跳过基础直接看进阶','只背面试题','B',25,'理论必须结合动手实践',1,NOW(),0),
 (10,3,1,'遇到报错最合理的第一步是？','立刻放弃','复制报错信息阅读并定位','重装系统','无视报错','A',25,'先阅读报错信息、定位堆栈，是排错第一步',2,NOW(),0),
 (11,3,1,'学习进度达到多少会自动生成结业证书？','50%','80%','100%','不需要学习','C',25,'全部课时学完(100%)自动生成证书',3,NOW(),0),
 (12,3,3,'付费课程前3节可以免费试看。','正确','错误',NULL,NULL,'TRUE',25,'平台规则：付费课支持前3节免费试看',4,NOW(),0),
 (13,4,1,'本课程的核心学习方法是什么？','只看视频不练习','跟着视频敲代码并完成测验作业','跳过基础直接看进阶','只背面试题','B',25,'理论必须结合动手实践',1,NOW(),0),
 (14,4,1,'遇到报错最合理的第一步是？','立刻放弃','复制报错信息阅读并定位','重装系统','无视报错','A',25,'先阅读报错信息、定位堆栈，是排错第一步',2,NOW(),0),
 (15,4,1,'学习进度达到多少会自动生成结业证书？','50%','80%','100%','不需要学习','C',25,'全部课时学完(100%)自动生成证书',3,NOW(),0),
 (16,4,3,'付费课程前3节可以免费试看。','正确','错误',NULL,NULL,'TRUE',25,'平台规则：付费课支持前3节免费试看',4,NOW(),0),
 (17,5,1,'本课程的核心学习方法是什么？','只看视频不练习','跟着视频敲代码并完成测验作业','跳过基础直接看进阶','只背面试题','B',25,'理论必须结合动手实践',1,NOW(),0),
 (18,5,1,'遇到报错最合理的第一步是？','立刻放弃','复制报错信息阅读并定位','重装系统','无视报错','A',25,'先阅读报错信息、定位堆栈，是排错第一步',2,NOW(),0),
 (19,5,1,'学习进度达到多少会自动生成结业证书？','50%','80%','100%','不需要学习','C',25,'全部课时学完(100%)自动生成证书',3,NOW(),0),
 (20,5,3,'付费课程前3节可以免费试看。','正确','错误',NULL,NULL,'TRUE',25,'平台规则：付费课支持前3节免费试看',4,NOW(),0),
 (21,6,1,'本课程的核心学习方法是什么？','只看视频不练习','跟着视频敲代码并完成测验作业','跳过基础直接看进阶','只背面试题','B',25,'理论必须结合动手实践',1,NOW(),0),
 (22,6,1,'遇到报错最合理的第一步是？','立刻放弃','复制报错信息阅读并定位','重装系统','无视报错','A',25,'先阅读报错信息、定位堆栈，是排错第一步',2,NOW(),0),
 (23,6,1,'学习进度达到多少会自动生成结业证书？','50%','80%','100%','不需要学习','C',25,'全部课时学完(100%)自动生成证书',3,NOW(),0),
 (24,6,3,'付费课程前3节可以免费试看。','正确','错误',NULL,NULL,'TRUE',25,'平台规则：付费课支持前3节免费试看',4,NOW(),0),
 (25,7,1,'本课程的核心学习方法是什么？','只看视频不练习','跟着视频敲代码并完成测验作业','跳过基础直接看进阶','只背面试题','B',25,'理论必须结合动手实践',1,NOW(),0),
 (26,7,1,'遇到报错最合理的第一步是？','立刻放弃','复制报错信息阅读并定位','重装系统','无视报错','A',25,'先阅读报错信息、定位堆栈，是排错第一步',2,NOW(),0),
 (27,7,1,'学习进度达到多少会自动生成结业证书？','50%','80%','100%','不需要学习','C',25,'全部课时学完(100%)自动生成证书',3,NOW(),0),
 (28,7,3,'付费课程前3节可以免费试看。','正确','错误',NULL,NULL,'TRUE',25,'平台规则：付费课支持前3节免费试看',4,NOW(),0),
 (29,8,1,'本课程的核心学习方法是什么？','只看视频不练习','跟着视频敲代码并完成测验作业','跳过基础直接看进阶','只背面试题','B',25,'理论必须结合动手实践',1,NOW(),0),
 (30,8,1,'遇到报错最合理的第一步是？','立刻放弃','复制报错信息阅读并定位','重装系统','无视报错','A',25,'先阅读报错信息、定位堆栈，是排错第一步',2,NOW(),0),
 (31,8,1,'学习进度达到多少会自动生成结业证书？','50%','80%','100%','不需要学习','C',25,'全部课时学完(100%)自动生成证书',3,NOW(),0),
 (32,8,3,'付费课程前3节可以免费试看。','正确','错误',NULL,NULL,'TRUE',25,'平台规则：付费课支持前3节免费试看',4,NOW(),0),
 (33,9,1,'本课程的核心学习方法是什么？','只看视频不练习','跟着视频敲代码并完成测验作业','跳过基础直接看进阶','只背面试题','B',25,'理论必须结合动手实践',1,NOW(),0),
 (34,9,1,'遇到报错最合理的第一步是？','立刻放弃','复制报错信息阅读并定位','重装系统','无视报错','A',25,'先阅读报错信息、定位堆栈，是排错第一步',2,NOW(),0),
 (35,9,1,'学习进度达到多少会自动生成结业证书？','50%','80%','100%','不需要学习','C',25,'全部课时学完(100%)自动生成证书',3,NOW(),0),
 (36,9,3,'付费课程前3节可以免费试看。','正确','错误',NULL,NULL,'TRUE',25,'平台规则：付费课支持前3节免费试看',4,NOW(),0),
 (37,10,1,'本课程的核心学习方法是什么？','只看视频不练习','跟着视频敲代码并完成测验作业','跳过基础直接看进阶','只背面试题','B',25,'理论必须结合动手实践',1,NOW(),0),
 (38,10,1,'遇到报错最合理的第一步是？','立刻放弃','复制报错信息阅读并定位','重装系统','无视报错','A',25,'先阅读报错信息、定位堆栈，是排错第一步',2,NOW(),0),
 (39,10,1,'学习进度达到多少会自动生成结业证书？','50%','80%','100%','不需要学习','C',25,'全部课时学完(100%)自动生成证书',3,NOW(),0),
 (40,10,3,'付费课程前3节可以免费试看。','正确','错误',NULL,NULL,'TRUE',25,'平台规则：付费课支持前3节免费试看',4,NOW(),0),
 (41,11,1,'本课程的核心学习方法是什么？','只看视频不练习','跟着视频敲代码并完成测验作业','跳过基础直接看进阶','只背面试题','B',25,'理论必须结合动手实践',1,NOW(),0),
 (42,11,1,'遇到报错最合理的第一步是？','立刻放弃','复制报错信息阅读并定位','重装系统','无视报错','A',25,'先阅读报错信息、定位堆栈，是排错第一步',2,NOW(),0),
 (43,11,1,'学习进度达到多少会自动生成结业证书？','50%','80%','100%','不需要学习','C',25,'全部课时学完(100%)自动生成证书',3,NOW(),0),
 (44,11,3,'付费课程前3节可以免费试看。','正确','错误',NULL,NULL,'TRUE',25,'平台规则：付费课支持前3节免费试看',4,NOW(),0),
 (45,12,1,'本课程的核心学习方法是什么？','只看视频不练习','跟着视频敲代码并完成测验作业','跳过基础直接看进阶','只背面试题','B',25,'理论必须结合动手实践',1,NOW(),0),
 (46,12,1,'遇到报错最合理的第一步是？','立刻放弃','复制报错信息阅读并定位','重装系统','无视报错','A',25,'先阅读报错信息、定位堆栈，是排错第一步',2,NOW(),0),
 (47,12,1,'学习进度达到多少会自动生成结业证书？','50%','80%','100%','不需要学习','C',25,'全部课时学完(100%)自动生成证书',3,NOW(),0),
 (48,12,3,'付费课程前3节可以免费试看。','正确','错误',NULL,NULL,'TRUE',25,'平台规则：付费课支持前3节免费试看',4,NOW(),0);

-- 11. 作业（每门课 1 次，挂第8节）
INSERT INTO assignment(id,course_id,lesson_id,teacher_id,title,content,attachment,total_score,deadline,create_time,update_time,is_deleted) VALUES
 (1,1,8,2,'课程1 结业作业','结合本课程所学，完成一个综合实践并提交总结，要求步骤完整、截图清晰。',NULL,100,DATE_ADD(NOW(), INTERVAL 30 DAY),NOW(),NOW(),0),
 (2,2,16,3,'课程2 结业作业','结合本课程所学，完成一个综合实践并提交总结，要求步骤完整、截图清晰。',NULL,100,DATE_ADD(NOW(), INTERVAL 30 DAY),NOW(),NOW(),0),
 (3,3,24,3,'课程3 结业作业','结合本课程所学，完成一个综合实践并提交总结，要求步骤完整、截图清晰。',NULL,100,DATE_ADD(NOW(), INTERVAL 30 DAY),NOW(),NOW(),0),
 (4,4,32,4,'课程4 结业作业','结合本课程所学，完成一个综合实践并提交总结，要求步骤完整、截图清晰。',NULL,100,DATE_ADD(NOW(), INTERVAL 30 DAY),NOW(),NOW(),0),
 (5,5,40,4,'课程5 结业作业','结合本课程所学，完成一个综合实践并提交总结，要求步骤完整、截图清晰。',NULL,100,DATE_ADD(NOW(), INTERVAL 30 DAY),NOW(),NOW(),0),
 (6,6,48,5,'课程6 结业作业','结合本课程所学，完成一个综合实践并提交总结，要求步骤完整、截图清晰。',NULL,100,DATE_ADD(NOW(), INTERVAL 30 DAY),NOW(),NOW(),0),
 (7,7,56,5,'课程7 结业作业','结合本课程所学，完成一个综合实践并提交总结，要求步骤完整、截图清晰。',NULL,100,DATE_ADD(NOW(), INTERVAL 30 DAY),NOW(),NOW(),0),
 (8,8,64,6,'课程8 结业作业','结合本课程所学，完成一个综合实践并提交总结，要求步骤完整、截图清晰。',NULL,100,DATE_ADD(NOW(), INTERVAL 30 DAY),NOW(),NOW(),0),
 (9,9,72,6,'课程9 结业作业','结合本课程所学，完成一个综合实践并提交总结，要求步骤完整、截图清晰。',NULL,100,DATE_ADD(NOW(), INTERVAL 30 DAY),NOW(),NOW(),0),
 (10,10,80,2,'课程10 结业作业','结合本课程所学，完成一个综合实践并提交总结，要求步骤完整、截图清晰。',NULL,100,DATE_ADD(NOW(), INTERVAL 30 DAY),NOW(),NOW(),0),
 (11,11,88,4,'课程11 结业作业','结合本课程所学，完成一个综合实践并提交总结，要求步骤完整、截图清晰。',NULL,100,DATE_ADD(NOW(), INTERVAL 30 DAY),NOW(),NOW(),0),
 (12,12,96,5,'课程12 结业作业','结合本课程所学，完成一个综合实践并提交总结，要求步骤完整、截图清晰。',NULL,100,DATE_ADD(NOW(), INTERVAL 30 DAY),NOW(),NOW(),0);

-- 12. 优惠券（满减/立减/折扣 三种）
INSERT INTO coupon(id,coupon_name,coupon_type,threshold,discount_value,total_count,receive_count,valid_days,status,create_time,update_time,is_deleted) VALUES
 (1,'满100减20新人券',1,100.00,20.00,1000,0,30,1,NOW(),NOW(),0),
 (2,'无门槛立减10元券',2,0.00,10.00,500,0,15,1,NOW(),NOW(),0),
 (3,'全场85折券',3,0.00,8.50,300,0,7,1,NOW(),NOW(),0);

-- 13. 课程订单（演示已购关系；免费课无需订单）
INSERT INTO course_order(id,order_no,user_id,course_id,original_amount,discount_amount,pay_amount,coupon_id,pay_type,status,pay_time,create_time,update_time,is_deleted) VALUES
 (1,'XY20260901000001',7,1,199,0.00,199,NULL,1,1,DATE_SUB(NOW(),INTERVAL 1 DAY),NOW(),NOW(),0),
 (2,'XY20260901000002',7,2,159,0.00,159,NULL,1,1,DATE_SUB(NOW(),INTERVAL 2 DAY),NOW(),NOW(),0),
 (3,'XY20260901000003',7,5,299,0.00,299,NULL,1,1,DATE_SUB(NOW(),INTERVAL 3 DAY),NOW(),NOW(),0),
 (4,'XY20260901000004',8,1,199,0.00,199,NULL,1,1,DATE_SUB(NOW(),INTERVAL 4 DAY),NOW(),NOW(),0),
 (5,'XY20260901000005',8,6,129,0.00,129,NULL,1,1,DATE_SUB(NOW(),INTERVAL 5 DAY),NOW(),NOW(),0),
 (6,'XY20260901000006',9,2,159,0.00,159,NULL,1,1,DATE_SUB(NOW(),INTERVAL 6 DAY),NOW(),NOW(),0),
 (7,'XY20260901000007',9,8,139,0.00,139,NULL,1,1,DATE_SUB(NOW(),INTERVAL 7 DAY),NOW(),NOW(),0),
 (8,'XY20260901000008',10,5,299,0.00,299,NULL,1,1,DATE_SUB(NOW(),INTERVAL 8 DAY),NOW(),NOW(),0),
 (9,'XY20260901000009',10,7,259,0.00,259,NULL,1,1,DATE_SUB(NOW(),INTERVAL 9 DAY),NOW(),NOW(),0),
 (10,'XY20260901000010',10,11,199,0.00,199,NULL,1,1,DATE_SUB(NOW(),INTERVAL 10 DAY),NOW(),NOW(),0),
 (11,'XY20260901000011',11,1,199,0.00,199,NULL,1,1,DATE_SUB(NOW(),INTERVAL 11 DAY),NOW(),NOW(),0),
 (12,'XY20260901000012',11,12,99,0.00,99,NULL,1,1,DATE_SUB(NOW(),INTERVAL 12 DAY),NOW(),NOW(),0),
 (13,'XY20260901000013',12,2,159,0.00,159,NULL,1,1,DATE_SUB(NOW(),INTERVAL 13 DAY),NOW(),NOW(),0),
 (14,'XY20260901000014',13,1,199,0.00,199,NULL,1,1,DATE_SUB(NOW(),INTERVAL 14 DAY),NOW(),NOW(),0),
 (15,'XY20260901000015',14,8,139,0.00,139,NULL,1,1,DATE_SUB(NOW(),INTERVAL 15 DAY),NOW(),NOW(),0),
 (16,'XY20260901000016',14,11,199,0.00,199,NULL,1,1,DATE_SUB(NOW(),INTERVAL 16 DAY),NOW(),NOW(),0);

-- 14. 部分学员领取优惠券
INSERT INTO user_coupon(id,user_id,coupon_id,status,order_id,receive_time,expire_time,use_time) VALUES
 (1,7,1,0,NULL,NOW(),DATE_ADD(NOW(),INTERVAL 30 DAY),NULL),
 (2,8,1,0,NULL,NOW(),DATE_ADD(NOW(),INTERVAL 30 DAY),NULL),
 (3,9,2,0,NULL,NOW(),DATE_ADD(NOW(),INTERVAL 30 DAY),NULL),
 (4,10,3,0,NULL,NOW(),DATE_ADD(NOW(),INTERVAL 30 DAY),NULL),
 (5,11,2,0,NULL,NOW(),DATE_ADD(NOW(),INTERVAL 30 DAY),NULL),
 (6,12,1,0,NULL,NOW(),DATE_ADD(NOW(),INTERVAL 30 DAY),NULL),
 (7,13,3,0,NULL,NOW(),DATE_ADD(NOW(),INTERVAL 30 DAY),NULL);

-- 15. 学习进度（已完成课程全部100%并生成证书；学习中课程只学到部分课时）
INSERT INTO learn_progress(id,user_id,course_id,lesson_id,progress,position_seconds,learned_seconds,status,finish_time,last_learn_time,create_time,update_time,is_deleted) VALUES
 (0,7,1,1,100,720,720,1,DATE_SUB(NOW(),INTERVAL 1 HOUR),DATE_SUB(NOW(),INTERVAL 2 HOUR),NOW(),NOW(),0),
 (0,7,1,2,100,720,720,1,DATE_SUB(NOW(),INTERVAL 1 HOUR),DATE_SUB(NOW(),INTERVAL 2 HOUR),NOW(),NOW(),0),
 (0,7,1,3,100,720,720,1,DATE_SUB(NOW(),INTERVAL 1 HOUR),DATE_SUB(NOW(),INTERVAL 2 HOUR),NOW(),NOW(),0),
 (0,7,1,4,100,720,720,1,DATE_SUB(NOW(),INTERVAL 1 HOUR),DATE_SUB(NOW(),INTERVAL 2 HOUR),NOW(),NOW(),0),
 (0,7,1,5,100,720,720,1,DATE_SUB(NOW(),INTERVAL 1 HOUR),DATE_SUB(NOW(),INTERVAL 2 HOUR),NOW(),NOW(),0),
 (0,7,1,6,100,720,720,1,DATE_SUB(NOW(),INTERVAL 1 HOUR),DATE_SUB(NOW(),INTERVAL 2 HOUR),NOW(),NOW(),0),
 (0,7,1,7,100,720,720,1,DATE_SUB(NOW(),INTERVAL 1 HOUR),DATE_SUB(NOW(),INTERVAL 2 HOUR),NOW(),NOW(),0),
 (0,7,1,8,100,720,720,1,DATE_SUB(NOW(),INTERVAL 1 HOUR),DATE_SUB(NOW(),INTERVAL 2 HOUR),NOW(),NOW(),0),
 (0,8,1,1,100,720,720,1,DATE_SUB(NOW(),INTERVAL 1 HOUR),DATE_SUB(NOW(),INTERVAL 2 HOUR),NOW(),NOW(),0),
 (0,8,1,2,100,720,720,1,DATE_SUB(NOW(),INTERVAL 1 HOUR),DATE_SUB(NOW(),INTERVAL 2 HOUR),NOW(),NOW(),0),
 (0,8,1,3,100,720,720,1,DATE_SUB(NOW(),INTERVAL 1 HOUR),DATE_SUB(NOW(),INTERVAL 2 HOUR),NOW(),NOW(),0),
 (0,8,1,4,100,720,720,1,DATE_SUB(NOW(),INTERVAL 1 HOUR),DATE_SUB(NOW(),INTERVAL 2 HOUR),NOW(),NOW(),0),
 (0,8,1,5,100,720,720,1,DATE_SUB(NOW(),INTERVAL 1 HOUR),DATE_SUB(NOW(),INTERVAL 2 HOUR),NOW(),NOW(),0),
 (0,8,1,6,100,720,720,1,DATE_SUB(NOW(),INTERVAL 1 HOUR),DATE_SUB(NOW(),INTERVAL 2 HOUR),NOW(),NOW(),0),
 (0,8,1,7,100,720,720,1,DATE_SUB(NOW(),INTERVAL 1 HOUR),DATE_SUB(NOW(),INTERVAL 2 HOUR),NOW(),NOW(),0),
 (0,8,1,8,100,720,720,1,DATE_SUB(NOW(),INTERVAL 1 HOUR),DATE_SUB(NOW(),INTERVAL 2 HOUR),NOW(),NOW(),0),
 (0,9,2,9,100,720,720,1,DATE_SUB(NOW(),INTERVAL 1 HOUR),DATE_SUB(NOW(),INTERVAL 2 HOUR),NOW(),NOW(),0),
 (0,9,2,10,100,720,720,1,DATE_SUB(NOW(),INTERVAL 1 HOUR),DATE_SUB(NOW(),INTERVAL 2 HOUR),NOW(),NOW(),0),
 (0,9,2,11,100,720,720,1,DATE_SUB(NOW(),INTERVAL 1 HOUR),DATE_SUB(NOW(),INTERVAL 2 HOUR),NOW(),NOW(),0),
 (0,9,2,12,100,720,720,1,DATE_SUB(NOW(),INTERVAL 1 HOUR),DATE_SUB(NOW(),INTERVAL 2 HOUR),NOW(),NOW(),0),
 (0,9,2,13,100,720,720,1,DATE_SUB(NOW(),INTERVAL 1 HOUR),DATE_SUB(NOW(),INTERVAL 2 HOUR),NOW(),NOW(),0),
 (0,9,2,14,100,720,720,1,DATE_SUB(NOW(),INTERVAL 1 HOUR),DATE_SUB(NOW(),INTERVAL 2 HOUR),NOW(),NOW(),0),
 (0,9,2,15,100,720,720,1,DATE_SUB(NOW(),INTERVAL 1 HOUR),DATE_SUB(NOW(),INTERVAL 2 HOUR),NOW(),NOW(),0),
 (0,9,2,16,100,720,720,1,DATE_SUB(NOW(),INTERVAL 1 HOUR),DATE_SUB(NOW(),INTERVAL 2 HOUR),NOW(),NOW(),0),
 (0,8,4,25,100,720,720,1,DATE_SUB(NOW(),INTERVAL 1 HOUR),DATE_SUB(NOW(),INTERVAL 2 HOUR),NOW(),NOW(),0),
 (0,8,4,26,100,720,720,1,DATE_SUB(NOW(),INTERVAL 1 HOUR),DATE_SUB(NOW(),INTERVAL 2 HOUR),NOW(),NOW(),0),
 (0,8,4,27,100,720,720,1,DATE_SUB(NOW(),INTERVAL 1 HOUR),DATE_SUB(NOW(),INTERVAL 2 HOUR),NOW(),NOW(),0),
 (0,8,4,28,100,720,720,1,DATE_SUB(NOW(),INTERVAL 1 HOUR),DATE_SUB(NOW(),INTERVAL 2 HOUR),NOW(),NOW(),0),
 (0,8,4,29,100,720,720,1,DATE_SUB(NOW(),INTERVAL 1 HOUR),DATE_SUB(NOW(),INTERVAL 2 HOUR),NOW(),NOW(),0),
 (0,8,4,30,100,720,720,1,DATE_SUB(NOW(),INTERVAL 1 HOUR),DATE_SUB(NOW(),INTERVAL 2 HOUR),NOW(),NOW(),0),
 (0,8,4,31,100,720,720,1,DATE_SUB(NOW(),INTERVAL 1 HOUR),DATE_SUB(NOW(),INTERVAL 2 HOUR),NOW(),NOW(),0),
 (0,8,4,32,100,720,720,1,DATE_SUB(NOW(),INTERVAL 1 HOUR),DATE_SUB(NOW(),INTERVAL 2 HOUR),NOW(),NOW(),0),
 (0,7,2,9,100,720,720,1,DATE_SUB(NOW(),INTERVAL 3 HOUR),DATE_SUB(NOW(),INTERVAL 5 HOUR),NOW(),NOW(),0),
 (0,7,2,10,100,720,720,1,DATE_SUB(NOW(),INTERVAL 3 HOUR),DATE_SUB(NOW(),INTERVAL 5 HOUR),NOW(),NOW(),0),
 (0,7,2,11,45,324,324,0,NULL,NOW(),NOW(),NOW(),0),
 (0,10,5,33,100,720,720,1,DATE_SUB(NOW(),INTERVAL 3 HOUR),DATE_SUB(NOW(),INTERVAL 5 HOUR),NOW(),NOW(),0),
 (0,10,5,34,100,720,720,1,DATE_SUB(NOW(),INTERVAL 3 HOUR),DATE_SUB(NOW(),INTERVAL 5 HOUR),NOW(),NOW(),0),
 (0,10,5,35,45,324,324,0,NULL,NOW(),NOW(),NOW(),0),
 (0,7,3,17,100,720,720,1,DATE_SUB(NOW(),INTERVAL 3 HOUR),DATE_SUB(NOW(),INTERVAL 5 HOUR),NOW(),NOW(),0),
 (0,7,3,18,100,720,720,1,DATE_SUB(NOW(),INTERVAL 3 HOUR),DATE_SUB(NOW(),INTERVAL 5 HOUR),NOW(),NOW(),0),
 (0,7,3,19,45,324,324,0,NULL,NOW(),NOW(),NOW(),0);

-- 16. 结业证书（与上方已完成课程对应）
INSERT INTO certificate(id,cert_no,user_id,course_id,certificate_name,issue_time,status,create_time,is_deleted) VALUES
 (1,'XY2026090001',7,1,'《Java零基础到SpringBoot全栈实战》结业证书',DATE_SUB(NOW(),INTERVAL 1 HOUR),1,NOW(),0),
 (2,'XY2026090002',8,1,'《Java零基础到SpringBoot全栈实战》结业证书',DATE_SUB(NOW(),INTERVAL 1 HOUR),1,NOW(),0),
 (3,'XY2026090003',9,2,'《Vue3 + Element Plus 前端实战》结业证书',DATE_SUB(NOW(),INTERVAL 1 HOUR),1,NOW(),0),
 (4,'XY2026090004',8,4,'《Python人工智能入门》结业证书',DATE_SUB(NOW(),INTERVAL 1 HOUR),1,NOW(),0);

-- 17. 播放打卡流水（示例）
INSERT INTO play_record(id,user_id,course_id,lesson_id,position_sec,device,create_time) VALUES
 (0,8,1,1,1*90,'Chrome/Windows',NOW()),
 (0,9,1,2,2*90,'Chrome/Windows',NOW()),
 (0,10,1,3,3*90,'Chrome/Windows',NOW()),
 (0,11,1,4,4*90,'Chrome/Windows',NOW()),
 (0,12,1,5,5*90,'Chrome/Windows',NOW()),
 (0,7,1,6,6*90,'Chrome/Windows',NOW()),
 (0,8,1,7,7*90,'Chrome/Windows',NOW()),
 (0,9,1,8,8*90,'Chrome/Windows',NOW());

-- 18. 作业提交与批改（部分待批、部分已批）
INSERT INTO assignment_submission(id,user_id,course_id,assignment_id,content,attachment,submit_time,grade_status,score,teacher_comment,teacher_id,grade_time,update_time,is_deleted) VALUES
 (1,7,1,1,'已完成课程1综合实践，详见文档总结。',NULL,DATE_SUB(NOW(),INTERVAL 2 DAY),1,92,'完成度高，继续保持。',2,DATE_SUB(NOW(),INTERVAL 1 DAY),NOW(),0),
 (2,8,1,1,'课程1作业提交，包含截图与代码。',NULL,DATE_SUB(NOW(),INTERVAL 2 DAY),1,88,'思路清晰，注意命名规范。',2,DATE_SUB(NOW(),INTERVAL 1 DAY),NOW(),0),
 (3,9,2,2,'课程2前端页面实战作业。',NULL,DATE_SUB(NOW(),INTERVAL 2 DAY),0,NULL,NULL,NULL,NULL,NOW(),0),
 (4,10,5,5,'机器学习小项目作业。',NULL,DATE_SUB(NOW(),INTERVAL 2 DAY),1,85,'模型评估部分可再深入。',4,DATE_SUB(NOW(),INTERVAL 1 DAY),NOW(),0),
 (5,7,3,3,'小程序入门练习作业。',NULL,DATE_SUB(NOW(),INTERVAL 2 DAY),0,NULL,NULL,NULL,NULL,NOW(),0);

-- 19. 课程评论（每门课 2 条）
INSERT INTO course_comment(id,course_id,user_id,parent_id,content,score,status,create_time,is_deleted) VALUES
 (1,1,8,0,'老师讲得非常清晰，零基础也能跟上，强烈推荐！',5,1,NOW(),0),
 (2,1,9,0,'课程内容很扎实，配套资料齐全，性价比高。',4,1,NOW(),0),
 (3,2,10,0,'老师讲得非常清晰，零基础也能跟上，强烈推荐！',5,1,NOW(),0),
 (4,2,11,0,'课程内容很扎实，配套资料齐全，性价比高。',4,1,NOW(),0),
 (5,3,12,0,'老师讲得非常清晰，零基础也能跟上，强烈推荐！',5,1,NOW(),0),
 (6,3,13,0,'课程内容很扎实，配套资料齐全，性价比高。',4,1,NOW(),0),
 (7,4,14,0,'老师讲得非常清晰，零基础也能跟上，强烈推荐！',5,1,NOW(),0),
 (8,4,15,0,'课程内容很扎实，配套资料齐全，性价比高。',4,1,NOW(),0),
 (9,5,16,0,'老师讲得非常清晰，零基础也能跟上，强烈推荐！',5,1,NOW(),0),
 (10,5,17,0,'课程内容很扎实，配套资料齐全，性价比高。',4,1,NOW(),0),
 (11,6,18,0,'老师讲得非常清晰，零基础也能跟上，强烈推荐！',5,1,NOW(),0),
 (12,6,19,0,'课程内容很扎实，配套资料齐全，性价比高。',4,1,NOW(),0),
 (13,7,20,0,'老师讲得非常清晰，零基础也能跟上，强烈推荐！',5,1,NOW(),0),
 (14,7,21,0,'课程内容很扎实，配套资料齐全，性价比高。',4,1,NOW(),0),
 (15,8,22,0,'老师讲得非常清晰，零基础也能跟上，强烈推荐！',5,1,NOW(),0),
 (16,8,23,0,'课程内容很扎实，配套资料齐全，性价比高。',4,1,NOW(),0),
 (17,9,24,0,'老师讲得非常清晰，零基础也能跟上，强烈推荐！',5,1,NOW(),0),
 (18,9,25,0,'课程内容很扎实，配套资料齐全，性价比高。',4,1,NOW(),0),
 (19,10,26,0,'老师讲得非常清晰，零基础也能跟上，强烈推荐！',5,1,NOW(),0),
 (20,10,7,0,'课程内容很扎实，配套资料齐全，性价比高。',4,1,NOW(),0),
 (21,11,8,0,'老师讲得非常清晰，零基础也能跟上，强烈推荐！',5,1,NOW(),0),
 (22,11,9,0,'课程内容很扎实，配套资料齐全，性价比高。',4,1,NOW(),0),
 (23,12,10,0,'老师讲得非常清晰，零基础也能跟上，强烈推荐！',5,1,NOW(),0),
 (24,12,11,0,'课程内容很扎实，配套资料齐全，性价比高。',4,1,NOW(),0);

-- 20. 课程问答（一半已回复，一半待回复）
INSERT INTO course_qa(id,course_id,lesson_id,user_id,question,reply_user_id,reply_content,reply_time,status,create_time,update_time,is_deleted) VALUES
 (1,1,0,8,'这里没太听懂，能再讲讲吗？',NULL,NULL,NULL,0,NOW(),NOW(),0),
 (2,2,0,9,'请问第？',3,'建议结合随堂测验与课件，把每个知识点动手实践一遍。',DATE_SUB(NOW(),INTERVAL 1 DAY),1,NOW(),NOW(),0),
 (3,3,0,10,'这里没太听懂，能再讲讲吗？',NULL,NULL,NULL,0,NOW(),NOW(),0),
 (4,4,0,11,'请问第？',4,'建议结合随堂测验与课件，把每个知识点动手实践一遍。',DATE_SUB(NOW(),INTERVAL 1 DAY),1,NOW(),NOW(),0),
 (5,5,0,12,'这里没太听懂，能再讲讲吗？',NULL,NULL,NULL,0,NOW(),NOW(),0),
 (6,6,0,13,'请问第？',5,'建议结合随堂测验与课件，把每个知识点动手实践一遍。',DATE_SUB(NOW(),INTERVAL 1 DAY),1,NOW(),NOW(),0),
 (7,7,0,14,'这里没太听懂，能再讲讲吗？',NULL,NULL,NULL,0,NOW(),NOW(),0),
 (8,8,0,15,'请问第？',6,'建议结合随堂测验与课件，把每个知识点动手实践一遍。',DATE_SUB(NOW(),INTERVAL 1 DAY),1,NOW(),NOW(),0),
 (9,9,0,16,'这里没太听懂，能再讲讲吗？',NULL,NULL,NULL,0,NOW(),NOW(),0),
 (10,10,0,17,'请问第？',2,'建议结合随堂测验与课件，把每个知识点动手实践一遍。',DATE_SUB(NOW(),INTERVAL 1 DAY),1,NOW(),NOW(),0),
 (11,11,0,18,'这里没太听懂，能再讲讲吗？',NULL,NULL,NULL,0,NOW(),NOW(),0),
 (12,12,0,19,'请问第？',5,'建议结合随堂测验与课件，把每个知识点动手实践一遍。',DATE_SUB(NOW(),INTERVAL 1 DAY),1,NOW(),NOW(),0);

-- 21. 课程收藏
INSERT INTO course_favorite(id,user_id,course_id,create_time) VALUES
 (1,7,2,NOW()),
 (2,7,5,NOW()),
 (3,8,2,NOW()),
 (4,9,1,NOW()),
 (5,10,1,NOW()),
 (6,11,4,NOW()),
 (7,12,3,NOW());

-- 22. 平台公告
INSERT INTO announcement(id,title,content,type,status,publisher_id,publish_time,create_time,update_time,is_deleted) VALUES
 (1,'欢迎来到星云在线教育平台','平台Slogan：知识点亮星云。新用户注册即可免费学习全部免费课程，付费课程支持前3节免费试看。',1,1,1,NOW(),NOW(),NOW(),0),
 (2,'关于结业证书自动生成的说明','当你完成某门课程全部课时（学习进度100%）后，系统将自动生成电子结业证书，可在「学习档案-我的证书」中查看下载。',1,1,1,NOW(),NOW(),NOW(),0),
 (3,'新人优惠券发放通知','本月新注册学员可在个人中心领取新人优惠券，购买付费课程时自动抵扣，数量有限先到先得。',2,1,1,NOW(),NOW(),NOW(),0),
 (4,'文明学习与评论规范','请大家在课程评论与问答区文明交流，违规内容将被屏蔽，共同营造良好学习氛围。',1,1,1,NOW(),NOW(),NOW(),0);

SET FOREIGN_KEY_CHECKS=1;
SELECT CONCAT('初始数据导入完成：用户',(SELECT COUNT(*) FROM sys_user),' 课程',(SELECT COUNT(*) FROM course),' 课时',(SELECT COUNT(*) FROM course_lesson)) AS result_msg;