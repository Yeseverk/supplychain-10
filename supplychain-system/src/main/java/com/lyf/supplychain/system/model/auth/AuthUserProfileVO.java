package com.lyf.supplychain.system.model.auth;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 当前登录用户资料响应。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
@Builder
public class AuthUserProfileVO {

    private Long userId;

    private Long tenantId;

    private String username;

    private String realName;

    private Integer planType;

    private List<String> roles;

    private List<String> permissions;
}
