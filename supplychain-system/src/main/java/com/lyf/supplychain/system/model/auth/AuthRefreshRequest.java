package com.lyf.supplychain.system.model.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 刷新访问令牌请求。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Data
public class AuthRefreshRequest {

    @NotBlank(message = "刷新令牌不能为空")
    private String refreshToken;
}
