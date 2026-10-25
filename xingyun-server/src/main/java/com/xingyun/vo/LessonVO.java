package com.xingyun.vo;

import com.xingyun.entity.CourseLesson;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 课时视图：补充当前用户是否有权播放、学习进度与续播位置 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LessonVO extends CourseLesson {
    /** 当前用户是否可播放（免费/试看/已购） */
    private Boolean canPlay;
    /** 不可播放原因（如：付费内容，请先购买） */
    private String lockReason;
    /** 学习进度 0-100 */
    private Integer progress;
    /** 断点续播位置（秒） */
    private Integer positionSeconds;
}
