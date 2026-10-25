package com.xingyun.config;

import com.xingyun.security.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * 全局访问日志过滤器（审计追踪）：
 * 记录每个 API 请求的 方法、路径、来源 IP、操作人、耗时，便于问题排查与安全审计。
 * 操作人直接从 Authorization 令牌解析（无效令牌记 "-"，不影响请求本身）。
 * 只记录 /api 开头的业务请求，静态资源与流媒体不记录，避免日志刷屏。
 *
 * @author xingyun
 */
@Slf4j
@Component
@Order(1)
public class AccessLogFilter implements Filter {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        String uri = req.getRequestURI();
        // 链路追踪 ID：优先复用上游传入的 X-Request-Id，便于 Nginx/网关全链路串联
        String traceId = req.getHeader("X-Request-Id");
        if (!StringUtils.hasText(traceId)) {
            traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        }
        MDC.put("traceId", traceId);
        resp.setHeader("X-Request-Id", traceId);
        try {
            // 只记录业务接口，跳过视频流（高频分片请求）与静态资源
            if (uri.startsWith("/api/") && !uri.startsWith("/api/video/stream")) {
                long start = System.currentTimeMillis();
                try {
                    chain.doFilter(request, response);
                } finally {
                    long cost = System.currentTimeMillis() - start;
                    log.info("访问日志 method={} uri={} ip={} user={} status={} cost={}ms",
                            req.getMethod(), uri, clientIp(req), currentUserId(req),
                            resp.getStatus(), cost);
                }
            } else {
                chain.doFilter(request, response);
            }
        } finally {
            MDC.remove("traceId");
        }
    }

    /** 从 Authorization 头解析当前操作人 ID（解析失败返回 "-"，绝不阻断请求） */
    private String currentUserId(HttpServletRequest req) {
        String auth = req.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            return "-";
        }
        try {
            return String.valueOf(jwtUtils.getUserId(auth.substring(7)));
        } catch (Exception ignore) {
            return "-";
        }
    }

    /** 取真实客户端 IP（兼容代理 X-Forwarded-For） */
    private String clientIp(HttpServletRequest req) {
        String forwarded = req.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return req.getRemoteAddr();
    }
}
