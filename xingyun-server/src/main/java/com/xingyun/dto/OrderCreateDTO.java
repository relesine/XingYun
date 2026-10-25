package com.xingyun.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/** 创建订单参数（couponId 可空，表示不使用优惠券） */
@Data
public class OrderCreateDTO {

    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    private Long userCouponId;
}
