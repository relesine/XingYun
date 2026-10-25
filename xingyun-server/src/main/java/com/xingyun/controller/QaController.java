package com.xingyun.controller;

import com.xingyun.common.Result;
import com.xingyun.dto.QaDTO;
import com.xingyun.security.UserContext;
import com.xingyun.service.QaService;
import com.xingyun.vo.QaVO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/** 课程问答（学员提问，回复在讲师端） */
@RestController
@RequestMapping("/qa")
public class QaController {

    @Resource
    private QaService qaService;

    @GetMapping("/list/{courseId}")
    public Result<List<QaVO>> list(@PathVariable Long courseId) {
        return Result.success(qaService.listByCourse(courseId));
    }

    @PostMapping("/ask")
    public Result<Void> ask(@Valid @RequestBody QaDTO dto) {
        qaService.ask(dto, UserContext.getUserId());
        return Result.success();
    }
}
