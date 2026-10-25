package com.xingyun.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 学习进度（user_id + lesson_id 唯一，用于自动续播与学完判定）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("learn_progress")
public class LearnProgress extends BaseEntity {
    private Long userId;
    private Long courseId;
    private Long lessonId;
    /** 0-100 */
    private Integer progress;
    /** 断点续播位置（秒） */
    private Integer positionSeconds;
    private Integer learnedSeconds;
    /** 0学习中 1已完成 */
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date finishTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date lastLearnTime;
}
