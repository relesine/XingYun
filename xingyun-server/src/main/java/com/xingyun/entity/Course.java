package com.xingyun.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 课程实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("course")
public class Course extends BaseEntity {

    private String courseName;
    private String cover;
    private Long categoryId;
    private Long category2Id;
    private Long teacherId;
    private String intro;
    private String description;

    /** 销售价 */
    private BigDecimal price;
    /** 划线原价 */
    private BigDecimal originalPrice;
    /** 1免费 0付费 */
    private Integer isFree;
    /** 1入门 2进阶 3高级 */
    private Integer difficulty;

    private Integer lessonCount;
    private Integer totalDuration;
    private Integer studentCount;
    private Integer viewCount;
    private BigDecimal score;
    private String tags;
    private Integer isRecommend;

    /** 0草稿 1待审核 2通过 3驳回 */
    private Integer auditStatus;
    private String rejectReason;
    /** 0下架 1上架 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private Long createBy;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;
}
