package com.lyf.supplychain.common.security.interceptor;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lyf.supplychain.common.context.TenantContext;
import com.lyf.supplychain.common.security.constant.SecurityConstants;
import com.lyf.supplychain.common.security.context.SecurityContextHolder;
import com.lyf.supplychain.common.security.model.LoginUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 请求级安全上下文拦截器。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Component
public class SecurityContextInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper;

    public SecurityContextInterceptor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 从 Sa-Token 会话恢复登录用户上下文。
     *
     * @param request  HTTP请求
     * @param response HTTP响应
     * @param handler  处理器
     * @return 是否放行
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (isInternalRequest(request)) {
            restoreInternalContext(request);
            return true;
        }
        try {
            if (StpUtil.isLogin()) {
                Object value = StpUtil.getSession().get(SecurityConstants.LOGIN_USER_SESSION_KEY);
                SecurityContextHolder.setLoginUser(resolveLoginUser(value));
            }
        } catch (NotLoginException ignored) {
            // 未登录请求交给具体权限注解或控制器处理，这里只负责恢复上下文
        }
        return true;
    }

    /**
     * 请求完成后清理线程上下文。
     *
     * @param request  HTTP请求
     * @param response HTTP响应
     * @param handler  处理器
     * @param ex       异常
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        SecurityContextHolder.clear();
    }

    private boolean isInternalRequest(HttpServletRequest request) {
        return SecurityConstants.INTERNAL_REQUEST_VALUE.equals(request.getHeader(SecurityConstants.INTERNAL_REQUEST_HEADER));
    }

    private void restoreInternalContext(HttpServletRequest request) {
        Long tenantId = parseLongHeader(request, SecurityConstants.TENANT_ID_HEADER);
        Long userId = parseLongHeader(request, SecurityConstants.USER_ID_HEADER);
        if (tenantId != null || userId != null) {
            TenantContext.set(tenantId, userId);
        }
    }

    private Long parseLongHeader(HttpServletRequest request, String headerName) {
        String value = request.getHeader(headerName);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private LoginUser resolveLoginUser(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LoginUser loginUser) {
            return loginUser;
        }
        return objectMapper.convertValue(value, LoginUser.class);
    }
}
