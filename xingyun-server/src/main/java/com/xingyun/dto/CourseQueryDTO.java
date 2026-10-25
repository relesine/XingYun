package com.xingyun.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/** 课程分页/筛选查询参数 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CourseQueryDTO extends PageQuery {
    /** 关键字（课程名模糊） */
    private String keyword;
    /** 一级分类 */
    private Long categoryId;
    /** 二级分类 */
    private Long category2Id;
    /** 难度 */
    private Integer difficulty;
    /** 是否免费 */
    private Integer isFree;
    /** 排序：hot 最热 / new 最新 / price_asc 价格升序 */
    private String sort;
    /** 审核状态（管理端用） */
    private Integer auditStatus;
    /** 讲师ID（讲师端用） */
    private Long teacherId;
}
