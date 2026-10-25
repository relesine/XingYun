package com.xingyun.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

/** 管理端数据大盘统计结果 */
@Data
public class DashboardVO {
    /** 学员数 */
    private Long studentCount;
    /** 讲师数 */
    private Long teacherCount;
    /** 课程数 */
    private Long courseCount;
    /** 待审核课程数 */
    private Long pendingCount;
    /** 订单总数 */
    private Long orderCount;
    /** 销售总额 */
    private String totalRevenue;
    /** 累计学习时长（分钟） */
    private Long totalStudyMinutes;
    /** 各分类课程数量分布（分类名 -> 数量），用于饼图 */
    private List<Map<String, Object>> categoryDist;
    /** 近 7 天新增学员（日期 -> 数量），用于折线图 */
    private List<Map<String, Object>> weekNewUsers;
}
