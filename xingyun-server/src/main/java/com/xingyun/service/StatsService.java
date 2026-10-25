package com.xingyun.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xingyun.common.BusinessException;
import com.xingyun.common.Result;
import com.xingyun.entity.*;
import com.xingyun.mapper.*;
import com.xingyun.vo.DashboardVO;
import com.xingyun.vo.TeacherStatVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 统计服务：管理端数据大盘、讲师个人数据统计。
 */
@Service
public class StatsService {

    @Resource
    private SysUserMapper userMapper;
    @Resource
    private CourseMapper courseMapper;
    @Resource
    private CourseOrderMapper orderMapper;
    @Resource
    private CourseCategoryMapper categoryMapper;
    @Resource
    private AssignmentService assignmentService;
    @Resource
    private QaService qaService;
    @Resource
    private LearnProgressMapper progressMapper;
    @Resource
    private CourseLessonMapper lessonMapper;

    /** 管理端大盘数据 */
    public DashboardVO dashboard() {
        DashboardVO vo = new DashboardVO();
        vo.setStudentCount(userMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUserType, 1)));
        vo.setTeacherCount(userMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUserType, 2)));
        vo.setCourseCount(courseMapper.selectCount(null));
        vo.setPendingCount(courseMapper.selectCount(new LambdaQueryWrapper<Course>().eq(Course::getAuditStatus, 1)));
        // 【性能优化】订单数/营收/学习时长改为 SQL 聚合下推数据库，避免全量加载内存求和
        Map<String, Object> orderAgg = orderMapper.selectMaps(new QueryWrapper<CourseOrder>()
                .select("COUNT(*) AS cnt", "COALESCE(SUM(pay_amount), 0) AS amount")
                .eq("status", 1)).stream().findFirst().orElse(new HashMap<>());
        vo.setOrderCount(orderAgg.get("cnt") == null ? 0L : Long.parseLong(String.valueOf(orderAgg.get("cnt"))));
        String revenue = orderAgg.get("amount") == null ? "0.00" : String.valueOf(orderAgg.get("amount"));
        vo.setTotalRevenue(new BigDecimal(revenue).setScale(2, RoundingMode.HALF_UP).toPlainString());
        // 累计学习时长（分钟）：一次性 SQL 求和，不再全表拉学员
        Map<String, Object> minuteAgg = userMapper.selectMaps(new QueryWrapper<SysUser>()
                .select("COALESCE(SUM(study_minutes), 0) AS total")
                .eq("user_type", 1)).stream().findFirst().orElse(new HashMap<>());
        long minutes = minuteAgg.get("total") == null ? 0L : Long.parseLong(String.valueOf(minuteAgg.get("total")));
        vo.setTotalStudyMinutes(minutes);

        // 各一级分类课程数量分布（用于饼图）
        QueryWrapper<Course> qw = new QueryWrapper<>();
        qw.select("category_id AS cid", "COUNT(*) AS cnt").groupBy("category_id");
        List<Map<String, Object>> dist = new ArrayList<>();
        for (Map<String, Object> row : courseMapper.selectMaps(qw)) {
            Long cid = Long.valueOf(String.valueOf(row.get("cid")));
            CourseCategory c = categoryMapper.selectById(cid);
            Map<String, Object> m = new HashMap<>();
            m.put("name", c == null ? "未分类" : c.getName());
            m.put("value", Long.valueOf(String.valueOf(row.get("cnt"))));
            dist.add(m);
        }
        vo.setCategoryDist(dist);

        // 近 7 天每日新增用户（用于折线图）
        QueryWrapper<SysUser> uw = new QueryWrapper<>();
        uw.select("DATE(create_time) AS day", "COUNT(*) AS cnt")
                .ge("create_time", new Date(System.currentTimeMillis() - 7L * 24 * 3600 * 1000))
                .groupBy("DATE(create_time)").orderByAsc("day");
        List<Map<String, Object>> week = new ArrayList<>();
        for (Map<String, Object> row : userMapper.selectMaps(uw)) {
            Map<String, Object> m = new HashMap<>();
            m.put("date", String.valueOf(row.get("day")));
            m.put("count", Long.valueOf(String.valueOf(row.get("cnt"))));
            week.add(m);
        }
        vo.setWeekNewUsers(week);
        return vo;
    }

    /** 讲师个人统计 */
    public TeacherStatVO teacherStat(Long teacherId) {
        TeacherStatVO vo = new TeacherStatVO();
        List<Course> courses = courseMapper.selectList(new LambdaQueryWrapper<Course>()
                .eq(Course::getTeacherId, teacherId));
        vo.setMyCourseCount((long) courses.size());
        vo.setTotalStudent(courses.stream().mapToLong(c -> c.getStudentCount() == null ? 0 : c.getStudentCount()).sum());
        vo.setTotalView(courses.stream().mapToLong(c -> c.getViewCount() == null ? 0 : c.getViewCount()).sum());
        vo.setPendingGrade(assignmentService.pendingCount(teacherId));
        vo.setPendingQa(qaService.pendingCount(teacherId));
        Set<Long> courseIds = courses.stream().map(Course::getId).collect(Collectors.toSet());
        BigDecimal income = BigDecimal.ZERO;
        if (!courseIds.isEmpty()) {
            Map<String, Object> incomeAgg = orderMapper.selectMaps(new QueryWrapper<CourseOrder>()
                    .select("COALESCE(SUM(pay_amount), 0) AS amount")
                    .in("course_id", courseIds).eq("status", 1))
                    .stream().findFirst().orElse(new HashMap<>());
            if (incomeAgg.get("amount") != null) {
                income = new BigDecimal(String.valueOf(incomeAgg.get("amount")));
            }
        }
        vo.setTotalIncome(income.setScale(2, RoundingMode.HALF_UP).toPlainString());
        return vo;
    }

    /**
     * 讲师查看某门课程下「报名/学习过」的学员及其学习进度。
     * 学员来源 = 已支付订单的学员 ∪ 产生过学习进度的学员（兼容免费课无订单的情况）。
     * 安全：先校验该课程确实属于当前讲师，防止讲师通过改 courseId 偷看他人课程（数据级越权防护）。
     *
     * @param courseId  课程ID
     * @param teacherId 当前登录讲师ID
     * @return 每个学员一条：昵称头像、总课时、已完成课时、整体进度、累计学习分钟、最近学习时间
     */
    public List<Map<String, Object>> courseStudents(Long courseId, Long teacherId) {
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }
        // 数据级越权校验：讲师只能看自己课程的学员
        if (course.getTeacherId() == null || !course.getTeacherId().equals(teacherId)) {
            throw new BusinessException(Result.CODE_FORBIDDEN, "无权查看该课程学员");
        }

        // 该课程启用的课时总数（status=1），作为进度分母
        long totalLessons = lessonMapper.selectCount(new LambdaQueryWrapper<CourseLesson>()
                .eq(CourseLesson::getCourseId, courseId)
                .eq(CourseLesson::getStatus, 1));

        // 1) 已支付订单的学员
        Set<Long> userIds = new LinkedHashSet<>();
        Map<Long, Date> enrollTimeMap = new HashMap<>();
        List<CourseOrder> paidOrders = orderMapper.selectList(new LambdaQueryWrapper<CourseOrder>()
                .eq(CourseOrder::getCourseId, courseId).eq(CourseOrder::getStatus, 1));
        for (CourseOrder o : paidOrders) {
            userIds.add(o.getUserId());
            if (o.getPayTime() != null) {
                enrollTimeMap.put(o.getUserId(), o.getPayTime());
            }
        }

        // 2) 该课程下全部学习进度，并按学员分组
        List<LearnProgress> progresses = progressMapper.selectList(new LambdaQueryWrapper<LearnProgress>()
                .eq(LearnProgress::getCourseId, courseId));
        Map<Long, List<LearnProgress>> byUser = progresses.stream()
                .collect(Collectors.groupingBy(LearnProgress::getUserId));
        userIds.addAll(byUser.keySet());

        List<Map<String, Object>> list = new ArrayList<>();
        for (Long uid : userIds) {
            SysUser u = userMapper.selectById(uid);
            // 只统计学员账号，排除讲师/管理员自己的测试痕迹
            if (u == null || u.getUserType() == null || u.getUserType() != 1) {
                continue;
            }
            List<LearnProgress> ps = byUser.getOrDefault(uid, Collections.emptyList());
            long finished = ps.stream().filter(p -> p.getStatus() != null && p.getStatus() == 1).count();
            int learnedSeconds = ps.stream().mapToInt(p -> p.getLearnedSeconds() == null ? 0 : p.getLearnedSeconds()).sum();
            Optional<Date> lastOpt = ps.stream().map(LearnProgress::getLastLearnTime)
                    .filter(Objects::nonNull).max(Date::compareTo);

            Map<String, Object> m = new LinkedHashMap<>();
            m.put("userId", uid);
            m.put("nickname", u.getNickname());
            m.put("avatar", u.getAvatar());
            m.put("totalLessons", totalLessons);
            m.put("finishedLessons", finished);
            // 整体进度按「已完成课时 / 总课时」计算，四舍五入为整数百分比
            m.put("progress", totalLessons == 0 ? 0 : (int) Math.round(finished * 100.0 / totalLessons));
            m.put("learnedMinutes", learnedSeconds / 60);
            m.put("lastLearnTime", lastOpt.orElse(null));
            m.put("enrollTime", enrollTimeMap.get(uid));
            // 是否学完：全部课时均完成
            m.put("finishedAll", totalLessons > 0 && finished >= totalLessons);
            list.add(m);
        }
        // 最近学习的学员排在前面，没学过的按报名时间排后
        list.sort((a, b) -> {
            Date da = (Date) a.get("lastLearnTime");
            Date db = (Date) b.get("lastLearnTime");
            if (da == null && db == null) return 0;
            if (da == null) return 1;
            if (db == null) return -1;
            return db.compareTo(da);
        });
        return list;
    }
}
