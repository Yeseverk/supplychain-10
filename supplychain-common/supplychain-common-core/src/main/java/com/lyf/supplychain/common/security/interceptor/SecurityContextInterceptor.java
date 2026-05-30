package com.lyf.supplychain.common.security.interceptor;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lyf.supplychain.common.context.TenantContext;
import com.lyf.supplychain.common.security.constant.SecurityConstants;
import com.lyf.supplychain.common.security.context.SecurityContextHolder;
import com.lyf.supplychain.common.security.model.LoginUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * ????????????
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Slf4j
@Component
public class SecurityContextInterceptor implements HandlerInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    private final ObjectMapper objectMapper;

    public SecurityContextInterceptor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * ? Sa-Token ????????????
     *
     * @param request  HTTP??
     * @param response HTTP??
     * @param handler  ???
     * @return ????
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (isInternalRequest(request)) {
            restoreInternalContext(request);
            return true;
        }
        LoginUser loginUser = restoreLoginUser(request);
        if (loginUser != null) {
            SecurityContextHolder.setLoginUser(loginUser);
        }
        return true;
    }

    /**
     * ?????????????
     *
     * @param request  HTTP??
     * @param response HTTP??
     * @param handler  ???
     * @param ex       ??
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        SecurityContextHolder.clear();
    }

    private LoginUser restoreLoginUser(HttpServletRequest request) {
        LoginUser loginUser = restoreFromCurrentSession();
        if (loginUser != null) {
            return loginUser;
        }
        return restoreFromTokenFallback(request);
    }

    private LoginUser restoreFromCurrentSession() {
        try {
            if (!StpUtil.isLogin()) {
                return null;
            }
            return readLoginUser(StpUtil.getSession(false));
        } catch (NotLoginException ignored) {
            return null;
        } catch (Exception exception) {
            log.warn("Restore login user from Sa-Token current session failed", exception);
            return null;
        }
    }

    private LoginUser restoreFromTokenFallback(HttpServletRequest request) {
        String tokenValue = resolveTokenValue(request);
        if (StrUtil.isBlank(tokenValue)) {
            return null;
        }
        LoginUser loginUser = restoreFromTokenApi(tokenValue);
        if (loginUser != null) {
            return loginUser;
        }
        try {
            return restoreFromTokenDaoFallback(tokenValue, SaManager.getSaTokenDao());
        } catch (Exception exception) {
            log.warn("Restore login user from Sa-Token dao fallback failed", exception);
            return null;
        }
    }

    private LoginUser restoreFromTokenApi(String tokenValue) {
        try {
            Object loginId = StpUtil.getLoginIdByToken(tokenValue);
            if (loginId == null) {
                return null;
            }
            return readLoginUser(StpUtil.getSessionByLoginId(loginId, false));
        } catch (Exception exception) {
            log.warn("Restore login user from Sa-Token token api failed", exception);
            return null;
        }
    }

    LoginUser restoreFromTokenDaoFallback(String tokenValue, SaTokenDao saTokenDao) {
        if (StrUtil.isBlank(tokenValue) || saTokenDao == null) {
            return null;
        }
        StpLogic stpLogic = StpUtil.getStpLogic();
        Object loginId = readLoginId(saTokenDao, stpLogic.splicingKeyTokenValue(tokenValue));
        if (loginId == null) {
            return null;
        }
        SaSession session = saTokenDao.getSession(stpLogic.splicingKeySession(loginId));
        return readLoginUser(session);
    }

    private Object readLoginId(SaTokenDao saTokenDao, String tokenKey) {
        String loginId = saTokenDao.get(tokenKey);
        if (StrUtil.isNotBlank(loginId)) {
            return loginId;
        }
        return saTokenDao.getObject(tokenKey);
    }

    private LoginUser readLoginUser(SaSession session) {
        if (session == null) {
            return null;
        }
        return resolveLoginUser(session.get(SecurityConstants.LOGIN_USER_SESSION_KEY));
    }

    private String resolveTokenValue(HttpServletRequest request) {
        String tokenValue = null;
        try {
            tokenValue = StpUtil.getTokenValue();
        } catch (Exception ignored) {
            // Fall back to reading the configured token header from the raw request.
        }
        if (StrUtil.isBlank(tokenValue)) {
            tokenValue = request.getHeader(resolveTokenName());
        }
        if (StrUtil.isBlank(tokenValue)) {
            return null;
        }
        String trimmedValue = tokenValue.trim();
        if (StrUtil.startWithIgnoreCase(trimmedValue, BEARER_PREFIX)) {
            return trimmedValue.substring(BEARER_PREFIX.length()).trim();
        }
        return trimmedValue;
    }

    private String resolveTokenName() {
        try {
            String tokenName = StpUtil.getTokenName();
            if (StrUtil.isNotBlank(tokenName)) {
                return tokenName;
            }
        } catch (Exception ignored) {
            // Use the project default when Sa-Token has not completed initialization.
        }
        return SecurityConstants.AUTHORIZATION_HEADER;
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
        try {
            return objectMapper.convertValue(value, LoginUser.class);
        } catch (IllegalArgumentException exception) {
            log.warn("Convert Sa-Token loginUser session data failed", exception);
            return null;
        }
    }
}
