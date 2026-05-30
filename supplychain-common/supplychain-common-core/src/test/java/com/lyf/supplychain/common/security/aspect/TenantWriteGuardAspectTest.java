package com.lyf.supplychain.common.security.aspect;

import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.common.constant.ResultCode;
import com.lyf.supplychain.common.context.TenantContext;
import com.lyf.supplychain.common.exception.BusinessException;
import com.lyf.supplychain.common.feign.system.SystemTenantWriteCheckRequest;
import com.lyf.supplychain.common.feign.system.SystemTenantWriteCheckResponse;
import com.lyf.supplychain.common.feign.system.SystemTenantWriteFeignClient;
import com.lyf.supplychain.common.security.annotation.TenantWriteGuard;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 租户写操作保护切面单元测试。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
class TenantWriteGuardAspectTest {

    private final List<SystemTenantWriteCheckRequest> requests = new ArrayList<>();
    private boolean readonly;

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void writeOperationShouldContinueWhenTenantCanWrite() {
        TenantContext.set(101L, 501L);
        DemoService proxy = proxy();

        String result = proxy.createOrder();

        assertThat(result).isEqualTo("ok");
        assertThat(requests).hasSize(1);
        assertThat(requests.get(0).getTenantId()).isEqualTo(101L);
        assertThat(requests.get(0).getScene()).isEqualTo("创建订单");
    }

    @Test
    void writeOperationShouldRejectWhenTenantReadonly() {
        TenantContext.set(101L, 501L);
        readonly = true;
        DemoService proxy = proxy();

        assertThatThrownBy(proxy::createOrder)
                .isInstanceOf(BusinessException.class)
                .hasMessage("租户套餐已到期，当前仅允许查看历史数据");
    }

    private DemoService proxy() {
        AspectJProxyFactory factory = new AspectJProxyFactory(new DemoService());
        factory.addAspect(new TenantWriteGuardAspect(feignClient()));
        return factory.getProxy();
    }

    private SystemTenantWriteFeignClient feignClient() {
        return request -> {
            requests.add(request);
            if (readonly) {
                return R.fail(ResultCode.FORBIDDEN.getCode(), "租户套餐已到期，当前仅允许查看历史数据");
            }
            SystemTenantWriteCheckResponse response = new SystemTenantWriteCheckResponse();
            response.setTenantId(request.getTenantId());
            response.setCanWrite(true);
            return R.ok(response);
        };
    }

    static class DemoService {

        @TenantWriteGuard(scene = "创建订单")
        String createOrder() {
            return "ok";
        }
    }
}
