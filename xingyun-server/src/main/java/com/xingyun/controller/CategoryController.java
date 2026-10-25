package com.xingyun.controller;

import com.xingyun.common.Result;
import com.xingyun.service.CategoryService;
import com.xingyun.vo.CategoryTreeVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/** 课程分类（前台） */
@RestController
@RequestMapping("/category")
public class CategoryController {

    @Resource
    private CategoryService categoryService;

    /** 两级分类树（公开接口） */
    @GetMapping("/tree")
    public Result<List<CategoryTreeVO>> tree() {
        return Result.success(categoryService.tree());
    }
}
