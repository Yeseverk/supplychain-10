package com.lyf.supplychain.system.model.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录认证请求。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
public class AuthLoginRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "租户编码不能为空")
    private String tenantCode;
}
