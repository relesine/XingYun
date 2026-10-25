package com.xingyun.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/** 讲师批改作业参数 */
@Data
public class GradeDTO {

    @NotNull(message = "提交记录ID不能为空")
    private Long submissionId;

    @NotNull(message = "分数不能为空")
    @Min(value = 0, message = "分数不能低于0")
    @Max(value = 100, message = "分数不能高于100")
    private Integer score;

    private String teacherComment;

    /** 1已批改 2已打回，默认批改通过 */
    private Integer gradeStatus = 1;
}
