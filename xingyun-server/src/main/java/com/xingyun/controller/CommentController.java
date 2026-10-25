package com.xingyun.controller;

import com.xingyun.common.Result;
import com.xingyun.dto.CommentDTO;
import com.xingyun.security.UserContext;
import com.xingyun.service.CommentService;
import com.xingyun.vo.CommentVO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/** 课程评论（学员侧） */
@RestController
@RequestMapping("/comment")
public class CommentController {

    @Resource
    private CommentService commentService;

    /** 某课程评论列表（公开内容，登录后可看） */
    @GetMapping("/list/{courseId}")
    public Result<List<CommentVO>> list(@PathVariable Long courseId) {
        return Result.success(commentService.listByCourse(courseId));
    }

    /** 发表评论/评分 */
    @PostMapping("/add")
    public Result<Void> add(@Valid @RequestBody CommentDTO dto) {
        commentService.add(dto, UserContext.getUserId());
        return Result.success();
    }
}
