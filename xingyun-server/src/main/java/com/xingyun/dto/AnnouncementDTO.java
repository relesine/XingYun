package com.xingyun.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/** 公告新增/修改参数 */
@Data
public class AnnouncementDTO {
    private Long id;

    @NotBlank(message = "公告标题不能为空")
    private String title;

    @NotBlank(message = "公告内容不能为空")
    private String content;

    private Integer type = 1;
    private Integer status = 1;
}
