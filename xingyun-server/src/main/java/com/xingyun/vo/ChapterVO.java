package com.xingyun.vo;

import lombok.Data;

import java.util.List;

/** 章节目录视图：一章对应多节课 */
@Data
public class ChapterVO {
    /** 章节名 */
    private String chapter;
    /** 该章下的课时 */
    private List<LessonVO> lessons;
}
