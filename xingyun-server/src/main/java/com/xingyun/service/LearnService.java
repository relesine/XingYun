package com.xingyun.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xingyun.common.BusinessException;
import com.xingyun.dto.ProgressDTO;
import com.xingyun.entity.*;
import com.xingyun.mapper.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 学习进度服务（核心链路）：
 * 1. 播放器定时上报 -> upsert 进度（user+lesson 唯一，天然防重复提交）；
 * 2. 记录断点位置用于「续播」；
 * 3. 进度达 100% 标记课时完成；
 * 4. 整门课全部完成时自动生成结业证书。
 */
@Service
public class LearnService {

    @Resource
    private LearnProgressMapper progressMapper;
    @Resource
    private PlayRecordMapper playRecordMapper;
    @Resource
    private CourseLessonMapper lessonMapper;
    @Resource
    private CourseMapper courseMapper;
    @Resource
    private SysUserMapper userMapper;
    @Resource
    private CertificateService certificateService;
    @Resource
    private CourseService courseService;

    /**
     * 上报播放进度
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> report(ProgressDTO dto, Long userId) {
        // 1. 校验课时确实属于该课程，防止伪造 courseId
        CourseLesson lesson = lessonMapper.selectById(dto.getLessonId());
        if (lesson == null || !lesson.getCourseId().equals(dto.getCourseId())) {
            throw new BusinessException("课时与课程不匹配");
        }
        if (dto.getPositionSec() == null || dto.getPositionSec() < 0) {
            throw new BusinessException("播放位置不合法");
        }
        // 2.【安全】权限校验：免费课/前3节/标记试看/已购才允许上报，防止未购学员伪造进度白拿证书
        Course course = courseMapper.selectById(dto.getCourseId());
        if (course == null) {
            throw new BusinessException("课程不存在");
        }
        boolean purchased = courseService.hasPurchased(userId, course);
        List<CourseLesson> allLessons = lessonMapper.selectList(new LambdaQueryWrapper<CourseLesson>()
                .eq(CourseLesson::getCourseId, course.getId())
                .eq(CourseLesson::getStatus, 1)
                .orderByAsc(CourseLesson::getSort));
        int sortIndex = 0;
        for (int i = 0; i < allLessons.size(); i++) {
            if (allLessons.get(i).getId().equals(lesson.getId())) {
                sortIndex = i + 1; // 1-based
                break;
            }
        }
        if (!courseService.canPlay(course, sortIndex, lesson, purchased)) {
            throw new BusinessException(403, "该课时为付费内容，购买课程后解锁");
        }
        // 3. 计算进度百分比：以课时登记时长为权威值（防止前端伪造 totalSec 刷进度），
        //    仅当课时时长为 0 时才回退到上报的总时长。
        int total = (lesson.getDuration() != null && lesson.getDuration() > 0)
                ? lesson.getDuration()
                : (dto.getTotalSec() != null && dto.getTotalSec() > 0 ? dto.getTotalSec() : 0);
        int progress = 0;
        if (total > 0) {
            progress = (int) Math.min(100, Math.round(dto.getPositionSec() * 100.0 / total));
        }
        boolean justFinished = progress >= 100;

        // upsert：按 user+lesson 找已有记录，保证只有一条，杜绝重复提交产生多条
        LearnProgress exist = progressMapper.selectOne(new LambdaQueryWrapper<LearnProgress>()
                .eq(LearnProgress::getUserId, userId)
                .eq(LearnProgress::getLessonId, dto.getLessonId()));
        boolean newFinish = false;
        if (exist == null) {
            LearnProgress p = new LearnProgress();
            p.setUserId(userId);
            p.setCourseId(dto.getCourseId());
            p.setLessonId(dto.getLessonId());
            p.setProgress(progress);
            p.setPositionSeconds(dto.getPositionSec());
            p.setLearnedSeconds(dto.getPositionSec());
            p.setLastLearnTime(new Date());
            p.setStatus(justFinished ? 1 : 0);
            if (justFinished) {
                p.setFinishTime(new Date());
                newFinish = true;
            }
            progressMapper.insert(p);
        } else {
            LearnProgress update = new LearnProgress();
            update.setId(exist.getId());
            update.setProgress(Math.max(exist.getProgress() == null ? 0 : exist.getProgress(), progress));
            update.setPositionSeconds(dto.getPositionSec());
            update.setLearnedSeconds(Math.max(exist.getLearnedSeconds() == null ? 0 : exist.getLearnedSeconds(), dto.getPositionSec()));
            update.setLastLearnTime(new Date());
            if (justFinished && (exist.getStatus() == null || exist.getStatus() == 0)) {
                update.setStatus(1);
                update.setFinishTime(new Date());
                newFinish = true;
            }
            progressMapper.updateById(update);
        }

        // 4. 播放流水（每次上报一条，便于行为分析）
        PlayRecord record = new PlayRecord();
        record.setUserId(userId);
        record.setCourseId(dto.getCourseId());
        record.setLessonId(dto.getLessonId());
        record.setPositionSec(dto.getPositionSec());
        record.setDevice(dto.getDevice());
        playRecordMapper.insert(record);

        // 5. 新完成一个课时：累加学习时长，并判断整门课是否学完 -> 自动发证
        Certificate cert = null;
        if (newFinish) {
            addStudyMinutes(userId, total);
            cert = checkAndIssueCertificate(userId, dto.getCourseId());
        }

        int courseProgress = courseProgress(userId, dto.getCourseId());
        Map<String, Object> result = new HashMap<>();
        result.put("lessonProgress", progress);
        result.put("courseProgress", courseProgress);
        result.put("lessonFinished", justFinished);
        result.put("newCertificate", cert);
        return result;
    }

    /** 某课程的学习进度（完成课时/总课时） */
    public int courseProgress(Long userId, Long courseId) {
        List<CourseLesson> lessons = lessonMapper.selectList(new LambdaQueryWrapper<CourseLesson>()
                .eq(CourseLesson::getCourseId, courseId).eq(CourseLesson::getStatus, 1));
        if (lessons.isEmpty()) {
            return 0;
        }
        Long finished = progressMapper.selectCount(new LambdaQueryWrapper<LearnProgress>()
                .eq(LearnProgress::getUserId, userId)
                .eq(LearnProgress::getCourseId, courseId)
                .eq(LearnProgress::getStatus, 1));
        return (int) Math.round((finished == null ? 0 : finished) * 100.0 / lessons.size());
    }

