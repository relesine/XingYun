package com.xingyun.controller;

import com.xingyun.common.Result;
import com.xingyun.dto.QuizSubmitDTO;
import com.xingyun.security.UserContext;
import com.xingyun.service.QuizService;
import com.xingyun.vo.QuizResultVO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/** 随堂测验：拉题、交卷判分、错题本 */
@RestController
@RequestMapping("/quiz")
public class QuizController {

    @Resource
    private QuizService quizService;

    /** 按课时获取测验与题目 */
    @GetMapping("/byLesson/{lessonId}")
    public Result<Map<String, Object>> byLesson(@PathVariable Long lessonId) {
        return Result.success(quizService.getByLesson(lessonId));
    }

    /** 提交测验（后端判分） */
    @PostMapping("/submit")
    public Result<QuizResultVO> submit(@Valid @RequestBody QuizSubmitDTO dto) {
        return Result.success(quizService.submit(dto, UserContext.getUserId()));
    }

    /** 我的错题本 */
    @GetMapping("/wrongBook")
    public Result<List<Map<String, Object>>> wrongBook() {
        return Result.success(quizService.wrongBook(UserContext.getUserId()));
    }
}
