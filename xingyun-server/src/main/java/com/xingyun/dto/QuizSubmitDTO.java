package com.xingyun.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/** 提交整套测验参数 */
@Data
public class QuizSubmitDTO {

    @NotNull(message = "测验ID不能为空")
    private Long quizId;

    /** 每题作答 */
    private List<AnswerItem> answers;

    /** 单题作答 */
    @Data
    public static class AnswerItem {
        private Long questionId;
        private String userAnswer;
    }
}
