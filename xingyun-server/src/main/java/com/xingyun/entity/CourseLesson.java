package com.xingyun.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 课时实体（一章多节，按 sort 排序）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("course_lesson")
public class CourseLesson extends BaseEntity {
    private Long courseId;
    private String chapter;
    private String lessonName;
    private Long videoId;
    /** 时长（秒） */
    private Integer duration;
    private Integer sort;
    /** 1免费试看 0否 */
    private Integer isFreeTrial;
    private Integer status;
}
