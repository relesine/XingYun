package com.xingyun.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/** 优惠券新增/修改参数 */
@Data
public class CouponDTO {
    private Long id;

    @NotBlank(message = "优惠券名称不能为空")
    private String couponName;

    @NotNull(message = "类型不能为空")
    private Integer couponType;

    @Min(value = 0, message = "使用门槛不能为负数")
    private BigDecimal threshold = BigDecimal.ZERO;

    @NotNull(message = "优惠值不能为空")
    private BigDecimal discountValue;

    @Min(value = 0, message = "发放总量不能为负数")
    private Integer totalCount = 0;

    @Min(value = 1, message = "有效天数至少1天")
    private Integer validDays = 30;

    private Integer status = 1;
}
