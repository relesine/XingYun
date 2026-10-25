package com.xingyun.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 课件实体（PPT/PDF/源码等） */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("course_ware")
public class CourseWare extends BaseEntity {
    private Long courseId;
    private Long lessonId;
    private String wareName;
    private String filePath;
    private Long fileSize;
    private String fileType;
    private Integer downloadCount;
}
