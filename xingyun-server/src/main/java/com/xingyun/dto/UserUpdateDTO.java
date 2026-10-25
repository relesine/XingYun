package com.xingyun.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/** 修改个人资料参数 */
@Data
public class UserUpdateDTO {

    @Size(max = 30, message = "昵称最长30个字符")
    private String nickname;

    private Integer gender;

    @Size(max = 255, message = "头像地址过长")
    private String avatar;

    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱最长100个字符")
    private String email;

    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Size(max = 200, message = "个人简介最长200字")
    private String intro;

    @Size(max = 50, message = "头衔最长50字")
    private String title;
}
