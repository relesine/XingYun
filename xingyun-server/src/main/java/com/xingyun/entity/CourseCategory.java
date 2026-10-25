package com.xingyun.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 课程分类（两级：parent_id=0 为一级分类）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("course_category")
public class CourseCategory extends BaseEntity {
    private String name;
    private Long parentId;
    private Integer level;
    private String icon;
    private Integer sort;
    /** 0启用 1停用 */
    private Integer status;
}
