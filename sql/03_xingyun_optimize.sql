-- =====================================================================
-- XingYun 星云在线教育平台 · v1.2 性能优化索引脚本（增量）
-- 适用对象：已按 01_xingyun_schema.sql 建库、已导入 02_xingyun_data.sql 的存量数据库
-- 用法：在 MySQL 客户端执行本脚本即可（可重复执行，已存在索引会自动跳过报错可忽略）
-- 说明：01_xingyun_schema.sql 已同步写入以下索引，新库无需执行本脚本
-- =====================================================================
USE xingyun;

-- 1. 管理端大盘「近7天新增用户」按 create_time 分组统计，加速范围过滤
ALTER TABLE sys_user ADD INDEX idx_create_time (create_time);

-- 2. 首页/课程列表「热门排序」：审核通过+已上架 按 student_count 降序
--    联合索引让 过滤 + 排序 一次索引完成，避免 filesort
ALTER TABLE course ADD INDEX idx_hot (audit_status, status, student_count);

-- 3. 学习进度上报链路按 user+lesson 定位播放流水，加速行为统计
ALTER TABLE play_record ADD INDEX idx_user_lesson (user_id, lesson_id);

-- 4. 讲师待批作业列表按讲师ID过滤，加速「我的待办」
ALTER TABLE assignment ADD INDEX idx_teacher (teacher_id);
