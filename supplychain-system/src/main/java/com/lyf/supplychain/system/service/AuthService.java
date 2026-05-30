package com.lyf.supplychain.system.service;

import com.lyf.supplychain.system.model.auth.AuthLoginRequest;
import com.lyf.supplychain.system.model.auth.AuthLoginVO;
import com.lyf.supplychain.system.model.auth.AuthUserProfileVO;

import java.util.List;

/**
 * 登录认证应用服务。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
public interface AuthService {

    /**
     * 用户登录并签发访问令牌。
     *
     * @param request 登录请求
     * @param clientIp 客户端IP
     * @return 登录响应
     */
    AuthLoginVO login(AuthLoginRequest request, String clientIp);

    /**
     * 使用刷新令牌换取新的访问令牌。
     *
     * @param refreshToken 刷新令牌
     * @return 登录响应
     */
    AuthLoginVO refresh(String refreshToken);

    /**
     * 退出当前登录会话。
     */
    void logout(String refreshToken);

    /**
     * 退出当前登录会话。
     */
    default void logout() {
        logout(null);
    }

    /**
     * 查询当前登录用户资料。
     *
     * @return 当前用户资料
     */
    AuthUserProfileVO profile();

    /**
     * 查询当前登录用户权限。
     *
     * @return 权限标识集合
     */
    List<String> permissions();
}
