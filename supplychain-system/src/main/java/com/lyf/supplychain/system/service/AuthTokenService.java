package com.lyf.supplychain.system.service;

import com.lyf.supplychain.common.security.model.LoginUser;

/**
 * 认证令牌服务。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
public interface AuthTokenService {

    /**
     * 使用登录用户创建 Sa-Token 会话。
     *
     * @param loginUser 登录用户
     * @return 令牌值
     */
    String login(LoginUser loginUser);

    /**
     * 签发刷新令牌。
     *
     * @param loginUser 登录用户
     * @return 刷新令牌
     */
    String issueRefreshToken(LoginUser loginUser);

    /**
     * 解析刷新令牌对应的用户ID。
     *
     * @param refreshToken 刷新令牌
     * @return 用户ID
     */
    Long resolveRefreshUserId(String refreshToken);

    /**
     * 吊销刷新令牌。
     *
     * @param refreshToken 刷新令牌
     */
    void revokeRefreshToken(String refreshToken);

    /**
     * 退出当前登录会话。
     */
    void logout();

    /**
     * 获取当前 Token 名称。
     *
     * @return Token 请求头名称
     */
    String getTokenName();

    /**
     * 获取当前登录用户快照。
     *
     * @return 登录用户
     */
    LoginUser getLoginUser();
}
