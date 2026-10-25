package com.xingyun.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 随堂测验（挂在课时下） */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("quiz")
public class Quiz extends BaseEntity {
    private Long courseId;
    private Long lessonId;
    private String quizName;
    private Integer questionCount;
    private Integer totalScore;
    private Integer passScore;
    private Integer status;
}
