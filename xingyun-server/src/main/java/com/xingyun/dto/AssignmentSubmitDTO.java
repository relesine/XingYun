package com.xingyun.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/** 学员提交作业参数 */
@Data
public class AssignmentSubmitDTO {

    @NotNull(message = "作业ID不能为空")
    private Long assignmentId;

    private String content;
    private String attachment;
}
