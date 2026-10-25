package com.xingyun.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/** 讲师新增/编辑课时参数 */
@Data
public class LessonDTO {
    private Long id;

    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    @NotBlank(message = "章节不能为空")
    private String chapter;

    @NotBlank(message = "课时名称不能为空")
    private String lessonName;

    private Long videoId;

    @Min(value = 0, message = "时长不能为负数")
    private Integer duration;

    @Min(value = 1, message = "排序序号最小为1")
    private Integer sort;

    private Integer isFreeTrial;
    private Integer status;
}
