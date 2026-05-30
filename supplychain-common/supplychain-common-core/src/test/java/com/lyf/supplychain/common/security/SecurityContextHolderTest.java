package com.lyf.supplychain.common.security;

import com.lyf.supplychain.common.context.TenantContext;
import com.lyf.supplychain.common.security.context.SecurityContextHolder;
import com.lyf.supplychain.common.security.model.LoginUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 安全上下文单元测试。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
class SecurityContextHolderTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clear();
    }

    @Test
    void setLoginUserShouldAlsoPopulateTenantContext() {
        LoginUser loginUser = LoginUser.builder()
                .userId(501L)
                .tenantId(101L)
                .username("purchase01")
                .realName("采购专员")
                .planType(2)
                .permissions(List.of("srm:supplier:list", "pms:order:list"))
                .roles(List.of("ROLE_PURCHASE"))
                .build();

        SecurityContextHolder.setLoginUser(loginUser);

        assertThat(SecurityContextHolder.getLoginUser()).isSameAs(loginUser);
        assertThat(SecurityContextHolder.getUserId()).isEqualTo(501L);
        assertThat(SecurityContextHolder.getTenantId()).isEqualTo(101L);
        assertThat(SecurityContextHolder.hasPermission("srm:supplier:list")).isTrue();
        assertThat(SecurityContextHolder.hasPermission("srm:supplier:audit")).isFalse();
        assertThat(TenantContext.getTenantId()).isEqualTo(101L);
        assertThat(TenantContext.getUserId()).isEqualTo(501L);
    }

    @Test
    void clearShouldRemoveLoginUserAndTenantContext() {
        SecurityContextHolder.setLoginUser(LoginUser.builder()
                .userId(501L)
                .tenantId(101L)
                .permissions(List.of("*"))
                .build());

        SecurityContextHolder.clear();

        assertThat(SecurityContextHolder.getLoginUser()).isNull();
        assertThat(SecurityContextHolder.getUserId()).isNull();
        assertThat(SecurityContextHolder.getTenantId()).isNull();
        assertThat(TenantContext.getTenantId()).isNull();
        assertThat(TenantContext.getUserId()).isNull();
    }
}
