package com.xingyun.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/** 注册请求参数（注册后默认是学员身份） */
@Data
public class RegisterDTO {

    @NotBlank(message = "账号不能为空")
    @Size(min = 4, max = 20, message = "账号长度需在4-20位之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度需在6-20位之间")
    private String password;

    private String nickname;
    private String phone;
    private String email;
}
