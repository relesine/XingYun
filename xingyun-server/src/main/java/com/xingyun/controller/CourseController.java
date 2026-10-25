package com.xingyun.controller;

import com.xingyun.common.PageResult;
import com.xingyun.common.Result;
import com.xingyun.dto.CourseQueryDTO;
import com.xingyun.security.UserContext;
import com.xingyun.service.CourseService;
import com.xingyun.vo.CourseDetailVO;
import com.xingyun.vo.CourseVO;
import com.xingyun.vo.MyCourseVO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 课程控制器（面向学员/游客的只读部分）。讲师维护走 /teacher，管理员走 /admin。
 */
@RestController
@RequestMapping("/course")
public class CourseController {

    @Resource
    private CourseService courseService;

    /** 课程分页检索（公开） */
    @PostMapping("/page")
    public Result<PageResult<CourseVO>> page(@RequestBody CourseQueryDTO query) {
        return Result.success(courseService.page(query));
    }

    /** 课程详情，含章节目录与当前用户权限/进度（公开，登录后带进度） */
    @GetMapping("/detail/{id}")
    public Result<CourseDetailVO> detail(@PathVariable Long id) {
        return Result.success(courseService.detail(id));
    }

    /** 热门推荐课程（公开） */
    @GetMapping("/hot")
    public Result<List<CourseVO>> hot(@RequestParam(defaultValue = "8") int limit) {
        return Result.success(courseService.hot(limit));
    }

    /** 我的课程（已购/免费在学） */
    @GetMapping("/my")
    public Result<List<MyCourseVO>> my() {
        return Result.success(courseService.myCourses(UserContext.getUserId()));
    }
}
