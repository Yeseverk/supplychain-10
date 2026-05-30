package com.lyf.supplychain.common.security;

import com.lyf.supplychain.common.exception.BusinessException;
import com.lyf.supplychain.common.security.annotation.RequiresPermission;
import com.lyf.supplychain.common.security.aspect.RequiresPermissionAspect;
import com.lyf.supplychain.common.security.context.SecurityContextHolder;
import com.lyf.supplychain.common.security.model.LoginUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 权限注解切面单元测试。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
class RequiresPermissionAspectTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clear();
    }

    @Test
    void shouldAllowWhenUserHasPermission() {
        SecurityContextHolder.setLoginUser(LoginUser.builder()
                .userId(501L)
                .tenantId(101L)
                .permissions(List.of("srm:supplier:audit"))
                .build());
        DemoService proxy = proxy();

        proxy.audit();
    }

    @Test
    void shouldRejectWhenUserMissesPermission() {
        SecurityContextHolder.setLoginUser(LoginUser.builder()
                .userId(501L)
                .tenantId(101L)
                .permissions(List.of("srm:supplier:list"))
                .build());
        DemoService proxy = proxy();

        assertThatThrownBy(proxy::audit)
                .isInstanceOf(BusinessException.class)
                .hasMessage("无权限访问");
    }

    private DemoService proxy() {
        AspectJProxyFactory factory = new AspectJProxyFactory(new DemoService());
        factory.addAspect(new RequiresPermissionAspect());
        return factory.getProxy();
    }

    static class DemoService {

        @RequiresPermission("srm:supplier:audit")
        void audit() {
        }
    }
}
