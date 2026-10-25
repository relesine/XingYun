package com.xingyun.vo;

import lombok.Data;

import java.util.List;

/** 交卷结果：总分、得分、是否及格、每题对错明细 */
@Data
public class QuizResultVO {
    private Long quizId;
    private String quizName;
    private Integer totalScore;
    private Integer userScore;
    private Integer passScore;
    /** 是否及格 */
    private Boolean passed;
    /** 答对题数 */
    private Integer rightCount;
    private Integer questionCount;
    /** 每道题作答明细 */
    private List<ItemDetail> details;

    @Data
    public static class ItemDetail {
        private Long questionId;
        private String userAnswer;
        private String rightAnswer;
        private Boolean right;
        private Integer score;
    }
}
