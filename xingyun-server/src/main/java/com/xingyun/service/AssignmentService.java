package com.xingyun.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xingyun.common.BusinessException;
import com.xingyun.dto.AssignmentSubmitDTO;
import com.xingyun.dto.GradeDTO;
import com.xingyun.entity.Assignment;
import com.xingyun.entity.AssignmentSubmission;
import com.xingyun.entity.Course;
import com.xingyun.entity.SysUser;
import com.xingyun.mapper.AssignmentMapper;
import com.xingyun.mapper.AssignmentSubmissionMapper;
import com.xingyun.mapper.CourseMapper;
import com.xingyun.mapper.SysUserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 作业服务：学员提交、讲师批改。
 * 提交状态流转：0 待批改 -> 1 已批改；讲师可打回（2）后允许学员重新提交。
 */
@Service
public class AssignmentService {

    @Resource
    private AssignmentMapper assignmentMapper;
    @Resource
    private AssignmentSubmissionMapper submissionMapper;
    @Resource
    private SysUserMapper userMapper;
    @Resource
    private CourseMapper courseMapper;

    /** 某课程的作业列表（附带当前学员自己的提交状态） */
    public List<Map<String, Object>> listByCourse(Long courseId, Long userId) {
        List<Assignment> list = assignmentMapper.selectList(new LambdaQueryWrapper<Assignment>()
                .eq(Assignment::getCourseId, courseId).orderByDesc(Assignment::getId));
        if (list.isEmpty()) {
            return List.of();
        }
        List<Long> ids = list.stream().map(Assignment::getId).collect(Collectors.toList());
        Map<Long, AssignmentSubmission> myMap = userId == null ? Map.of()
                : submissionMapper.selectList(new LambdaQueryWrapper<AssignmentSubmission>()
                .eq(AssignmentSubmission::getUserId, userId)
                .in(AssignmentSubmission::getAssignmentId, ids)).stream()
                .collect(Collectors.toMap(AssignmentSubmission::getAssignmentId, s -> s, (a, b) -> a));
        return list.stream().map(a -> {
            Map<String, Object> m = new HashMap<>();
            m.put("assignment", a);
            m.put("mySubmission", myMap.get(a.getId()));
            return m;
        }).collect(Collectors.toList());
    }

    /** 学员提交/重新提交作业（同一作业同一学员只有一条，重复提交为更新，防止刷多条） */
    @Transactional(rollbackFor = Exception.class)
    public void submit(AssignmentSubmitDTO dto, Long userId) {
        Assignment assignment = assignmentMapper.selectById(dto.getAssignmentId());
        if (assignment == null) {
            throw new BusinessException("作业不存在");
        }
        AssignmentSubmission exist = submissionMapper.selectOne(new LambdaQueryWrapper<AssignmentSubmission>()
                .eq(AssignmentSubmission::getAssignmentId, dto.getAssignmentId())
                .eq(AssignmentSubmission::getUserId, userId));
        if (exist == null) {
            AssignmentSubmission s = new AssignmentSubmission();
            s.setAssignmentId(dto.getAssignmentId());
            s.setCourseId(assignment.getCourseId());
            s.setUserId(userId);
            s.setContent(dto.getContent());
            s.setAttachment(dto.getAttachment());
            s.setSubmitTime(new Date());
            s.setGradeStatus(0);
            submissionMapper.insert(s);
        } else {
            // 已批改(1)的作业不允许再次提交，只有打回(2)或待批(0)可改
            if (exist.getGradeStatus() != null && exist.getGradeStatus() == 1) {
                throw new BusinessException("作业已批改，不能重复提交");
            }
            exist.setContent(dto.getContent());
            exist.setAttachment(dto.getAttachment());
            exist.setSubmitTime(new Date());
            exist.setGradeStatus(0);
            exist.setScore(null);
            exist.setTeacherComment(null);
            submissionMapper.updateById(exist);
        }
    }

