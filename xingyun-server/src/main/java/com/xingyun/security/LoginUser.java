package com.xingyun.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 登录成功后缓存在 Token / Redis 中的当前登录用户信息（轻量，不放密码等敏感字段）。
 *
 * @author xingyun
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser implements Serializable {

    private Long id;
    private String username;
    private String nickname;
    private Integer userType;          // 1学员 2讲师 3管理员
    private List<String> roles;        // 角色编码集合
    private String avatar;

    /** 是否管理员 */
    public boolean isAdmin() {
        return userType != null && userType == 3;
    }

    /** 是否讲师 */
    public boolean isTeacher() {
        return userType != null && userType == 2;
    }
}
