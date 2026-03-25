package com.pet.config;

import com.pet.interceptor.JwtInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new JwtInterceptor())
                .addPathPatterns("/api/**")  // 拦截所有 /api/** 请求
                .excludePathPatterns(
                        "/api/user/login",           // 登录接口不拦截
                        "/api/user/register",        // 注册接口不拦截
                        "/api/user/check-username",  // 检查用户名不拦截
                        "/api/user/check-email",     // 检查邮箱不拦截
                        "/api/code/register",        // 发送注册验证码不拦截
                        "/api/code/forget"           // 发送找回密码验证码不拦截
                );
    }
}