package com.xingyun.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 用户实体（学员 / 讲师 / 管理员统一一张表）。
 *
 * @author xingyun
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    private String username;

    /** 密码只允许写入（登录/修改），序列化返回前端时不输出，避免泄露密文 */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private String nickname;
    private String realName;
    private Integer gender;
    private String avatar;
    private String email;
    private String phone;
    /** 1学员 2讲师 3管理员 */
    private Integer userType;
    private String title;
    private String intro;
    private Integer studyMinutes;
    private Integer integral;
    /** 0正常 1禁用 */
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date lastLoginTime;

    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;
}