    /** 讲师：待批改/已批改列表（只看自己课程的作业），并联表填充学员名/作业标题/课程名 */
    public List<Map<String, Object>> teacherSubmissions(Long teacherId, Integer gradeStatus) {
        List<Assignment> mine = assignmentMapper.selectList(new LambdaQueryWrapper<Assignment>()
                .eq(Assignment::getTeacherId, teacherId));
        if (mine.isEmpty()) {
            return List.of();
        }
        Map<Long, Assignment> asgMap = mine.stream()
                .collect(Collectors.toMap(Assignment::getId, a -> a, (a, b) -> a));
        List<Long> assignmentIds = mine.stream().map(Assignment::getId).collect(Collectors.toList());
        LambdaQueryWrapper<AssignmentSubmission> w = new LambdaQueryWrapper<AssignmentSubmission>()
                .in(AssignmentSubmission::getAssignmentId, assignmentIds)
                .orderByAsc(AssignmentSubmission::getGradeStatus)
                .orderByDesc(AssignmentSubmission::getSubmitTime);
        if (gradeStatus != null) {
            w.eq(AssignmentSubmission::getGradeStatus, gradeStatus);
        }
        List<AssignmentSubmission> subs = submissionMapper.selectList(w);
        if (subs.isEmpty()) {
            return List.of();
        }
        // 批量查学员、课程，避免 N+1
        Map<Long, String> userMap = userMapper.selectBatchIds(
                subs.stream().map(AssignmentSubmission::getUserId).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(SysUser::getId, SysUser::getNickname, (a, b) -> a));
        Map<Long, Course> courseMap = courseMapper.selectBatchIds(
                mine.stream().map(Assignment::getCourseId).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(Course::getId, c -> c, (a, b) -> a));

        return subs.stream().map(s -> {
            Map<String, Object> m = new HashMap<>();
            m.put("submission", s);
            Assignment asg = asgMap.get(s.getAssignmentId());
            m.put("assignmentTitle", asg == null ? "" : asg.getTitle());
            m.put("totalScore", asg == null ? 100 : asg.getTotalScore());
            Course course = asg == null ? null : courseMap.get(asg.getCourseId());
            m.put("courseName", course == null ? "" : course.getCourseName());
            m.put("studentName", userMap.get(s.getUserId()));
            return m;
        }).collect(Collectors.toList());
    }

    /** 讲师批改打分 */
    @Transactional(rollbackFor = Exception.class)
    public void grade(GradeDTO dto, Long teacherId) {
        AssignmentSubmission sub = submissionMapper.selectById(dto.getSubmissionId());
        if (sub == null) {
            throw new BusinessException("提交记录不存在");
        }
        Assignment assignment = assignmentMapper.selectById(sub.getAssignmentId());
        // 越权校验：只能批改自己课程的作业
        if (!assignment.getTeacherId().equals(teacherId)) {
            throw new BusinessException(403, "只能批改自己课程的作业");
        }
        if (dto.getScore() < 0 || dto.getScore() > assignment.getTotalScore()) {
            throw new BusinessException("分数需在 0-" + assignment.getTotalScore() + " 之间");
        }
        AssignmentSubmission update = new AssignmentSubmission();
        update.setId(sub.getId());
        update.setScore(dto.getScore());
        update.setTeacherComment(dto.getTeacherComment());
        update.setTeacherId(teacherId);
        update.setGradeStatus(dto.getGradeStatus() == null ? 1 : dto.getGradeStatus());
        update.setGradeTime(new Date());
        submissionMapper.updateById(update);
    }

    public long pendingCount(Long teacherId) {
        List<Assignment> mine = assignmentMapper.selectList(new LambdaQueryWrapper<Assignment>()
                .eq(Assignment::getTeacherId, teacherId));
        if (mine.isEmpty()) {
            return 0;
        }
        List<Long> ids = mine.stream().map(Assignment::getId).collect(Collectors.toList());
        Long c = submissionMapper.selectCount(new LambdaQueryWrapper<AssignmentSubmission>()
                .in(AssignmentSubmission::getAssignmentId, ids)
                .eq(AssignmentSubmission::getGradeStatus, 0));
        return c == null ? 0 : c;
    }
}
