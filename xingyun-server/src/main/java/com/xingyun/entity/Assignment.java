package com.xingyun.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/** 作业（讲师布置） */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("assignment")
public class Assignment extends BaseEntity {
    private Long courseId;
    private Long lessonId;
    private Long teacherId;
    private String title;
    private String content;
    private String attachment;
    private Integer totalScore;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date deadline;
}
