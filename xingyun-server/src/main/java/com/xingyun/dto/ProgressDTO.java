package com.xingyun.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 学习进度上报参数（播放器每隔几秒/暂停时上报，实现自动记录与断点续播）。
 */
@Data
public class ProgressDTO {

    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    @NotNull(message = "课时ID不能为空")
    private Long lessonId;

    /** 当前播放位置（秒） */
    @NotNull(message = "播放位置不能为空")
    @Min(value = 0, message = "播放位置不能为负数")
    @Max(value = 86400, message = "播放位置超出合理范围")
    private Integer positionSec;

    /** 该课时总时长（秒），用于计算进度百分比；后端优先以课时登记时长为权威值 */
    @Min(value = 0, message = "总时长不能为负数")
    private Integer totalSec;

    /** 播放设备 */
    private String device;
}
