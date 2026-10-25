package com.xingyun.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

/** 课程订单 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("course_order")
public class CourseOrder extends BaseEntity {

    private String orderNo;
    private Long userId;
    private Long courseId;
    private BigDecimal originalAmount;
    private BigDecimal discountAmount;
    private BigDecimal payAmount;
    private Long couponId;
    /** 1微信 2支付宝 */
    private Integer payType;
    /** 0待支付 1已支付 2已取消 3已退款 */
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date payTime;

    /** 课程名（非数据库列，查询时联表填充，便于前端展示） */
    @TableField(exist = false)
    private String courseName;
}
