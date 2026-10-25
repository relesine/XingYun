package com.xingyun.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

/** 模拟支付参数（学习项目用模拟支付，无需真实微信/支付宝） */
@Data
public class PayDTO {

    @NotBlank(message = "订单号不能为空")
    private String orderNo;

    /** 1微信 2支付宝 */
    @Min(value = 1, message = "支付方式不合法")
    @Max(value = 2, message = "支付方式不合法")
    private Integer payType = 1;
}
