package com.xingyun.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xingyun.common.BusinessException;
import com.xingyun.dto.QaDTO;
import com.xingyun.entity.Course;
import com.xingyun.entity.CourseQa;
import com.xingyun.entity.SysUser;
import com.xingyun.mapper.CourseMapper;
import com.xingyun.mapper.CourseQaMapper;
import com.xingyun.mapper.SysUserMapper;
import com.xingyun.vo.QaVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** 课程问答服务：学员提问、讲师/管理员回复 */
@Service
public class QaService {

    @Resource
    private CourseQaMapper qaMapper;
    @Resource
    private CourseMapper courseMapper;
    @Resource
    private SysUserMapper userMapper;

    /** 某课程全部问答 */
    public List<QaVO> listByCourse(Long courseId) {
        List<CourseQa> list = qaMapper.selectList(new LambdaQueryWrapper<CourseQa>()
                .eq(CourseQa::getCourseId, courseId).orderByDesc(CourseQa::getId));
        return fill(list);
    }

    /** 学员提问 */
    public void ask(QaDTO dto, Long userId) {
        CourseQa qa = new CourseQa();
        qa.setCourseId(dto.getCourseId());
        qa.setLessonId(dto.getLessonId() == null ? 0L : dto.getLessonId());
        qa.setUserId(userId);
        qa.setQuestion(dto.getQuestion());
        qa.setStatus(0);
        qaMapper.insert(qa);
    }

    /** 讲师/管理员回复 */
    public void reply(Long qaId, String content, Long replyUserId) {
        CourseQa qa = qaMapper.selectById(qaId);
        if (qa == null) {
            throw new BusinessException("问题不存在");
        }
        // 讲师只能回复自己课程的问题
        SysUser u = userMapper.selectById(replyUserId);
        if (u != null && u.getUserType() == 2) {
            Course course = courseMapper.selectById(qa.getCourseId());
            if (course == null || !course.getTeacherId().equals(replyUserId)) {
                throw new BusinessException(403, "只能回复自己课程下的问题");
            }
        }
        CourseQa up = new CourseQa();
        up.setId(qaId);
        up.setReplyContent(content);
        up.setReplyUserId(replyUserId);
        up.setReplyTime(new Date());
        up.setStatus(1);
        qaMapper.updateById(up);
    }

    /** 讲师待回复问题列表（自己课程下 status=0） */
    public List<QaVO> teacherPending(Long teacherId) {
        List<Course> myCourses = courseMapper.selectList(new LambdaQueryWrapper<Course>()
                .eq(Course::getTeacherId, teacherId));
        if (myCourses.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> ids = myCourses.stream().map(Course::getId).collect(Collectors.toList());
        List<CourseQa> list = qaMapper.selectList(new LambdaQueryWrapper<CourseQa>()
                .in(CourseQa::getCourseId, ids).eq(CourseQa::getStatus, 0)
                .orderByDesc(CourseQa::getId));
        return fill(list);
    }

    public long pendingCount(Long teacherId) {
        List<Course> myCourses = courseMapper.selectList(new LambdaQueryWrapper<Course>()
                .eq(Course::getTeacherId, teacherId));
        if (myCourses.isEmpty()) {
            return 0;
        }
        List<Long> ids = myCourses.stream().map(Course::getId).collect(Collectors.toList());
        Long c = qaMapper.selectCount(new LambdaQueryWrapper<CourseQa>()
                .in(CourseQa::getCourseId, ids).eq(CourseQa::getStatus, 0));
        return c == null ? 0 : c;
    }

    private List<QaVO> fill(List<CourseQa> list) {
        if (list.isEmpty()) {
            return new ArrayList<>();
        }
        Set<Long> userIds = list.stream().flatMap(q -> {
            java.util.List<Long> l = new ArrayList<>();
            l.add(q.getUserId());
            if (q.getReplyUserId() != null) {
                l.add(q.getReplyUserId());
            }
            return l.stream();
        }).collect(Collectors.toSet());
        Map<Long, SysUser> map = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, u -> u));
        // 批量查课程名，避免逐条查询
        Set<Long> courseIds = list.stream().map(CourseQa::getCourseId).collect(Collectors.toSet());
        Map<Long, String> courseNameMap = courseMapper.selectBatchIds(courseIds).stream()
                .collect(Collectors.toMap(Course::getId, Course::getCourseName, (a, b) -> a));
        List<QaVO> result = new ArrayList<>();
        for (CourseQa q : list) {
            QaVO vo = new QaVO();
            BeanUtils.copyProperties(q, vo);
            vo.setCourseName(courseNameMap.get(q.getCourseId()));
            SysUser asker = map.get(q.getUserId());
            if (asker != null) {
                vo.setAskerName(asker.getNickname());
                vo.setAskerAvatar(asker.getAvatar());
            }
            if (q.getReplyUserId() != null) {
                SysUser replier = map.get(q.getReplyUserId());
                if (replier != null) {
                    vo.setReplierName(replier.getNickname());
                }
            }
            result.add(vo);
        }
        return result;
    }
}
