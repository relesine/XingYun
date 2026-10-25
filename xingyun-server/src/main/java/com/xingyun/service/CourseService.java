package com.xingyun.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xingyun.common.BusinessException;
import com.xingyun.common.Constants;
import com.xingyun.common.PageResult;
import com.xingyun.dto.CourseDTO;
import com.xingyun.dto.CourseQueryDTO;
import com.xingyun.entity.*;
import com.xingyun.mapper.*;
import com.xingyun.security.LoginUser;
import com.xingyun.security.UserContext;
import com.xingyun.vo.ChapterVO;
import com.xingyun.vo.CourseDetailVO;
import com.xingyun.vo.CourseVO;
import com.xingyun.vo.LessonVO;
import com.xingyun.vo.MyCourseVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 课程服务（平台核心域）：课程检索、详情与章节目录、试看/购买权限、课程维护与审核。
 *
 * @author xingyun
 */
@Service
public class CourseService {

    @Resource
    private CourseMapper courseMapper;
    @Resource
    private CourseLessonMapper lessonMapper;
    @Resource
    private CourseCategoryMapper categoryMapper;
    @Resource
    private SysUserMapper userMapper;
    @Resource
    private CourseOrderMapper orderMapper;
    @Resource
    private LearnProgressMapper progressMapper;

    /** 学员端/公开课程分页：仅展示审核通过且上架的课程 */
    public PageResult<CourseVO> page(CourseQueryDTO query) {
        LambdaQueryWrapper<Course> wrapper = buildQueryWrapper(query);
        // 公开列表只看审核通过(2)、上架(1)
        wrapper.eq(Course::getAuditStatus, 2).eq(Course::getStatus, 1);
        applySort(wrapper, query.getSort());
        Page<Course> page = courseMapper.selectPage(new Page<>(query.getPage(), query.getSize()), wrapper);
        List<CourseVO> voList = fillCourseVO(page.getRecords());
        PageResult<CourseVO> result = new PageResult<>();
        result.setRecords(voList);
        result.setTotal(page.getTotal());
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        return result;
    }

    /** 讲师端：我的课程分页（包含各种审核状态） */
    public PageResult<CourseVO> teacherPage(CourseQueryDTO query, Long teacherId) {
        query.setTeacherId(teacherId);
        LambdaQueryWrapper<Course> wrapper = buildQueryWrapper(query);
        wrapper.orderByDesc(Course::getId);
        Page<Course> page = courseMapper.selectPage(new Page<>(query.getPage(), query.getSize()), wrapper);
        return toVOPage(page);
    }

    /** 管理端：课程分页（可按审核状态筛） */
    public PageResult<CourseVO> adminPage(CourseQueryDTO query) {
        LambdaQueryWrapper<Course> wrapper = buildQueryWrapper(query);
        if (query.getAuditStatus() != null) {
            wrapper.eq(Course::getAuditStatus, query.getAuditStatus());
        }
        wrapper.orderByDesc(Course::getId);
        Page<Course> page = courseMapper.selectPage(new Page<>(query.getPage(), query.getSize()), wrapper);
        return toVOPage(page);
    }

    /** 热门/推荐课程（首页用） */
    public List<CourseVO> hot(int limit) {
        List<Course> list = courseMapper.selectList(new LambdaQueryWrapper<Course>()
                .eq(Course::getAuditStatus, 2).eq(Course::getStatus, 1)
                .orderByDesc(Course::getStudentCount).last("limit " + limit));
        return fillCourseVO(list);
    }

    /**
     * 课程详情：课程信息 + 讲师 + 分类 + 章节目录（含每节是否可播放）+ 当前用户学习状态。
     */
    public CourseDetailVO detail(Long courseId) {
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException("课程不存在或已下架");
        }
        Long userId = UserContext.getUserId();
        CourseDetailVO vo = new CourseDetailVO();
        BeanUtils.copyProperties(course, vo);

        SysUser teacher = userMapper.selectById(course.getTeacherId());
        if (teacher != null) {
            vo.setTeacherName(teacher.getNickname());
            vo.setTeacherTitle(teacher.getTitle());
            vo.setTeacherAvatar(teacher.getAvatar());
            vo.setTeacherIntro(teacher.getIntro());
        }
        CourseCategory c1 = categoryMapper.selectById(course.getCategoryId());
        if (c1 != null) {
            vo.setCategoryName(c1.getName());
        }
        if (course.getCategory2Id() != null && course.getCategory2Id() != 0L) {
            CourseCategory c2 = categoryMapper.selectById(course.getCategory2Id());
            if (c2 != null) {
                vo.setCategory2Name(c2.getName());
            }
        }

