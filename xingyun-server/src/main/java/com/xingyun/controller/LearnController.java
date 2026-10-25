package com.xingyun.controller;

import com.xingyun.common.Result;
import com.xingyun.dto.ProgressDTO;
import com.xingyun.security.UserContext;
import com.xingyun.service.LearnService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Map;

/** 学习中心：进度上报、续播、学习档案 */
@RestController
@RequestMapping("/learn")
public class LearnController {

    @Resource
    private LearnService learnService;

    /** 上报播放进度（自动记录、续播、学完发证） */
    @PostMapping("/report")
    public Result<Map<String, Object>> report(@Valid @RequestBody ProgressDTO dto) {
        return Result.success(learnService.report(dto, UserContext.getUserId()));
    }

    /** 某课程整体进度 */
    @GetMapping("/courseProgress/{courseId}")
    public Result<Integer> courseProgress(@PathVariable Long courseId) {
        return Result.success(learnService.courseProgress(UserContext.getUserId(), courseId));
    }

    /** 续播信息（上次看到哪） */
    @GetMapping("/continue/{courseId}")
    public Result<Map<String, Object>> continueInfo(@PathVariable Long courseId) {
        return Result.success(learnService.continueInfo(UserContext.getUserId(), courseId));
    }

    /** 个人学习档案 */
    @GetMapping("/profile")
    public Result<Map<String, Object>> profile() {
        return Result.success(learnService.profile(UserContext.getUserId()));
    }
}
