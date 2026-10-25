package com.xingyun.vo;

import com.xingyun.entity.Course;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** "我的课程"视图：课程信息 + 学习进度 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MyCourseVO extends Course {
    /** 课程整体进度 0-100 */
    private Integer courseProgress;
    /** 已完成课时数 */
    private Integer finishedLessons;
    /** 最近学习时间 */
    private String lastLearnTime;
    /** 获取来源：免费学习 / 已购买 */
    private String source;
}
