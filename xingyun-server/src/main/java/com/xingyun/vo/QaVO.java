package com.xingyun.vo;

import com.xingyun.entity.CourseQa;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 问答视图：补充提问人、回复人昵称 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QaVO extends CourseQa {
    private String askerName;
    private String askerAvatar;
    private String replierName;
    /** 所属课程名（联表填充，方便讲师端展示） */
    private String courseName;
}
