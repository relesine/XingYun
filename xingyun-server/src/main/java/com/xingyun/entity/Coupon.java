package com.xingyun.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/** 优惠券 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("coupon")
public class Coupon extends BaseEntity {
    private String couponName;
    /** 1满减 2立减 3折扣 */
    private Integer couponType;
    /** 使用门槛 */
    private BigDecimal threshold;
    /** 优惠值：满减/立减为金额，折扣如 8.5 表示 85 折 */
    private BigDecimal discountValue;
    private Integer totalCount;
    private Integer receiveCount;
    private Integer validDays;
    private Integer status;
}
