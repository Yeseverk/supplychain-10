package com.lyf.supplychain.system.model.auth;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 登录认证响应。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
@Builder
public class AuthLoginVO {

    private String tokenName;

    private String tokenValue;

    private String refreshToken;

    private Long refreshTokenExpireSeconds;

    private Long userId;

    private Long tenantId;

    private String tenantCode;

    private String username;

    private String realName;

    private Integer planType;

    private List<String> roles;

    private List<String> permissions;
}
