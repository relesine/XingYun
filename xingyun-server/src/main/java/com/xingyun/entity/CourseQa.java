package com.xingyun.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/** 课程问答（学员提问，讲师/管理员回复） */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("course_qa")
public class CourseQa extends BaseEntity {
    private Long courseId;
    private Long lessonId;
    private Long userId;
    private String question;
    private Long replyUserId;
    private String replyContent;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date replyTime;

    /** 0待回复 1已回复 */
    private Integer status;
}
