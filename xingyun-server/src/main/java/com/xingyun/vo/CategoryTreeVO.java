package com.xingyun.vo;

import com.xingyun.entity.CourseCategory;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/** 分类树节点：分类自身字段 + 子分类集合 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CategoryTreeVO extends CourseCategory {
    private List<CourseCategory> children = new ArrayList<>();
}
