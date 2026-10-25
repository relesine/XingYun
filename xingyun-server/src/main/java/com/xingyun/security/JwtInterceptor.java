package com.xingyun.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xingyun.common.Constants;
import com.xingyun.common.Result;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * JWT 登录与角色鉴权拦截器（RBAC 接口底座）。
 * 职责：
 *   1. 白名单（登录注册、课程浏览等）直接放行；
 *   2. 其余请求必须携带有效 Token，并从 Redis 还原登录用户到 UserContext；
 *   3. /teacher/** 仅讲师/管理员可访问，/admin/** 仅管理员可访问。
 *
 * @author xingyun
 */
@Slf4j
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final AntPathMatcher matcher = new AntPathMatcher();

    /** 登录白名单（无需 token 也能访问） */
    private static final List<String> WHITE_LIST = List.of(
            "/auth/login",
            "/auth/register",
            "/course/page",        // 课程列表（游客可浏览）
            "/course/detail/**",   // 课程详情（游客可浏览）
            "/course/hot",
            "/category/tree",
            "/announcement/list",
            "/teacher/list",
            "/teacher/detail/**",
            // video 标签请求无法携带 Authorization 头，视频流仅凭时效 sign 鉴权（防盗链）
            "/video/stream",
            // 探活接口供负载均衡/K8s 调用，只暴露 health 状态，不含敏感明细
            "/actuator/health",
            "/actuator/health/**",
            "/actuator/info",
            "/error"
    );

    public JwtInterceptor(JwtUtils jwtUtils, StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.jwtUtils = jwtUtils;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        // 去掉 context-path(/api) 前缀，得到 /auth/login 这样的相对路径
        String contextPath = request.getContextPath();
        if (StringUtils.hasText(contextPath) && uri.startsWith(contextPath)) {
            uri = uri.substring(contextPath.length());
        }

        // OPTIONS 预检请求直接放行（跨域时浏览器会先发 OPTIONS）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        boolean isWhite = false;
        for (String pattern : WHITE_LIST) {
            if (matcher.match(pattern, uri)) {
                isWhite = true;
                break;
            }
        }

        // 1. 尝试解析 token（无论是否白名单，只要带了合法 token 就设置登录上下文，
        //    这样「课程详情」这类公开页在登录用户访问时也能带出购买状态/学习进度——即可选登录）
        String token = request.getHeader("Authorization");
        if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        LoginUser loginUser = null;
        if (StringUtils.hasText(token)) {
            try {
                Claims claims = jwtUtils.parse(token);
                Long userId = Long.valueOf(claims.getSubject());
                String json = redisTemplate.opsForValue().get(Constants.REDIS_TOKEN + userId);
                if (StringUtils.hasText(json)) {
                    loginUser = objectMapper.readValue(json, LoginUser.class);
                    UserContext.set(loginUser);
                }
            } catch (Exception ignored) {
                // 解析失败先不报错，若为受保护接口下面会拦
            }
        }

        // 2. 白名单直接放行（游客可访问；已登录则上面已附带登录态）
        if (isWhite) {
            return true;
        }

        // 3. 非白名单：必须已登录
        if (loginUser == null) {
            return writeError(response, Result.CODE_UNAUTHORIZED, "未登录或登录已过期，请先登录");
        }

        // 4. 基于 URL 前缀的角色鉴权
        Integer type = loginUser.getUserType();
        if (uri.startsWith("/admin") && (type == null || type != Constants.TYPE_ADMIN)) {
            return writeError(response, Result.CODE_FORBIDDEN, "无权访问：仅平台管理员可操作");
        }
        if (uri.startsWith("/teacher") && (type == null
                || (type != Constants.TYPE_TEACHER && type != Constants.TYPE_ADMIN))) {
            return writeError(response, Result.CODE_FORBIDDEN, "无权访问：仅认证讲师可操作");
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 必须清理 ThreadLocal，防止 Tomcat 线程复用造成用户身份串号
        UserContext.clear();
    }

    /** 直接向前端输出一段标准 JSON 错误 */
    private boolean writeError(HttpServletResponse response, int code, String msg) throws Exception {
        response.setStatus(200); // 业务状态用 body.code 表达，避免前端 axios 直接进 catch
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(Result.error(code, msg)));
        return false;
    }
}
