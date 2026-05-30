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
import com.lyf.supplychain.system.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 登录失败锁定策略测试。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
class AuthLockPolicyTest {

    @Test
    void loginShouldLockUserForThirtyMinutesWhenPasswordFailsFiveTimes() {
        SysUser user = user(new BCryptPasswordEncoder().encode("123456"));
        user.setLoginFailCount(4);
        MapperState state = new MapperState(user);
        AuthService authService = authService(state);

        assertThatThrownBy(() -> authService.login(request("bad"), "127.0.0.1"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("密码错误次数过多，账号已锁定30分钟");

        assertThat(state.updatedUser.getStatus()).isEqualTo(SystemConstants.STATUS_LOCKED);
        assertThat(state.updatedUser.getLoginFailCount()).isEqualTo(5);
        assertThat(state.updatedUser.getLockTime()).isNotNull();
        assertThat(state.updatedUser.getLockUntilTime()).isAfter(state.updatedUser.getLockTime());
    }

    @Test
    void loginShouldAutoUnlockWhenLockTimeExpired() {
        SysUser user = user(new BCryptPasswordEncoder().encode("123456"));
        user.setStatus(SystemConstants.STATUS_LOCKED);
        user.setLoginFailCount(5);
        user.setLockTime(LocalDateTime.now().minusMinutes(31));
        user.setLockUntilTime(LocalDateTime.now().minusMinutes(1));
        MapperState state = new MapperState(user);
        AuthService authService = authService(state);

        authService.login(request("123456"), "127.0.0.1");

        assertThat(state.updatedUsers).anySatisfy(updated -> {
            assertThat(updated.getStatus()).isEqualTo(SystemConstants.STATUS_ENABLED);
            assertThat(updated.getLoginFailCount()).isZero();
            assertThat(updated.getLockTime()).isNull();
            assertThat(updated.getLockUntilTime()).isNull();
        });
    }

    private AuthService authService(MapperState state) {
        return new AuthServiceImpl(
                userMapper(state),
                tenantMapper(),
                rbacPermissionService(),
                authTokenService());
    }

    private AuthLoginRequest request(String password) {
        AuthLoginRequest request = new AuthLoginRequest();
        request.setTenantCode("TC001");
        request.setUsername("purchase01");
        request.setPassword(password);
        return request;
    }

    private SysUser user(String password) {
        SysUser user = new SysUser();
        user.setId(501L);
        user.setTenantId(101L);
        user.setUsername("purchase01");
        user.setPassword(password);
        user.setRealName("采购专员");
        user.setUserType(1);
        user.setStatus(SystemConstants.STATUS_ENABLED);
        user.setLoginFailCount(0);
        return user;
    }

    private SysTenantMapper tenantMapper() {
        return proxy(SysTenantMapper.class, (proxy, method, args) -> {
            if ("selectOne".equals(method.getName())) {
                SysTenant tenant = new SysTenant();
                tenant.setId(101L);
                tenant.setTenantCode("TC001");
                tenant.setPlanType(2);
                tenant.setStatus(SystemConstants.TENANT_STATUS_ENABLED);
                return tenant;
            }
            return defaultValue(method.getReturnType());
        });
    }

    private SysUserMapper userMapper(MapperState state) {
        return proxy(SysUserMapper.class, (proxy, method, args) -> {
            if ("selectOne".equals(method.getName())) {
                return state.user;
            }
            if ("updateById".equals(method.getName())) {
                SysUser update = (SysUser) args[0];
                state.updatedUser = update;
                state.updatedUsers.add(update);
                merge(state.user, update);
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

    private AuthTokenService authTokenService() {
        return new AuthTokenService() {
            @Override
            public String login(LoginUser loginUser) {
                return "token";
            }

            @Override
            public String issueRefreshToken(LoginUser loginUser) {
                return "refresh-token";
            }

            @Override
            public Long resolveRefreshUserId(String refreshToken) {
                return 501L;
            }

            @Override
            public void revokeRefreshToken(String refreshToken) {
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

    private void merge(SysUser user, SysUser update) {
        if (update.getStatus() != null) {
            user.setStatus(update.getStatus());
        }
        if (update.getLoginFailCount() != null) {
            user.setLoginFailCount(update.getLoginFailCount());
        }
        user.setLockTime(update.getLockTime());
        user.setLockUntilTime(update.getLockUntilTime());
    }

    private static class MapperState {

        private final SysUser user;

        private SysUser updatedUser;

        private final List<SysUser> updatedUsers = new java.util.ArrayList<>();

        private MapperState(SysUser user) {
            this.user = user;
        }
    }
}
