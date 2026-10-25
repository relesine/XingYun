package com.xingyun.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/** 学员提问参数 */
@Data
public class QaDTO {

    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    private Long lessonId = 0L;

    @NotBlank(message = "问题内容不能为空")
    @Size(max = 1000, message = "问题内容不能超过1000字")
    private String question;
}
