package com.xingyun.controller;

import com.xingyun.common.Result;
import com.xingyun.dto.AssignmentSubmitDTO;
import com.xingyun.security.UserContext;
import com.xingyun.service.AssignmentService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/** 作业：学员查看与提交（批改在讲师端 /teacher） */
@RestController
@RequestMapping("/assignment")
public class AssignmentController {

    @Resource
    private AssignmentService assignmentService;

    /** 某课程作业列表（含我的提交状态） */
    @GetMapping("/list/{courseId}")
    public Result<List<Map<String, Object>>> list(@PathVariable Long courseId) {
        return Result.success(assignmentService.listByCourse(courseId, UserContext.getUserId()));
    }

    /** 提交/重新提交作业 */
    @PostMapping("/submit")
    public Result<Void> submit(@Valid @RequestBody AssignmentSubmitDTO dto) {
        assignmentService.submit(dto, UserContext.getUserId());
        return Result.success();
    }
}
