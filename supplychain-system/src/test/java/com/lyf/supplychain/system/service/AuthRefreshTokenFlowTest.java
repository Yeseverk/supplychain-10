package com.lyf.supplychain.system.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.lyf.supplychain.common.exception.BusinessException;
import com.lyf.supplychain.common.security.model.LoginUser;
import com.lyf.supplychain.system.constant.SystemConstants;
import com.lyf.supplychain.system.entity.SysTenant;
import com.lyf.supplychain.system.entity.SysUser;
import com.lyf.supplychain.system.mapper.SysTenantMapper;
import com.lyf.supplychain.system.mapper.SysUserMapper;
import com.lyf.supplychain.system.model.auth.AuthLoginRequest;
import com.lyf.supplychain.system.model.auth.AuthLoginVO;
import com.lyf.supplychain.system.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 双令牌刷新流程测试。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
class AuthRefreshTokenFlowTest {

    @Test
    void loginShouldReturnRefreshTokenAndRefreshShouldRotateToken() {
        TokenState tokenState = new TokenState();
        AuthService authService = authService(tokenState, user(SystemConstants.STATUS_ENABLED), tenant(SystemConstants.TENANT_STATUS_ENABLED));

        AuthLoginVO login = authService.login(request(), "127.0.0.1");
        AuthLoginVO refreshed = authService.refresh(login.getRefreshToken());

        assertThat(login.getTokenValue()).isEqualTo("access-1");
        assertThat(login.getRefreshToken()).isEqualTo("refresh-1");
        assertThat(refreshed.getTokenValue()).isEqualTo("access-2");
        assertThat(refreshed.getRefreshToken()).isEqualTo("refresh-2");
        assertThat(tokenState.revokedTokens).contains("refresh-1");
    }

    @Test
    void refreshShouldRejectDisabledUser() {
        TokenState tokenState = new TokenState();
        tokenState.nextRefreshUserId = 501L;
        AuthService authService = authService(tokenState, user(SystemConstants.STATUS_DISABLED), tenant(SystemConstants.TENANT_STATUS_ENABLED));

        assertThatThrownBy(() -> authService.refresh("refresh-1"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("账号已禁用");
    }

    private AuthService authService(TokenState tokenState, SysUser user, SysTenant tenant) {
        return new AuthServiceImpl(
                userMapper(user),
                tenantMapper(tenant),
                rbacPermissionService(),
                authTokenService(tokenState));
    }

    private AuthLoginRequest request() {
        AuthLoginRequest request = new AuthLoginRequest();
        request.setTenantCode("TC001");
        request.setUsername("purchase01");
        request.setPassword("123456");
        return request;
    }

    private SysTenant tenant(Integer status) {
        SysTenant tenant = new SysTenant();
        tenant.setId(101L);
        tenant.setTenantCode("TC001");
        tenant.setPlanType(2);
        tenant.setStatus(status);
        return tenant;
    }

    private SysUser user(Integer status) {
        SysUser user = new SysUser();
        user.setId(501L);
        user.setTenantId(101L);
        user.setUsername("purchase01");
        user.setPassword(new BCryptPasswordEncoder().encode("123456"));
        user.setRealName("采购专员");
        user.setUserType(1);
        user.setStatus(status);
        user.setLoginFailCount(0);
        return user;
    }

    private SysTenantMapper tenantMapper(SysTenant tenant) {
        return proxy(SysTenantMapper.class, (proxy, method, args) -> {
            if ("selectOne".equals(method.getName()) || "selectById".equals(method.getName())) {
                return tenant;
            }
            return defaultValue(method.getReturnType());
        });
    }

    private SysUserMapper userMapper(SysUser user) {
        return proxy(SysUserMapper.class, (proxy, method, args) -> {
            if ("selectOne".equals(method.getName()) || "selectById".equals(method.getName())) {
                return user;
            }
            if ("updateById".equals(method.getName())) {
                return 1;
            }
            return defaultValue(method.getReturnType());
        });
    }

    private RbacPermissionService rbacPermissionService() {
        return new RbacPermissionService() {
            @Override
            public List<String> listPermissions(Long tenantId, Long userId) {
                return List.of("srm:supplier:list");
            }

            @Override
            public List<String> listRoles(Long tenantId, Long userId) {
                return List.of("ROLE_PURCHASE");
            }

            @Override
            public Integer dataScope(Long tenantId, Long userId) {
                return 3;
            }

            @Override
            public void evictUserPermissionCache(Long tenantId, Long userId) {
            }

            @Override
            public void evictRolePermissionCache(Long tenantId, Long roleId) {
            }
        };
    }

    private AuthTokenService authTokenService(TokenState state) {
        return new AuthTokenService() {
            @Override
            public String login(LoginUser loginUser) {
                state.loginCount++;
                return "access-" + state.loginCount;
            }

            @Override
            public String issueRefreshToken(LoginUser loginUser) {
                state.refreshCount++;
                state.nextRefreshUserId = loginUser.getUserId();
                return "refresh-" + state.refreshCount;
            }

            @Override
            public Long resolveRefreshUserId(String refreshToken) {
                return state.nextRefreshUserId;
            }

            @Override
            public void revokeRefreshToken(String refreshToken) {
                state.revokedTokens.add(refreshToken);
            }

            @Override
            public void logout() {
            }

            @Override
            public String getTokenName() {
                return "Authorization";
            }

            @Override
            public LoginUser getLoginUser() {
                return null;
            }
        };
    }

    @SuppressWarnings("unchecked")
    private <T> T proxy(Class<T> type, InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler);
    }

    private Object defaultValue(Class<?> returnType) {
        if (Integer.TYPE == returnType) {
            return 0;
        }
        if (Boolean.TYPE == returnType) {
            return false;
        }
        if (Wrapper.class.isAssignableFrom(returnType)) {
            return null;
        }
        return null;
    }

    private static class TokenState {

        private int loginCount;

        private int refreshCount;

        private Long nextRefreshUserId;

        private final Set<String> revokedTokens = new HashSet<>();
    }
}
