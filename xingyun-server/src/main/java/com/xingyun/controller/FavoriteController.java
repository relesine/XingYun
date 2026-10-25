package com.xingyun.controller;

import com.xingyun.common.Result;
import com.xingyun.entity.Course;
import com.xingyun.security.UserContext;
import com.xingyun.service.FavoriteService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/** 课程收藏 */
@RestController
@RequestMapping("/favorite")
public class FavoriteController {

    @Resource
    private FavoriteService favoriteService;

    /** 收藏/取消收藏切换 */
    @PostMapping("/toggle/{courseId}")
    public Result<Map<String, Object>> toggle(@PathVariable Long courseId) {
        return Result.success(favoriteService.toggle(UserContext.getUserId(), courseId));
    }

    /** 我的收藏 */
    @GetMapping("/my")
    public Result<List<Course>> my() {
        return Result.success(favoriteService.myFavorites(UserContext.getUserId()));
    }
}
