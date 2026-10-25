package com.xingyun.vo;

import com.xingyun.security.LoginUser;
import lombok.Data;

/** 登录成功返回：token + 当前用户信息 */
@Data
public class LoginVO {
    /** 访问令牌 */
    private String token;
    /** 请求头前缀，固定 Bearer */
    private String tokenHead = "Bearer";
    /** 用户信息 */
    private LoginUser userInfo;
}
