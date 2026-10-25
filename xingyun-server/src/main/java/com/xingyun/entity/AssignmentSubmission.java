package com.xingyun.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 作业提交/批改。状态流转：0 待批改 -> 1 已批改 / 2 已打回。
 * 本表用 submit_time 充当创建时间，另带 update_time 与逻辑删除。
 */
@Data
@TableName("assignment_submission")
public class AssignmentSubmission {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long assignmentId;
    private Long courseId;
    private Long userId;
    private String content;
    private String attachment;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date submitTime;

    /** 0待批改 1已批改 2已打回 */
    private Integer gradeStatus;
    private Integer score;
    private String teacherComment;
    private Long teacherId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date gradeTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date updateTime;

    @TableLogic
    private Integer isDeleted;
}
