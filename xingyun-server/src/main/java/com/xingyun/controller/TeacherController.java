package com.xingyun.controller;

import com.xingyun.common.PageResult;
import com.xingyun.common.Result;
import com.xingyun.dto.*;
import com.xingyun.entity.AssignmentSubmission;
import com.xingyun.entity.SysUser;
import com.xingyun.security.UserContext;
import com.xingyun.service.*;
import com.xingyun.vo.CourseVO;
import com.xingyun.vo.QaVO;
import com.xingyun.vo.TeacherStatVO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 讲师控制器：
 * 1) /teacher/list、/teacher/detail 为公开接口（讲师展示，白名单放行）；
 * 2) 其余 /teacher/** 仅认证讲师/管理员可访问（由 JwtInterceptor 按前缀鉴权）。
 */
@RestController
@RequestMapping("/teacher")
public class TeacherController {

    @Resource
    private UserService userService;
    @Resource
    private CourseService courseService;
    @Resource
    private LessonService lessonService;
    @Resource
    private AssignmentService assignmentService;
    @Resource
    private QaService qaService;
    @Resource
    private StatsService statsService;
    @Resource
    private VideoService videoService;

    @GetMapping("/list")
    public Result<List<SysUser>> list(String keyword) {
        return Result.success(userService.listTeachers(keyword));
    }

    @GetMapping("/detail/{id}")
    public Result<SysUser> detail(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    @GetMapping("/stat")
    public Result<TeacherStatVO> stat() {
        return Result.success(statsService.teacherStat(UserContext.getUserId()));
    }

    @PostMapping("/course/page")
    public Result<PageResult<CourseVO>> myCourse(@RequestBody CourseQueryDTO query) {
        return Result.success(courseService.teacherPage(query, UserContext.getUserId()));
    }

    @PostMapping("/course/save")
    public Result<Long> saveCourse(@Valid @RequestBody CourseDTO dto) {
        return Result.success("保存成功", courseService.saveCourse(dto));
    }

    @DeleteMapping("/course/{id}")
    public Result<Void> deleteCourse(@PathVariable Long id) {
        courseService.delete(id);
        return Result.success();
    }

    /** 上架/下架 */
    @PostMapping("/course/shelf")
    public Result<Void> shelf(@RequestBody IdStatusDTO dto) {
        courseService.changeShelf(dto.getId(), dto.getStatus());
        return Result.success();
    }

    @PostMapping("/lesson/save")
    public Result<Long> saveLesson(@Valid @RequestBody LessonDTO dto) {
        return Result.success(lessonService.save(dto));
    }

    @DeleteMapping("/lesson/{id}")
    public Result<Void> deleteLesson(@PathVariable Long id) {
        lessonService.delete(id);
        return Result.success();
    }

    /** 视频库（讲师选择已上传视频） */
    @GetMapping("/video/library")
    public Result<Object> videoLibrary() {
        return Result.success(videoService.listAll());
    }

    /** 某课程下的学员学习进度（仅可查看本人课程，Service 内做越权校验） */
    @GetMapping("/course/students/{courseId}")
    public Result<List<Map<String, Object>>> courseStudents(@PathVariable Long courseId) {
        return Result.success(statsService.courseStudents(courseId, UserContext.getUserId()));
    }

    /** 提交记录列表，gradeStatus：0待批 1已批 2打回，空为全部（含学员名/作业标题/课程名） */
    @GetMapping("/assignment/submissions")
    public Result<List<java.util.Map<String, Object>>> submissions(Integer gradeStatus) {
        return Result.success(assignmentService.teacherSubmissions(UserContext.getUserId(), gradeStatus));
    }

    @PostMapping("/assignment/grade")
    public Result<Void> grade(@Valid @RequestBody GradeDTO dto) {
        assignmentService.grade(dto, UserContext.getUserId());
        return Result.success("批改成功", null);
    }

    @GetMapping("/qa/pending")
    public Result<List<QaVO>> pendingQa() {
        return Result.success(qaService.teacherPending(UserContext.getUserId()));
    }

    @PostMapping("/qa/reply")
    public Result<Void> reply(@Valid @RequestBody ReplyDTO dto) {
        qaService.reply(dto.getQaId(), dto.getReplyContent(), UserContext.getUserId());
        return Result.success();
    }
}
