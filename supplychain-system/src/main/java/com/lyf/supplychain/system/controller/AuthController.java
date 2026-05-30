package com.lyf.supplychain.system.controller;

import cn.hutool.core.util.StrUtil;
import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.common.security.annotation.OperationLog;
import com.lyf.supplychain.system.model.auth.AuthLoginRequest;
import com.lyf.supplychain.system.model.auth.AuthLoginVO;
import com.lyf.supplychain.system.model.auth.AuthRefreshRequest;
import com.lyf.supplychain.system.model.auth.AuthUserProfileVO;
import com.lyf.supplychain.system.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 登录认证控制器。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 用户登录并返回访问令牌。
     *
     * @param request     登录请求
     * @param servletRequest HTTP请求
     * @return 登录响应
     */
    @PostMapping("/login")
    @OperationLog(module = "登录认证", action = "用户登录", type = OperationLog.Type.QUERY, sensitiveFields = {"password", "token", "secret"})
    public R<AuthLoginVO> login(@Valid @RequestBody AuthLoginRequest request, HttpServletRequest servletRequest) {
        return R.ok(authService.login(request, resolveClientIp(servletRequest)));
    }

    /**
     * 使用刷新令牌换取新的访问令牌。
     *
     * @param request 刷新请求
     * @return 登录响应
     */
    @PostMapping("/refresh")
    @OperationLog(module = "登录认证", action = "刷新访问令牌", type = OperationLog.Type.QUERY, sensitiveFields = {"refreshToken", "token", "secret"})
    public R<AuthLoginVO> refresh(@Valid @RequestBody AuthRefreshRequest request) {
        return R.ok(authService.refresh(request.getRefreshToken()));
    }

    /**
     * 退出当前登录会话。
     *
     * @param request 刷新令牌请求
     * @return 无数据响应
     */
    @PostMapping("/logout")
    @OperationLog(module = "登录认证", action = "用户退出", type = OperationLog.Type.UPDATE, saveParam = false)
    public R<Void> logout(@RequestBody(required = false) AuthRefreshRequest request) {
        authService.logout(request == null ? null : request.getRefreshToken());
        return R.ok();
    }

    /**
     * 查询当前登录用户资料。
     *
     * @return 用户资料
     */
    @GetMapping("/profile")
    public R<AuthUserProfileVO> profile() {
        return R.ok(authService.profile());
    }

    /**
     * 查询当前登录用户权限。
     *
     * @return 权限标识集合
     */
    @GetMapping("/permissions")
    public R<List<String>> permissions() {
        return R.ok(authService.permissions());
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (StrUtil.isNotBlank(forwardedFor)) {
            return StrUtil.subBefore(forwardedFor, ",", false);
        }
        return request.getRemoteAddr();
    }
}
