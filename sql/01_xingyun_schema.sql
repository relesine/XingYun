-- =====================================================================
-- XingYun 星云在线教育平台 —— 数据库建表脚本
-- 数据库：MySQL 8.0（phpStudy Pro 集成）  引擎：InnoDB  字符集：utf8mb4
-- 文件作用：创建数据库 + 全部 24 张核心数据表
-- 规范说明：
--   1. 所有表统一带通用字段 create_time / update_time / is_deleted（逻辑删除 0 正常 1 删除）
--   2. 表与表之间使用「逻辑外键」（只建字段+索引，不建物理外键约束），
--      好处：新手删除数据不会被外键卡住、分库分表更容易，企业开发普遍这样做
--   3. 主键统一 BIGINT 自增；金额用 DECIMAL，禁止用浮点避免精度丢失
--   4. 重复执行不会报错：先 DROP 再 CREATE（注意会清空旧数据）
-- =====================================================================

-- 1) 若已有旧库则删除并按 utf8mb4 重建（utf8mb4 才能存 emoji 和生僻字）
DROP DATABASE IF EXISTS xingyun;
CREATE DATABASE xingyun DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE xingyun;

-- =====================================================================
-- 一、系统权限域：用户、角色、用户角色关联（RBAC 底座）
-- =====================================================================

-- 1. 用户表：学员 / 讲师 / 平台管理员统一账号，用 user_type 区分
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
  id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  username        VARCHAR(50)  NOT NULL COMMENT '登录账号',
  password        VARCHAR(100) NOT NULL COMMENT '登录密码（BCrypt 加密存储）',
  nickname        VARCHAR(50)  NULL COMMENT '昵称',
  real_name       VARCHAR(50)  NULL COMMENT '真实姓名',
  gender          TINYINT      NOT NULL DEFAULT 0 COMMENT '性别 0未知 1男 2女',
  avatar          VARCHAR(255) NULL COMMENT '头像URL',
  email           VARCHAR(100) NULL COMMENT '邮箱',
  phone           VARCHAR(20)  NULL COMMENT '手机号',
  user_type       TINYINT      NOT NULL DEFAULT 1 COMMENT '账号类型 1学员 2讲师 3管理员',
  title           VARCHAR(100) NULL COMMENT '讲师头衔（如：资深Java讲师）',
  intro           VARCHAR(500) NULL COMMENT '讲师/个人简介',
  study_minutes   INT          NOT NULL DEFAULT 0 COMMENT '累计学习时长（分钟）',
  integral        INT          NOT NULL DEFAULT 0 COMMENT '学习积分',
  status          TINYINT      NOT NULL DEFAULT 0 COMMENT '账号状态 0正常 1禁用',
  last_login_time DATETIME     NULL COMMENT '最近登录时间',
  create_by       BIGINT       NULL COMMENT '创建人ID',
  update_by       BIGINT       NULL COMMENT '更新人ID',
  create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1已删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_username (username),
  KEY idx_user_type (user_type),
  KEY idx_status (status),
  KEY idx_create_time (create_time) COMMENT 'v1.2：大盘近7天新增用户统计'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表（学员/讲师/管理员）';

