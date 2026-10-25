package com.xingyun.vo;

import com.xingyun.entity.Certificate;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 证书视图：补充课程名、学员姓名 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CertificateVO extends Certificate {
    private String courseName;
    private String studentName;
    private String teacherName;
}
