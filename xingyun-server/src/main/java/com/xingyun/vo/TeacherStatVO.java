package com.xingyun.vo;

import lombok.Data;

/** 讲师个人数据统计 */
@Data
public class TeacherStatVO {
    /** 我的课程数 */
    private Long myCourseCount;
    /** 课程累计学员数 */
    private Long totalStudent;
    /** 累计播放量 */
    private Long totalView;
    /** 待批改作业数 */
    private Long pendingGrade;
    /** 待回复问答数 */
    private Long pendingQa;
    /** 累计订单收入 */
    private String totalIncome;
}
