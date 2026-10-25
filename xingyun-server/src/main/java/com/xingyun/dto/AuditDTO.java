package com.xingyun.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/** 课程审核参数（管理员） */
@Data
public class AuditDTO {

    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    /** 2通过 3驳回 */
    @NotNull(message = "审核结果不能为空")
    private Integer auditStatus;

    private String rejectReason;
}
