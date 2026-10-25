package com.xingyun.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xingyun.common.BusinessException;
import com.xingyun.dto.LessonDTO;
import com.xingyun.entity.Course;
import com.xingyun.entity.CourseLesson;
import com.xingyun.mapper.CourseLessonMapper;
import com.xingyun.security.LoginUser;
import com.xingyun.security.UserContext;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 课时服务：课时维护（讲师）、课时排序、课时数回写课程。
 */
@Service
public class LessonService {

    @Resource
    private CourseLessonMapper lessonMapper;
    @Resource
    private CourseService courseService;

    /** 某课程的全部课时（按 sort 排序） */
    public List<CourseLesson> listByCourse(Long courseId) {
        return lessonMapper.selectList(new LambdaQueryWrapper<CourseLesson>()
                .eq(CourseLesson::getCourseId, courseId)
                .orderByAsc(CourseLesson::getSort));
    }

    /** 新增/编辑课时（限课程所属讲师或管理员），并回写课程课时统计 */
    public Long save(LessonDTO dto) {
        Course course = courseService.getCourseEntity(dto.getCourseId());
        checkOwner(course);
        CourseLesson lesson = new CourseLesson();
        BeanUtils.copyProperties(dto, lesson);
        if (dto.getId() == null) {
            // sort 不传则自动排到最后
            if (dto.getSort() == null) {
                Long count = lessonMapper.selectCount(new LambdaQueryWrapper<CourseLesson>()
                        .eq(CourseLesson::getCourseId, dto.getCourseId()));
                lesson.setSort(count == null ? 1 : count.intValue() + 1);
            }
            if (lesson.getStatus() == null) {
                lesson.setStatus(1);
            }
            lessonMapper.insert(lesson);
        } else {
            lessonMapper.updateById(lesson);
        }
        courseService.refreshCourseStat(course.getId());
        return lesson.getId();
    }

    public void delete(Long lessonId) {
        CourseLesson lesson = lessonMapper.selectById(lessonId);
        if (lesson == null) {
            throw new BusinessException("课时不存在");
        }
        Course course = courseService.getCourseEntity(lesson.getCourseId());
        checkOwner(course);
        lessonMapper.deleteById(lessonId);
        courseService.refreshCourseStat(course.getId());
    }

    /** 越权校验：非本人且非管理员不允许操作 */
    private void checkOwner(Course course) {
        LoginUser u = UserContext.get();
        if (!u.isAdmin() && !course.getTeacherId().equals(u.getId())) {
            throw new BusinessException(403, "只能维护自己课程下的课时");
        }
    }
}
