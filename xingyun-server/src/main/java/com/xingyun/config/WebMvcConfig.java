package com.xingyun.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xingyun.security.JwtInterceptor;
import com.xingyun.security.JwtUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * Web MVC 配置：跨域、JWT 拦截器注册、上传文件静态资源映射。
 * 防盗链说明：封面/头像/课件目录直接映射成静态资源；视频目录【故意不映射】，
 * 视频统一走 VideoController 的签名校验接口输出流，杜绝直接拼链接盗链。
 *
 * @author xingyun
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Resource
    private JwtUtils jwtUtils;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private ObjectMapper objectMapper;

    @Value("${xingyun.upload.path}")
    private String uploadPath;

    /** 跨域白名单，生产通过 XINGYUN_CORS_ORIGINS 注入真实前端域名 */
    @Value("${xingyun.cors.allowed-origins}")
    private String allowedOrigins;

    /** 跨域配置：仅放行白名单来源，避免 allowCredentials + 任意源带来的凭证泄露风险 */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins = allowedOrigins.split(",");
        for (int i = 0; i < origins.length; i++) {
            origins[i] = origins[i].trim();
        }
        registry.addMapping("/**")
                .allowedOriginPatterns(origins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("X-Request-Id")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new JwtInterceptor(jwtUtils, stringRedisTemplate, objectMapper))
                .addPathPatterns("/**")
                // 静态资源与错误页不拦截（视频目录没有做静态映射，因此不会在这里被放行）
                .excludePathPatterns("/upload/cover/**", "/upload/avatar/**", "/upload/ware/**", "/error");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 仅映射可公开访问的目录；注意末尾必须有分隔符 /
        registry.addResourceHandler("/upload/cover/**").addResourceLocations("file:" + uploadPath + "/cover/");
        registry.addResourceHandler("/upload/avatar/**").addResourceLocations("file:" + uploadPath + "/avatar/");
        registry.addResourceHandler("/upload/ware/**").addResourceLocations("file:" + uploadPath + "/ware/");
    }
}
