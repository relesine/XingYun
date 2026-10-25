package com.xingyun.security;

/**
 * 当前登录用户上下文（基于 ThreadLocal）：拦截器解析出登录用户后存入，
 * 业务代码任意位置可用 UserContext.getUserId() 取当前用户，无需层层传参；
 * 请求结束后必须 remove，防止线程复用导致用户串号。
 *
 * @author xingyun
 */
public class UserContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    public static Long getUserId() {
        LoginUser u = HOLDER.get();
        return u == null ? null : u.getId();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
