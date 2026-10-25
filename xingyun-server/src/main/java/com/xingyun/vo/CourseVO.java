package com.xingyun.vo;

import com.xingyun.entity.Course;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 课程列表/卡片视图对象：在课程字段基础上补充讲师名、分类名等联表字段。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CourseVO extends Course {
    /** 授课讲师昵称 */
    private String teacherName;
    /** 讲师头衔 */
    private String teacherTitle;
    /** 讲师头像 */
    private String teacherAvatar;
    /** 一级分类名 */
    private String categoryName;
    /** 二级分类名 */
    private String category2Name;
}