    /** 续播：获取上次播放位置 */
    public Map<String, Object> continueInfo(Long userId, Long courseId) {
        LearnProgress last = progressMapper.selectOne(new LambdaQueryWrapper<LearnProgress>()
                .eq(LearnProgress::getUserId, userId)
                .eq(LearnProgress::getCourseId, courseId)
                .orderByDesc(LearnProgress::getLastLearnTime).last("limit 1"));
        Map<String, Object> map = new HashMap<>();
        if (last != null) {
            map.put("lessonId", last.getLessonId());
            map.put("positionSeconds", last.getPositionSeconds());
            map.put("progress", last.getProgress());
        }
        return map;
    }

    /** 个人学习档案：学习课程数、完成课程数、累计时长、积分等 */
    public Map<String, Object> profile(Long userId) {
        SysUser user = userMapper.selectById(userId);
        Long learnedCourse = progressMapper.selectList(new LambdaQueryWrapper<LearnProgress>()
                .eq(LearnProgress::getUserId, userId)).stream()
                .map(LearnProgress::getCourseId).distinct().count();
        Long finishedLesson = progressMapper.selectCount(new LambdaQueryWrapper<LearnProgress>()
                .eq(LearnProgress::getUserId, userId).eq(LearnProgress::getStatus, 1));
        Map<String, Object> map = new HashMap<>();
        map.put("studyMinutes", user == null ? 0 : user.getStudyMinutes());
        map.put("integral", user == null ? 0 : user.getIntegral());
        map.put("learnedCourseCount", learnedCourse);
        map.put("finishedLessonCount", finishedLesson);
        return map;
    }

    /** 检查整门课是否全部学完，若是则自动发证 */
    private Certificate checkAndIssueCertificate(Long userId, Long courseId) {
        List<CourseLesson> lessons = lessonMapper.selectList(new LambdaQueryWrapper<CourseLesson>()
                .eq(CourseLesson::getCourseId, courseId).eq(CourseLesson::getStatus, 1));
        if (lessons.isEmpty()) {
            return null;
        }
        Long finished = progressMapper.selectCount(new LambdaQueryWrapper<LearnProgress>()
                .eq(LearnProgress::getUserId, userId)
                .eq(LearnProgress::getCourseId, courseId)
                .eq(LearnProgress::getStatus, 1));
        if (finished != null && finished >= lessons.size()) {
            Course course = courseMapper.selectById(courseId);
            // 全部课时完成，自动生成证书（方法内部保证幂等）
            return certificateService.tryIssue(userId, courseId, course.getCourseName());
        }
        return null;
    }

    /** 累加学习时长（分钟）与积分（每完成1分钟积1分），这里按课时秒数折算 */
    private void addStudyMinutes(Long userId, int seconds) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            return;
        }
        int addMin = Math.max(1, seconds / 60);
        SysUser update = new SysUser();
        update.setId(userId);
        update.setStudyMinutes((user.getStudyMinutes() == null ? 0 : user.getStudyMinutes()) + addMin);
        update.setIntegral((user.getIntegral() == null ? 0 : user.getIntegral()) + addMin);
        userMapper.updateById(update);
    }
}
