package com.lyf.supplychain.system.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.lyf.supplychain.common.exception.BusinessException;
import com.lyf.supplychain.system.entity.SysTenant;
import com.lyf.supplychain.system.entity.SysUser;
import com.lyf.supplychain.system.mapper.SysTenantMapper;
import com.lyf.supplychain.system.mapper.SysUserMapper;
import com.lyf.supplychain.system.model.auth.AuthLoginRequest;
import com.lyf.supplychain.system.model.auth.AuthLoginVO;
import com.lyf.supplychain.system.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 登录认证服务单元测试。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private SysUserMapper userMapper;

    @Mock
    private SysTenantMapper tenantMapper;

    @Mock
    private RbacPermissionService rbacPermissionService;

    @Mock
    private AuthTokenService authTokenService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(userMapper, tenantMapper, rbacPermissionService, authTokenService);
    }

    @Test
    void loginShouldReturnTokenAndPermissionsWhenPasswordMatches() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        SysTenant tenant = tenant();
        SysUser user = user(encoder.encode("123456"));
        when(tenantMapper.selectOne(any(Wrapper.class))).thenReturn(tenant);
        when(userMapper.selectOne(any(Wrapper.class))).thenReturn(user);
        when(rbacPermissionService.listPermissions(101L, 501L)).thenReturn(List.of("srm:supplier:list"));
        when(rbacPermissionService.listRoles(101L, 501L)).thenReturn(List.of("ROLE_PURCHASE"));
        when(authTokenService.login(any())).thenReturn("jwt-token");
        when(authTokenService.getTokenName()).thenReturn("Authorization");

        AuthLoginVO result = authService.login(request("purchase01", "123456", "TC001"), "127.0.0.1");

        assertThat(result.getTokenName()).isEqualTo("Authorization");
        assertThat(result.getTokenValue()).isEqualTo("jwt-token");
        assertThat(result.getTenantId()).isEqualTo(101L);
        assertThat(result.getUserId()).isEqualTo(501L);
        assertThat(result.getPermissions()).containsExactly("srm:supplier:list");
        verify(userMapper).updateById(any(SysUser.class));
    }

    @Test
    void loginShouldRejectDisabledTenant() {
        SysTenant tenant = tenant();
        tenant.setStatus(0);
        when(tenantMapper.selectOne(any(Wrapper.class))).thenReturn(tenant);

        assertThatThrownBy(() -> authService.login(request("purchase01", "123456", "TC001"), "127.0.0.1"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("租户已被禁用或注销");
    }

    @Test
    void loginShouldIncreaseFailCountWhenPasswordWrong() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        when(tenantMapper.selectOne(any(Wrapper.class))).thenReturn(tenant());
        when(userMapper.selectOne(any(Wrapper.class))).thenReturn(user(encoder.encode("123456")));

        assertThatThrownBy(() -> authService.login(request("purchase01", "bad", "TC001"), "127.0.0.1"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("密码错误");
        verify(userMapper).updateById(any(SysUser.class));
    }

    private AuthLoginRequest request(String username, String password, String tenantCode) {
        AuthLoginRequest request = new AuthLoginRequest();
        request.setUsername(username);
        request.setPassword(password);
        request.setTenantCode(tenantCode);
        return request;
    }

    private SysTenant tenant() {
        SysTenant tenant = new SysTenant();
        tenant.setId(101L);
        tenant.setTenantCode("TC001");
        tenant.setCompanyName("测试租户");
        tenant.setPlanType(2);
        tenant.setStatus(1);
        return tenant;
    }

    private SysUser user(String password) {
        SysUser user = new SysUser();
        user.setId(501L);
        user.setTenantId(101L);
        user.setUsername("purchase01");
        user.setPassword(password);
        user.setRealName("采购专员");
        user.setUserType(1);
        user.setStatus(1);
        user.setLoginFailCount(0);
        return user;
    }
}
