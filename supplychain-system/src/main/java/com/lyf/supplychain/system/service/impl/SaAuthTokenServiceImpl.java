package com.lyf.supplychain.system.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lyf.supplychain.common.redis.CommonRedisKeys;
import com.lyf.supplychain.common.security.constant.SecurityConstants;
import com.lyf.supplychain.common.security.context.SecurityContextHolder;
import com.lyf.supplychain.common.security.model.LoginUser;
import com.lyf.supplychain.system.service.AuthTokenService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

/**
 * 基于 Sa-Token 的认证令牌服务实现。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Service
public class SaAuthTokenServiceImpl implements AuthTokenService {

    private static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(7);

    private final StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper;

    public SaAuthTokenServiceImpl(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 创建登录态并把租户与权限快照写入 Sa-Token 会话。
     *
     * @param loginUser 登录用户
     * @return 令牌值
     */
    @Override
    public String login(LoginUser loginUser) {
        StpUtil.login(loginUser.getUserId());
        StpUtil.getSession().set(SecurityConstants.LOGIN_USER_SESSION_KEY, loginUser);
        StpUtil.getSession().set(SecurityConstants.SESSION_TENANT_ID, loginUser.getTenantId());
        StpUtil.getSession().set(SecurityConstants.SESSION_PLAN_TYPE, loginUser.getPlanType());
        StpUtil.getSession().set(SecurityConstants.SESSION_USERNAME, loginUser.getUsername());
        StpUtil.getSession().set(SecurityConstants.SESSION_REAL_NAME, loginUser.getRealName());
        return StpUtil.getTokenValue();
    }

    /**
     * 生成刷新令牌并写入 Redis。
     *
     * @param loginUser 登录用户
     * @return 刷新令牌
     */
    @Override
    public String issueRefreshToken(LoginUser loginUser) {
        String refreshToken = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(CommonRedisKeys.refreshToken(refreshToken),
                String.valueOf(loginUser.getUserId()), REFRESH_TOKEN_TTL);
        return refreshToken;
    }

    /**
     * 从 Redis 解析刷新令牌对应的用户ID。
     *
     * @param refreshToken 刷新令牌
     * @return 用户ID
     */
    @Override
    public Long resolveRefreshUserId(String refreshToken) {
        if (StrUtil.isBlank(refreshToken)) {
            return null;
        }
        String userId = redisTemplate.opsForValue().get(CommonRedisKeys.refreshToken(refreshToken));
        return StrUtil.isBlank(userId) ? null : Long.valueOf(userId);
    }

    /**
     * 删除刷新令牌。
     *
     * @param refreshToken 刷新令牌
     */
    @Override
    public void revokeRefreshToken(String refreshToken) {
        if (StrUtil.isBlank(refreshToken)) {
            return;
        }
        redisTemplate.delete(CommonRedisKeys.refreshToken(refreshToken));
    }

    /**
     * 退出当前登录态。
     */
    @Override
    public void logout() {
        StpUtil.logout();
    }

    /**
     * 获取前端应携带的 Token 请求头名称。
     *
     * @return Token 请求头名称
     */
    @Override
    public String getTokenName() {
        return StpUtil.getTokenName();
    }

    /**
     * 从 Sa-Token 会话读取当前登录用户快照。
     *
     * @return 当前登录用户
     */
    @Override
    public LoginUser getLoginUser() {
        LoginUser contextUser = SecurityContextHolder.getLoginUser();
        if (contextUser != null) {
            return contextUser;
        }
        try {
            SaSession session = StpUtil.getSession(false);
            Object value = session == null ? null : session.get(SecurityConstants.LOGIN_USER_SESSION_KEY);
            if (value == null) {
                return null;
            }
            if (value instanceof LoginUser loginUser) {
                return loginUser;
            }
            return objectMapper.convertValue(value, LoginUser.class);
        } catch (NotLoginException ignored) {
            return null;
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
