package com.xingyun.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/** 新增/修改课程分类参数 */
@Data
public class CategoryDTO {
    private Long id;

    @NotBlank(message = "分类名称不能为空")
    private String name;

    @NotNull(message = "父级分类不能为空")
    private Long parentId = 0L;
    private Integer level = 1;
    private String icon;
    private Integer sort = 0;

    @NotNull(message = "分类状态不能为空")
    private Integer status = 0;
}
