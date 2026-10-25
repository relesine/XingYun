package com.xingyun.vo;

import com.xingyun.entity.Course;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/** 课程详情视图：课程信息 + 讲师信息 + 章节目录 + 当前用户学习/购买状态 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CourseDetailVO extends Course {
    private String teacherName;
    private String teacherTitle;
    private String teacherAvatar;
    private String teacherIntro;
    private String categoryName;
    private String category2Name;

    /** 当前用户是否已购买（免费课恒为 true） */
    private Boolean purchased;
    /** 当前用户课程总进度 0-100 */
    private Integer courseProgress;
    /** 已完成课时数 */
    private Integer finishedLessons;
    /** 章节目录 */
    private List<ChapterVO> chapters;
}