        boolean purchased = hasPurchased(userId, course);
        vo.setPurchased(purchased);

        List<CourseLesson> lessons = lessonMapper.selectList(new LambdaQueryWrapper<CourseLesson>()
                .eq(CourseLesson::getCourseId, courseId)
                .eq(CourseLesson::getStatus, 1)
                .orderByAsc(CourseLesson::getSort));
        Map<Long, LearnProgress> progressMap = loadProgressMap(userId, courseId);
        List<LessonVO> lessonVOS = new ArrayList<>();
        int finished = 0;
        for (int i = 0; i < lessons.size(); i++) {
            CourseLesson lesson = lessons.get(i);
            LessonVO lv = new LessonVO();
            BeanUtils.copyProperties(lesson, lv);
            // 权限判定：免费课全开放；付费课前3节或标记试看开放；其余需已购
            boolean can = canPlay(course, i + 1, lesson, purchased);
            lv.setCanPlay(can);
            if (!can) {
                lv.setLockReason("付费内容，购买课程后解锁完整课时");
            }
            LearnProgress p = progressMap.get(lesson.getId());
            if (p != null) {
                lv.setProgress(p.getProgress());
                lv.setPositionSeconds(p.getPositionSeconds());
                if (p.getStatus() != null && p.getStatus() == 1) {
                    finished++;
                }
            } else {
                lv.setProgress(0);
                lv.setPositionSeconds(0);
            }
            lessonVOS.add(lv);
        }
        // 按 chapter 字段分组为章节目录，保持顺序
        LinkedHashMap<String, List<LessonVO>> chapterMap = new LinkedHashMap<>();
        for (LessonVO lv : lessonVOS) {
            chapterMap.computeIfAbsent(lv.getChapter(), k -> new ArrayList<>()).add(lv);
        }
        List<ChapterVO> chapters = new ArrayList<>();
        chapterMap.forEach((name, list) -> {
            ChapterVO ch = new ChapterVO();
            ch.setChapter(name);
            ch.setLessons(list);
            chapters.add(ch);
        });
        vo.setChapters(chapters);
        vo.setFinishedLessons(finished);
        int courseProgress = lessons.isEmpty() ? 0 : (int) Math.round(finished * 100.0 / lessons.size());
        vo.setCourseProgress(courseProgress);
        return vo;
    }

    /** 讲师新增/编辑课程（讲师只能维护自己的课） */
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public Long saveCourse(CourseDTO dto) {
        LoginUser loginUser = UserContext.get();
        Course course = new Course();
        BeanUtils.copyProperties(dto, course);
        // 免费课价格强制为 0
        if (course.getIsFree() != null && course.getIsFree() == 1) {
            course.setPrice(BigDecimal.ZERO);
        }
        if (dto.getId() == null) {
            // 新建：归属当前讲师，默认待审核、未上架
            course.setTeacherId(loginUser.getId());
            course.setAuditStatus(1);
            course.setStatus(0);
            course.setLessonCount(0);
            course.setTotalDuration(0);
            course.setStudentCount(0);
            course.setViewCount(0);
            course.setScore(new BigDecimal("4.8"));
            course.setIsRecommend(0);
            courseMapper.insert(course);
        } else {
            Course exist = courseMapper.selectById(dto.getId());
            if (exist == null) {
                throw new BusinessException("课程不存在");
            }
            // 越权校验：讲师只能改自己的课，管理员不限
            if (!loginUser.isAdmin() && !exist.getTeacherId().equals(loginUser.getId())) {
                throw new BusinessException(403, "只能维护自己的课程");
            }
            // 内容被编辑后重新进入待审核（管理端编辑除外）
            if (!loginUser.isAdmin()) {
                course.setAuditStatus(1);
            }
            courseMapper.updateById(course);
        }
        return course.getId();
    }

    /** 删除课程（逻辑删除，限本人或管理员） */
    public void delete(Long courseId) {
        Course exist = courseMapper.selectById(courseId);
        if (exist == null) {
            throw new BusinessException("课程不存在");
        }
        LoginUser loginUser = UserContext.get();
        if (!loginUser.isAdmin() && !exist.getTeacherId().equals(loginUser.getId())) {
            throw new BusinessException(403, "只能删除自己的课程");
        }
        courseMapper.deleteById(courseId);
    }

    /** 管理员审核课程：2 通过 / 3 驳回 */
    public void audit(Long courseId, Integer auditStatus, String reason) {
        // 入参校验：只允许 2（通过）/ 3（驳回），防止非法值破坏状态机
        if (auditStatus == null || (auditStatus != 2 && auditStatus != 3)) {
            throw new BusinessException("审核结果不合法：仅允许 2=通过 / 3=驳回");
        }
        if (auditStatus == 3 && !StringUtils.hasText(reason)) {
            throw new BusinessException("驳回课程必须填写驳回原因");
        }
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }
        if (course.getAuditStatus() != null && course.getAuditStatus() == 2 && auditStatus == 2) {
            throw new BusinessException("课程已是审核通过状态，无需重复审核");
        }
        Course update = new Course();
        update.setId(courseId);
        update.setAuditStatus(auditStatus);
        if (auditStatus == 3) {
            update.setRejectReason(reason);
            update.setStatus(0);
        } else if (auditStatus == 2) {
            update.setRejectReason(null);
            update.setStatus(1); // 审核通过自动上架
        }
        courseMapper.updateById(update);
    }

    /**
     * 上架/下架。
     * 【安全】讲师调用时校验课程归属，防止讲师越权上下架他人课程；管理员不受限。
     */
    public void changeShelf(Long courseId, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("上下架状态不合法：仅允许 0=下架 / 1=上架");
        }
        Course exist = courseMapper.selectById(courseId);
        if (exist == null) {
            throw new BusinessException("课程不存在");
        }
        LoginUser loginUser = UserContext.get();
        if (!loginUser.isAdmin() && !exist.getTeacherId().equals(loginUser.getId())) {
            throw new BusinessException(403, "只能上下架自己的课程");
        }
        Course update = new Course();
        update.setId(courseId);
        update.setStatus(status);
        courseMapper.updateById(update);
    }

    /** 推荐/取消推荐 */
    public void changeRecommend(Long courseId, Integer isRecommend) {
        Course update = new Course();
        update.setId(courseId);
        update.setIsRecommend(isRecommend);
        courseMapper.updateById(update);
    }

    /** 我的课程（已购 + 免费学过），附带进度 */
    public List<MyCourseVO> myCourses(Long userId) {
        List<CourseOrder> orders = orderMapper.selectList(new LambdaQueryWrapper<CourseOrder>()
                .eq(CourseOrder::getUserId, userId).eq(CourseOrder::getStatus, 1));
        Set<Long> paidIds = orders.stream().map(CourseOrder::getCourseId).collect(Collectors.toSet());
        List<LearnProgress> progresses = progressMapper.selectList(new LambdaQueryWrapper<LearnProgress>()
                .eq(LearnProgress::getUserId, userId));
        Map<Long, List<LearnProgress>> progByCourse = progresses.stream()
                .collect(Collectors.groupingBy(LearnProgress::getCourseId));
        for (Long cid : progByCourse.keySet()) {
            Course c = courseMapper.selectById(cid);
            if (c != null && c.getIsFree() == 1) {
                paidIds.add(cid);
            }
        }
        if (paidIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<Course> courses = courseMapper.selectBatchIds(paidIds);
        List<MyCourseVO> result = new ArrayList<>();
        for (Course c : courses) {
            MyCourseVO vo = new MyCourseVO();
            BeanUtils.copyProperties(c, vo);
            List<LearnProgress> ps = progByCourse.getOrDefault(c.getId(), new ArrayList<>());
            long finished = ps.stream().filter(p -> p.getStatus() == 1).count();
            int total = c.getLessonCount() == null ? 0 : c.getLessonCount();
            vo.setFinishedLessons((int) finished);
            vo.setCourseProgress(total == 0 ? 0 : (int) Math.round(finished * 100.0 / total));
            vo.setSource(c.getIsFree() == 1 ? "免费学习" : "已购买");
            result.add(vo);
        }
        return result;
    }

    /**
     * 判断某用户是否有权学习整门课：免费课直接 true；付费课需存在已支付订单。
     */
    public boolean hasPurchased(Long userId, Course course) {
        if (course.getIsFree() != null && course.getIsFree() == 1) {
            return true;
        }
        if (userId == null) {
            return false;
        }
        Long cnt = orderMapper.selectCount(new LambdaQueryWrapper<CourseOrder>()
                .eq(CourseOrder::getUserId, userId)
                .eq(CourseOrder::getCourseId, course.getId())
                .eq(CourseOrder::getStatus, 1));
        return cnt != null && cnt > 0;
    }

    /** 单课时是否可播放（免费课 / 前3节 / 标记试看 / 已购） */
    public boolean canPlay(Course course, int sortIndex, CourseLesson lesson, boolean purchased) {
        if (course.getIsFree() == 1) {
            return true;
        }
        if (purchased) {
            return true;
        }
        if (sortIndex <= Constants.FREE_TRIAL_COUNT) {
            return true;
        }
        return lesson.getIsFreeTrial() != null && lesson.getIsFreeTrial() == 1;
    }

    /** 重算课程冗余字段（课时数、总时长），课时增删改后调用 */
    public void refreshCourseStat(Long courseId) {
        List<CourseLesson> lessons = lessonMapper.selectList(new LambdaQueryWrapper<CourseLesson>()
                .eq(CourseLesson::getCourseId, courseId).eq(CourseLesson::getStatus, 1));
        int totalDuration = lessons.stream().mapToInt(l -> l.getDuration() == null ? 0 : l.getDuration()).sum();
        Course update = new Course();
        update.setId(courseId);
        update.setLessonCount(lessons.size());
        update.setTotalDuration(totalDuration);
        courseMapper.updateById(update);
    }

    public Course getCourseEntity(Long id) {
        Course c = courseMapper.selectById(id);
        if (c == null) {
            throw new BusinessException("课程不存在");
        }
        return c;
    }

    private LambdaQueryWrapper<Course> buildQueryWrapper(CourseQueryDTO q) {
        LambdaQueryWrapper<Course> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(q.getKeyword())) {
            w.like(Course::getCourseName, q.getKeyword());
        }
        if (q.getCategoryId() != null) {
            w.eq(Course::getCategoryId, q.getCategoryId());
        }
        if (q.getCategory2Id() != null) {
            w.eq(Course::getCategory2Id, q.getCategory2Id());
        }
        if (q.getDifficulty() != null) {
            w.eq(Course::getDifficulty, q.getDifficulty());
        }
        if (q.getIsFree() != null) {
            w.eq(Course::getIsFree, q.getIsFree());
        }
        if (q.getTeacherId() != null) {
            w.eq(Course::getTeacherId, q.getTeacherId());
        }
        return w;
    }

    private void applySort(LambdaQueryWrapper<Course> w, String sort) {
        if ("new".equals(sort)) {
            w.orderByDesc(Course::getId);
        } else if ("price_asc".equals(sort)) {
            w.orderByAsc(Course::getPrice);
        } else {
            // 默认最热：学习人数倒序
            w.orderByDesc(Course::getStudentCount);
        }
    }

    private PageResult<CourseVO> toVOPage(Page<Course> page) {
        PageResult<CourseVO> r = new PageResult<>();
        r.setRecords(fillCourseVO(page.getRecords()));
        r.setTotal(page.getTotal());
        r.setCurrent(page.getCurrent());
        r.setSize(page.getSize());
        return r;
    }

    /** 批量填充讲师名/分类名，避免在循环里一条条查库（N+1 问题） */
    private List<CourseVO> fillCourseVO(List<Course> courses) {
        if (courses == null || courses.isEmpty()) {
            return new ArrayList<>();
        }
        Set<Long> teacherIds = courses.stream().map(Course::getTeacherId).collect(Collectors.toSet());
        Set<Long> categoryIds = new HashSet<>();
        courses.forEach(c -> {
            categoryIds.add(c.getCategoryId());
            if (c.getCategory2Id() != null) {
                categoryIds.add(c.getCategory2Id());
            }
        });
        Map<Long, SysUser> teacherMap = userMapper.selectBatchIds(teacherIds).stream()
                .collect(Collectors.toMap(SysUser::getId, u -> u));
        Map<Long, CourseCategory> catMap = categoryMapper.selectBatchIds(categoryIds).stream()
                .collect(Collectors.toMap(CourseCategory::getId, c -> c));

        List<CourseVO> list = new ArrayList<>();
        for (Course c : courses) {
            CourseVO vo = new CourseVO();
            BeanUtils.copyProperties(c, vo);
            SysUser t = teacherMap.get(c.getTeacherId());
            if (t != null) {
                vo.setTeacherName(t.getNickname());
                vo.setTeacherTitle(t.getTitle());
                vo.setTeacherAvatar(t.getAvatar());
            }
            CourseCategory c1 = catMap.get(c.getCategoryId());
            if (c1 != null) {
                vo.setCategoryName(c1.getName());
            }
            if (c.getCategory2Id() != null) {
                CourseCategory c2 = catMap.get(c.getCategory2Id());
                if (c2 != null) {
                    vo.setCategory2Name(c2.getName());
                }
            }
            list.add(vo);
        }
        return list;
    }

    private Map<Long, LearnProgress> loadProgressMap(Long userId, Long courseId) {
        if (userId == null) {
            return new HashMap<>();
        }
        List<LearnProgress> list = progressMapper.selectList(new LambdaQueryWrapper<LearnProgress>()
                .eq(LearnProgress::getUserId, userId).eq(LearnProgress::getCourseId, courseId));
        return list.stream().collect(Collectors.toMap(LearnProgress::getLessonId, p -> p, (a, b) -> a));
    }
}
