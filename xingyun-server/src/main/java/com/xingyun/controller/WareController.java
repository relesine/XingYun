package com.xingyun.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xingyun.common.Result;
import com.xingyun.entity.CourseWare;
import com.xingyun.mapper.CourseWareMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/** 课件（学员查看课件清单，文件本身通过 /upload/ware 静态地址下载） */
@RestController
@RequestMapping("/ware")
public class WareController {

    @Resource
    private CourseWareMapper wareMapper;

    @GetMapping("/list/{courseId}")
    public Result<List<CourseWare>> list(@PathVariable Long courseId) {
        List<CourseWare> list = wareMapper.selectList(new LambdaQueryWrapper<CourseWare>()
                .eq(CourseWare::getCourseId, courseId).orderByAsc(CourseWare::getId));
        return Result.success(list);
    }
}
