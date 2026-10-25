package com.xingyun.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/** 答题记录（每题一条流水） */
@Data
@TableName("quiz_answer")
public class QuizAnswer {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long quizId;
    private Long userId;
    private Long questionId;
    private String userAnswer;
    /** 0错 1对 */
    private Integer isRight;
    private Integer score;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date createTime;
}