-- 2. 角色表：RBAC（基于角色的访问控制）三种固定角色
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
  id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  role_code   VARCHAR(50) NOT NULL COMMENT '角色编码 STUDENT/TEACHER/ADMIN',
  role_name   VARCHAR(50) NOT NULL COMMENT '角色名称',
  description VARCHAR(200) NULL COMMENT '角色描述',
  create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted  TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 3. 用户-角色 关联表（一个用户可拥有多个角色，多对多）
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
  id          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  user_id     BIGINT   NOT NULL COMMENT '用户ID',
  role_id     BIGINT   NOT NULL COMMENT '角色ID',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_role (user_id, role_id),
  KEY idx_role (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- =====================================================================
-- 二、课程内容域：分类、课程、课时、视频、课件
-- =====================================================================

-- 4. 课程分类表（一级/二级，parent_id=0 为一级分类，自关联实现两级树）
DROP TABLE IF EXISTS course_category;
CREATE TABLE course_category (
  id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  name        VARCHAR(50) NOT NULL COMMENT '分类名称',
  parent_id   BIGINT      NOT NULL DEFAULT 0 COMMENT '父分类ID，0表示一级分类',
  level       TINYINT     NOT NULL DEFAULT 1 COMMENT '层级 1一级 2二级',
  icon        VARCHAR(255) NULL COMMENT '分类图标URL',
  sort        INT         NOT NULL DEFAULT 0 COMMENT '排序号，越小越靠前',
  status      TINYINT     NOT NULL DEFAULT 0 COMMENT '状态 0启用 1停用',
  create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted  TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  KEY idx_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程分类表（两级自关联）';

-- 5. 课程表
DROP TABLE IF EXISTS course;
CREATE TABLE course (
  id               BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  course_name      VARCHAR(150)  NOT NULL COMMENT '课程名称',
  cover            VARCHAR(255)  NULL COMMENT '课程封面URL',
  category_id      BIGINT        NOT NULL COMMENT '一级分类ID',
  category2_id     BIGINT        NOT NULL DEFAULT 0 COMMENT '二级分类ID',
  teacher_id       BIGINT        NOT NULL COMMENT '授课讲师ID（逻辑外键 sys_user.id）',
  intro            VARCHAR(500)  NULL COMMENT '课程简介（一句话卖点）',
  description      TEXT          NULL COMMENT '课程详情（富文本）',
  price            DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '销售价格，0表示免费',
  original_price   DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '划线原价',
  is_free          TINYINT       NOT NULL DEFAULT 1 COMMENT '是否免费 1免费 0付费',
  difficulty       TINYINT       NOT NULL DEFAULT 1 COMMENT '难度 1入门 2进阶 3高级',
  lesson_count     INT           NOT NULL DEFAULT 0 COMMENT '课时总数（冗余，列表页免统计）',
  total_duration   INT           NOT NULL DEFAULT 0 COMMENT '总时长（秒）',
  student_count    INT           NOT NULL DEFAULT 0 COMMENT '学习人数（去重统计）',
  view_count       INT           NOT NULL DEFAULT 0 COMMENT '浏览量',
  score            DECIMAL(3,1) NOT NULL DEFAULT 5.0 COMMENT '课程评分（满分5）',
  tags             VARCHAR(200)  NULL COMMENT '标签，逗号分隔',
  is_recommend     TINYINT       NOT NULL DEFAULT 0 COMMENT '是否推荐 0否 1是',
  audit_status     TINYINT       NOT NULL DEFAULT 2 COMMENT '审核状态 0草稿 1待审核 2通过 3驳回',
  reject_reason    VARCHAR(255)  NULL COMMENT '驳回原因',
  status           TINYINT       NOT NULL DEFAULT 1 COMMENT '上下架 0下架 1上架',
  create_by        BIGINT        NULL COMMENT '创建人',
  update_by        BIGINT        NULL COMMENT '更新人',
  create_time      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted       TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  KEY idx_teacher (teacher_id),
  KEY idx_category (category_id, category2_id),
  KEY idx_status (audit_status, status),
  KEY idx_hot (audit_status, status, student_count) COMMENT 'v1.2：热门课程排序'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';

-- 6. 课时表（一章多节课，按 sort 排序）
DROP TABLE IF EXISTS course_lesson;
CREATE TABLE course_lesson (
  id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  course_id    BIGINT       NOT NULL COMMENT '所属课程ID',
  chapter      VARCHAR(100) NOT NULL COMMENT '章节名称',
  lesson_name  VARCHAR(150) NOT NULL COMMENT '课时名称',
  video_id     BIGINT       NULL COMMENT '关联视频资源ID',
  duration     INT          NOT NULL DEFAULT 0 COMMENT '时长（秒）',
  sort         INT          NOT NULL DEFAULT 0 COMMENT '课时排序号',
  is_free_trial TINYINT     NOT NULL DEFAULT 0 COMMENT '是否免费试看 1是 0否（付费课前3节后端强制开放）',
  status       TINYINT       NOT NULL DEFAULT 1 COMMENT '状态 0禁用 1正常',
  create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted   TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  KEY idx_course_sort (course_id, sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程课时表';

-- 7. 视频资源表（独立管理，做防盗链与播放量统计）
DROP TABLE IF EXISTS video_resource;
CREATE TABLE video_resource (
  id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  video_name  VARCHAR(150) NOT NULL COMMENT '视频名称',
  file_path   VARCHAR(255) NOT NULL COMMENT '服务器存储相对路径（不直接暴露给前端）',
  file_size   BIGINT       NOT NULL DEFAULT 0 COMMENT '文件大小（字节）',
  duration    INT          NOT NULL DEFAULT 0 COMMENT '视频时长（秒）',
  cover       VARCHAR(255) NULL COMMENT '视频封面',
  md5         VARCHAR(64)  NULL COMMENT '文件MD5（用于秒传去重）',
  play_count  INT          NOT NULL DEFAULT 0 COMMENT '播放次数',
  create_by   BIGINT       NULL COMMENT '上传人',
  create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted  TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  KEY idx_md5 (md5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频资源表';

-- 8. 课件表（PPT/PDF/源码等，挂在课时下）
DROP TABLE IF EXISTS course_ware;
CREATE TABLE course_ware (
  id             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  course_id      BIGINT       NOT NULL COMMENT '课程ID',
  lesson_id      BIGINT       NOT NULL DEFAULT 0 COMMENT '课时ID，0表示课程级资料',
  ware_name      VARCHAR(150) NOT NULL COMMENT '课件名称',
  file_path      VARCHAR(255) NOT NULL COMMENT '文件路径',
  file_size      BIGINT       NOT NULL DEFAULT 0 COMMENT '文件大小（字节）',
  file_type      VARCHAR(20)  NULL COMMENT '文件后缀类型',
  download_count INT          NOT NULL DEFAULT 0 COMMENT '下载次数',
  create_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  KEY idx_course (course_id, lesson_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程课件表';

-- =====================================================================
-- 三、学习过程域：进度、播放记录、测验、题目、答题、作业、错题本
-- =====================================================================

-- 9. 学习进度表（每用户每课时唯一一条，自动记录+续播）
DROP TABLE IF EXISTS learn_progress;
CREATE TABLE learn_progress (
  id               BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  user_id          BIGINT   NOT NULL COMMENT '学员ID',
  course_id        BIGINT   NOT NULL COMMENT '课程ID',
  lesson_id        BIGINT   NOT NULL COMMENT '课时ID',
  progress         INT      NOT NULL DEFAULT 0 COMMENT '播放进度百分比 0-100',
  position_seconds INT      NOT NULL DEFAULT 0 COMMENT '上次播放位置（秒），用于断点续播',
  learned_seconds  INT      NOT NULL DEFAULT 0 COMMENT '累计学习秒数',
  status           TINYINT  NOT NULL DEFAULT 0 COMMENT '状态 0学习中 1已完成',
  finish_time      DATETIME NULL COMMENT '完成时间',
  last_learn_time  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最近学习时间',
  create_time      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted       TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_lesson (user_id, lesson_id),
  KEY idx_user_course (user_id, course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习进度表';

-- 10. 播放/打卡流水表（每次播放产生一条，用于学习行为分析）
DROP TABLE IF EXISTS play_record;
CREATE TABLE play_record (
  id             BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  user_id        BIGINT   NOT NULL COMMENT '学员ID',
  course_id      BIGINT   NOT NULL COMMENT '课程ID',
  lesson_id      BIGINT   NOT NULL COMMENT '课时ID',
  position_sec   INT      NOT NULL DEFAULT 0 COMMENT '本次上报位置（秒）',
  device         VARCHAR(50) NULL COMMENT '播放设备',
  create_time    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_user_time (user_id, create_time),
  KEY idx_lesson (lesson_id),
  KEY idx_user_lesson (user_id, lesson_id) COMMENT 'v1.2：学习行为统计'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='播放打卡流水表';

-- 11. 随堂测验表（挂在课时下）
DROP TABLE IF EXISTS quiz;
CREATE TABLE quiz (
  id             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  course_id      BIGINT       NOT NULL COMMENT '课程ID',
  lesson_id      BIGINT       NOT NULL COMMENT '课时ID',
  quiz_name      VARCHAR(150) NOT NULL COMMENT '测验名称',
  question_count INT          NOT NULL DEFAULT 0 COMMENT '题目数量',
  total_score    INT          NOT NULL DEFAULT 100 COMMENT '总分',
  pass_score     INT          NOT NULL DEFAULT 60 COMMENT '及格分',
  status         TINYINT      NOT NULL DEFAULT 1 COMMENT '状态 0禁用 1启用',
  create_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  KEY idx_lesson (lesson_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='随堂测验表';

-- 12. 测验题目表
DROP TABLE IF EXISTS quiz_question;
CREATE TABLE quiz_question (
  id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  quiz_id     BIGINT       NOT NULL COMMENT '所属测验ID',
  question_type TINYINT    NOT NULL DEFAULT 1 COMMENT '题型 1单选 2多选 3判断',
  content     VARCHAR(500) NOT NULL COMMENT '题干',
  option_a    VARCHAR(255) NULL COMMENT '选项A',
  option_b    VARCHAR(255) NULL COMMENT '选项B',
  option_c    VARCHAR(255) NULL COMMENT '选项C',
  option_d    VARCHAR(255) NULL COMMENT '选项D',
  answer      VARCHAR(20)  NOT NULL COMMENT '正确答案（如 A / ABD / TRUE）',
  score       INT          NOT NULL DEFAULT 10 COMMENT '本题分值',
  analysis    VARCHAR(500) NULL COMMENT '答案解析',
  sort        INT          NOT NULL DEFAULT 0 COMMENT '排序',
  create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  is_deleted  TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  KEY idx_quiz (quiz_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测验题目表';

-- 13. 答题记录表（学员每题一条作答流水）
DROP TABLE IF EXISTS quiz_answer;
CREATE TABLE quiz_answer (
  id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  quiz_id     BIGINT      NOT NULL COMMENT '测验ID',
  user_id     BIGINT      NOT NULL COMMENT '学员ID',
  question_id BIGINT      NOT NULL COMMENT '题目ID',
  user_answer VARCHAR(20) NULL COMMENT '学员答案',
  is_right    TINYINT     NOT NULL DEFAULT 0 COMMENT '是否正确 0错 1对',
  score       INT         NOT NULL DEFAULT 0 COMMENT '本题得分',
  create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_user_quiz (user_id, quiz_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='答题记录表';

-- 14. 错题本表（错题自动归集，支持重做）
DROP TABLE IF EXISTS wrong_question;
CREATE TABLE wrong_question (
  id              BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  user_id         BIGINT   NOT NULL COMMENT '学员ID',
  quiz_id         BIGINT   NOT NULL COMMENT '测验ID',
  question_id     BIGINT   NOT NULL COMMENT '题目ID',
  wrong_count     INT      NOT NULL DEFAULT 1 COMMENT '错误次数',
  last_wrong_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最近错误时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_question (user_id, question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='错题本表';

-- 15. 作业表（讲师布置）
DROP TABLE IF EXISTS assignment;
CREATE TABLE assignment (
  id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  course_id   BIGINT       NOT NULL COMMENT '课程ID',
  lesson_id   BIGINT       NOT NULL DEFAULT 0 COMMENT '课时ID',
  teacher_id  BIGINT       NOT NULL COMMENT '布置讲师ID',
  title       VARCHAR(150) NOT NULL COMMENT '作业标题',
  content     TEXT         NULL COMMENT '作业要求',
  attachment  VARCHAR(255) NULL COMMENT '作业附件',
  total_score INT          NOT NULL DEFAULT 100 COMMENT '满分',
  deadline    DATETIME     NULL COMMENT '截止时间',
  create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted  TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  KEY idx_course (course_id),
  KEY idx_teacher (teacher_id) COMMENT 'v1.2：讲师待批作业统计'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作业表';

-- 16. 作业提交/批改表（学员提交，讲师批改；状态流转 待批->已批/打回）
DROP TABLE IF EXISTS assignment_submission;
CREATE TABLE assignment_submission (
  id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  assignment_id   BIGINT       NOT NULL COMMENT '作业ID',
  course_id       BIGINT       NOT NULL COMMENT '课程ID（冗余便于查询）',
  user_id         BIGINT       NOT NULL COMMENT '学员ID',
  content         TEXT         NULL COMMENT '提交内容',
  attachment      VARCHAR(255) NULL COMMENT '提交附件',
  submit_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  grade_status    TINYINT      NOT NULL DEFAULT 0 COMMENT '批改状态 0待批改 1已批改 2已打回',
  score           INT          NULL COMMENT '得分',
  teacher_comment VARCHAR(500) NULL COMMENT '讲师评语',
  teacher_id      BIGINT       NULL COMMENT '批改讲师ID',
  grade_time      DATETIME     NULL COMMENT '批改时间',
  update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_assignment_user (assignment_id, user_id),
  KEY idx_user (user_id),
  KEY idx_status (grade_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作业提交批改表';

-- =====================================================================
-- 四、交易运营域：订单、优惠券、用户券、评论、问答、证书、公告、收藏
-- =====================================================================

-- 17. 课程订单表（订单号唯一，防止重复支付）
DROP TABLE IF EXISTS course_order;
CREATE TABLE course_order (
  id               BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  order_no         VARCHAR(32)   NOT NULL COMMENT '订单编号（业务唯一）',
  user_id          BIGINT        NOT NULL COMMENT '下单学员ID',
  course_id        BIGINT        NOT NULL COMMENT '课程ID',
  original_amount  DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '订单原始金额',
  discount_amount  DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '优惠金额',
  pay_amount       DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '实付金额',
  coupon_id        BIGINT        NULL COMMENT '使用的用户优惠券ID',
  pay_type         TINYINT       NOT NULL DEFAULT 1 COMMENT '支付方式 1微信 2支付宝',
  status           TINYINT       NOT NULL DEFAULT 0 COMMENT '状态 0待支付 1已支付 2已取消 3已退款',
  pay_time         DATETIME      NULL COMMENT '支付时间',
  create_time      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted       TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_order_no (order_no),
  KEY idx_user_status (user_id, status),
  KEY idx_course (course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程订单表';

-- 18. 优惠券表
DROP TABLE IF EXISTS coupon;
CREATE TABLE coupon (
  id             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  coupon_name    VARCHAR(100)  NOT NULL COMMENT '优惠券名称',
  coupon_type    TINYINT       NOT NULL DEFAULT 1 COMMENT '类型 1满减 2立减 3折扣',
  threshold      DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '使用门槛（满多少元）',
  discount_value DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '优惠值（满减/立减为金额，折扣为如8.5表示85折）',
  total_count    INT           NOT NULL DEFAULT 0 COMMENT '发行总量，0为不限量',
  receive_count  INT           NOT NULL DEFAULT 0 COMMENT '已领取数量',
  valid_days     INT           NOT NULL DEFAULT 30 COMMENT '领取后有效天数',
  status         TINYINT       NOT NULL DEFAULT 1 COMMENT '状态 0停用 1启用',
  create_time    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted     TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券表';

-- 19. 用户优惠券表（领取记录，同一用户同一券限领1张）
DROP TABLE IF EXISTS user_coupon;
CREATE TABLE user_coupon (
  id           BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  user_id      BIGINT   NOT NULL COMMENT '学员ID',
  coupon_id    BIGINT   NOT NULL COMMENT '优惠券ID',
  status       TINYINT  NOT NULL DEFAULT 0 COMMENT '状态 0未使用 1已使用 2已过期',
  order_id     BIGINT   NULL COMMENT '核销订单ID',
  receive_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
  expire_time  DATETIME NULL COMMENT '失效时间',
  use_time     DATETIME NULL COMMENT '使用时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_coupon (user_id, coupon_id),
  KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户优惠券表';

-- 20. 课程评论表（parent_id 支持回复，评分1-5星）
DROP TABLE IF EXISTS course_comment;
CREATE TABLE course_comment (
  id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  course_id   BIGINT       NOT NULL COMMENT '课程ID',
  user_id     BIGINT       NOT NULL COMMENT '评论人ID',
  parent_id   BIGINT       NOT NULL DEFAULT 0 COMMENT '父评论ID，0为顶级评论',
  content     VARCHAR(1000) NOT NULL COMMENT '评论内容',
  score       TINYINT      NOT NULL DEFAULT 5 COMMENT '星级评分1-5',
  status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态 0待审核 1显示 2屏蔽',
  create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  is_deleted  TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  KEY idx_course (course_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程评论表';

-- 21. 课程问答表（学员提问，讲师/管理员回复）
DROP TABLE IF EXISTS course_qa;
CREATE TABLE course_qa (
  id             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  course_id      BIGINT       NOT NULL COMMENT '课程ID',
  lesson_id      BIGINT       NOT NULL DEFAULT 0 COMMENT '课时ID',
  user_id        BIGINT       NOT NULL COMMENT '提问学员ID',
  question       VARCHAR(1000) NOT NULL COMMENT '问题内容',
  reply_user_id  BIGINT       NULL COMMENT '回复人ID',
  reply_content  VARCHAR(1000) NULL COMMENT '回复内容',
  reply_time     DATETIME     NULL COMMENT '回复时间',
  status         TINYINT      NOT NULL DEFAULT 0 COMMENT '状态 0待回复 1已回复',
  create_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  KEY idx_course_status (course_id, status),
  KEY idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程问答表';

-- 22. 证书表（学完100%自动生成，证书号唯一）
DROP TABLE IF EXISTS certificate;
CREATE TABLE certificate (
  id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  cert_no          VARCHAR(40)  NOT NULL COMMENT '证书编号（唯一）',
  user_id          BIGINT       NOT NULL COMMENT '学员ID',
  course_id        BIGINT       NOT NULL COMMENT '课程ID',
  certificate_name VARCHAR(150) NOT NULL COMMENT '证书名称',
  issue_time       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '颁发时间',
  status           TINYINT      NOT NULL DEFAULT 1 COMMENT '状态 0作废 1有效',
  create_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  is_deleted       TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_cert_no (cert_no),
  UNIQUE KEY uk_user_course (user_id, course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习证书表';

-- 23. 公告表
DROP TABLE IF EXISTS announcement;
CREATE TABLE announcement (
  id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  title        VARCHAR(150) NOT NULL COMMENT '公告标题',
  content      TEXT         NOT NULL COMMENT '公告内容',
  type         TINYINT      NOT NULL DEFAULT 1 COMMENT '类型 1系统公告 2课程通知',
  status       TINYINT      NOT NULL DEFAULT 1 COMMENT '状态 0草稿 1已发布',
  publisher_id BIGINT       NULL COMMENT '发布人ID',
  publish_time DATETIME     NULL COMMENT '发布时间',
  create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted   TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  KEY idx_status_time (status, publish_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台公告表';

-- 24. 课程收藏表
DROP TABLE IF EXISTS course_favorite;
CREATE TABLE course_favorite (
  id          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  user_id     BIGINT   NOT NULL COMMENT '学员ID',
  course_id   BIGINT   NOT NULL COMMENT '课程ID',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_course (user_id, course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程收藏表';

-- 建表完成提示
SELECT CONCAT('XingYun 数据库与全部 ',
  (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='xingyun'),
  ' 张表创建完成！') AS result_msg;
