package com.livehouse.config;

import com.livehouse.common.result.Result;
import com.livehouse.util.JsonUtils;
import com.livehouse.util.JwtUtil;
import com.livehouse.vo.UserVO;
import com.livehouse.service.SysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * JWT权限拦截器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    @Lazy
    @Resource
    SysUserService sysUserService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //跨域 OPTIONS请求直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        // 从请求头获取Token
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED.value(), "请先登录");
            return false;
        }

        // 移除Bearer前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // 验证Token
        if (!jwtUtil.validateToken(token)) {
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED.value(), "Token已过期或无效");
            return false;
        }

        // 获取用户ID
        Long userId = jwtUtil.getUserId(token);
        if (userId == null) {
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED.value(), "用户不存在");
            return false;
        }

        // 获取用户信息并放入请求属性
        try {
            UserVO userVO = sysUserService.getUserInfo(userId);
            request.setAttribute("user", userVO);
            request.setAttribute("userId", userId);
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED.value(), "用户信息获取失败");
            return false;
        }

        return true;
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws Exception {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        Result<Object> result = Result.error(message);
        response.getWriter().write(JsonUtils.toJsonString(result));
    }
}
