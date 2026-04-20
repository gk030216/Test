package com.pet.interceptor;

import com.pet.util.JwtUtil;
import com.pet.util.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

public class JwtInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String requestUri = request.getRequestURI();

        // 放行 AI 相关接口
        if (requestUri.startsWith("/api/ai/")) {
            return true;
        }

        // 放行系统设置接口（不需要登录）
        if (requestUri.equals("/api/admin/settings")) {
            return true;
        }

        // 放行轮播图接口（不需要登录）
        if (requestUri.equals("/api/carousel/list")) {
            return true;
        }

        // 放行服务分类接口（前台不需要登录）
        if (requestUri.equals("/api/service/category/list")) {
            return true;
        }

        // 放行服务列表接口（前台不需要登录）
        if (requestUri.startsWith("/api/service/list") || requestUri.matches("/api/service/\\d+")) {
            return true;
        }

        // 放行热门服务接口（前台不需要登录）
        if (requestUri.equals("/api/service/hot")) {
            return true;
        }

        // 放行商品相关接口（前台不需要登录）
        if (requestUri.startsWith("/api/product/list") ||
                requestUri.matches("/api/product/\\d+") ||
                requestUri.equals("/api/product/hot") ||
                requestUri.equals("/api/product/new")) {
            return true;
        }

        // 放行社区帖子列表（不需要登录）
        if (requestUri.equals("/api/community/posts")) {
            return true;
        }

        // 放行登录注册等接口
        if (requestUri.equals("/api/user/login") ||
                requestUri.equals("/api/user/register") ||
                requestUri.equals("/api/user/reset-password") ||
                requestUri.equals("/api/user/check-username") ||
                requestUri.equals("/api/user/check-email") ||
                requestUri.startsWith("/api/code/") ||
                requestUri.equals("/api/pay/alipay/notify")) {
            return true;
        }

        // 获取请求头中的 token
        String token = request.getHeader("Authorization");

        // 检查 token 是否存在
        if (token == null || token.isEmpty()) {
            sendErrorResponse(response, 401, "未登录，请先登录");
            return false;
        }

        // 移除 Bearer 前缀（如果有）
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // 验证 token
        if (!JwtUtil.validateToken(token)) {
            sendErrorResponse(response, 401, "Token无效或已过期，请重新登录");
            return false;
        }

        // 将用户信息存入请求属性中
        Integer userId = JwtUtil.getUserIdFromToken(token);
        String username = JwtUtil.getUsernameFromToken(token);
        Integer role = JwtUtil.getRoleFromToken(token);

        request.setAttribute("userId", userId);
        request.setAttribute("username", username);
        request.setAttribute("role", role);

        return true;
    }

    private void sendErrorResponse(HttpServletResponse response, int code, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        Result<?> result = Result.error(code, message);
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(result);

        PrintWriter writer = response.getWriter();
        writer.write(json);
        writer.flush();
        writer.close();
    }
}