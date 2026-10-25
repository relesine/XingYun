package com.xingyun.dto;

import lombok.Data;

import javax.validation.constraints.Digits;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/** 讲师新增/编辑自己课程参数 */
@Data
public class CourseDTO {
    private Long id;

    @NotBlank(message = "课程名称不能为空")
    private String courseName;
    private String cover;

    @NotNull(message = "一级分类不能为空")
    private Long categoryId;
    private Long category2Id;
    private String intro;
    private String description;

    @NotNull(message = "课程价格不能为空")
    @Min(value = 0, message = "价格不能为负数")
    @Digits(integer = 7, fraction = 2, message = "价格最多两位小数")
    private BigDecimal price;
    private BigDecimal originalPrice;

    @NotNull(message = "是否免费不能为空")
    private Integer isFree;

    @NotNull(message = "难度等级不能为空")
    private Integer difficulty;
    private String tags;
}
