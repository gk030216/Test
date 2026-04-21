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

        // ========== 1. AI相关接口 ==========
        if (requestUri.startsWith("/api/ai/")) {
            return true;
        }

        // ========== 上传接口（注册时可上传头像） ==========
        if (requestUri.equals("/api/upload/avatar")) {
            return true;
        }

        // ========== 2. 系统设置接口 ==========
        if (requestUri.equals("/api/admin/settings")) {
            return true;
        }

        // ========== 3. 轮播图接口 ==========
        if (requestUri.equals("/api/carousel/list")) {
            return true;
        }

        // ========== 4. 服务相关接口 ==========
        if (requestUri.equals("/api/service/category/list")) {
            return true;
        }
        if (requestUri.startsWith("/api/service/list")) {
            return true;
        }
        if (requestUri.matches("/api/service/\\d+")) {
            return true;
        }
        if (requestUri.equals("/api/service/hot") || requestUri.equals("/api/service/hot/list")) {
            return true;
        }

        // 服务评价列表
        if (requestUri.matches("/api/service/comment/service/\\d+")) {
            return true;
        }
        // 服务评价统计
        if (requestUri.matches("/api/service/comment/service/\\d+/stats")) {
            return true;
        }

        // ========== 5. 商品相关接口（商城） ==========
        // 商品列表
        if (requestUri.equals("/api/product/list") || requestUri.startsWith("/api/product/list")) {
            return true;
        }
        // 商品分页
        if (requestUri.equals("/api/product/page") || requestUri.startsWith("/api/product/page")) {
            return true;
        }
        // 商品详情 /api/product/123
        if (requestUri.matches("/api/product/\\d+")) {
            return true;
        }
        // 热门商品
        if (requestUri.equals("/api/product/hot")) {
            return true;
        }
        // 新品商品
        if (requestUri.equals("/api/product/new")) {
            return true;
        }
        // 商品分类列表
        if (requestUri.equals("/api/admin/category/all") || requestUri.equals("/api/category/all")) {
            return true;
        }
        // 商品分类列表
        if (requestUri.equals("/api/product/category/list")) {
            return true;
        }
        // 商品分类详情
        if (requestUri.matches("/api/product/category/\\d+")) {
            return true;
        }
        // 商品搜索
        if (requestUri.startsWith("/api/product/search")) {
            return true;
        }

        // 商品评价接口
        if (requestUri.matches("/api/comment/product/\\d+")) {
            return true;
        }

        if (requestUri.matches("/api/comment/product/\\d+/stats")) {
            return true;
        }

        // ========== 6. 社区相关接口 ==========
        if (requestUri.equals("/api/community/posts") || requestUri.equals("/api/community/post/list")) {
            return true;
        }
        if (requestUri.matches("/api/community/post/\\d+")) {
            return true;
        }

        // ========== 7. 公告相关接口 ==========
        if (requestUri.equals("/api/consultation-notice/home")) {
            return true;
        }
        if (requestUri.equals("/api/consultation-notice/list")) {
            return true;
        }
        if (requestUri.matches("/api/consultation-notice/\\d+")) {
            return true;
        }
        if (requestUri.equals("/api/consultation-notice/top")) {
            return true;
        }

        // ========== 8. 用户认证相关接口 ==========
        if (requestUri.equals("/api/user/login") ||
                requestUri.equals("/api/user/register") ||
                requestUri.equals("/api/user/reset-password") ||
                requestUri.equals("/api/user/check-username") ||
                requestUri.equals("/api/user/check-email")) {
            return true;
        }

        // ========== 9. 验证码接口 ==========
        if (requestUri.startsWith("/api/code/")) {
            return true;
        }

        // ========== 10. 支付回调接口 ==========
        if (requestUri.equals("/api/pay/alipay/notify")) {
            return true;
        }

        // ========== 需要登录验证的接口 ==========
        String token = request.getHeader("Authorization");

        if (token == null || token.isEmpty()) {
            sendErrorResponse(response, 401, "未登录，请先登录");
            return false;
        }

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (!JwtUtil.validateToken(token)) {
            sendErrorResponse(response, 401, "Token无效或已过期，请重新登录");
            return false;
        }

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