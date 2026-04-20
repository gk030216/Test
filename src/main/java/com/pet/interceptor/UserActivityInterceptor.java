package com.pet.interceptor;

import com.pet.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class UserActivityInterceptor implements HandlerInterceptor {

    @Autowired
    private UserMapper userMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 异步更新，不影响响应速度
        new Thread(() -> {
            try {
                Integer userId = (Integer) request.getAttribute("userId");
                if (userId != null) {
                    userMapper.updateLastLoginTime(userId);
                }
            } catch (Exception e) {
                // 忽略更新失败，不影响正常请求
            }
        }).start();
        return true;
    }
}