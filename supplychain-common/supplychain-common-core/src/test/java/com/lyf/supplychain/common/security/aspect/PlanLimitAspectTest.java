package com.lyf.supplychain.common.security.aspect;

import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.common.constant.ResultCode;
import com.lyf.supplychain.common.context.TenantContext;
import com.lyf.supplychain.common.exception.BusinessException;
import com.lyf.supplychain.common.feign.system.SystemPlanLimitCheckRequest;
import com.lyf.supplychain.common.feign.system.SystemPlanLimitCheckResponse;
import com.lyf.supplychain.common.feign.system.SystemPlanLimitFeignClient;
import com.lyf.supplychain.common.security.annotation.PlanLimit;
import com.lyf.supplychain.common.security.plan.PlanUsageProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 套餐限制切面单元测试。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
class PlanLimitAspectTest {

    private final List<SystemPlanLimitCheckRequest> requests = new ArrayList<>();
    private boolean reject;

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void checkPlanLimitShouldPassCurrentUsageToSystemService() {
        TenantContext.set(101L, 501L);
        DemoService proxy = proxy();

        String result = proxy.createSupplier();

        assertThat(result).isEqualTo("ok");
        assertThat(requests).hasSize(1);
        assertThat(requests.get(0).getTenantId()).isEqualTo(101L);
        assertThat(requests.get(0).getFeatureCode()).isEqualTo("supplier.max");
        assertThat(requests.get(0).getCurrentUsage()).isEqualTo(19);
    }

    @Test
    void checkPlanLimitShouldRejectWhenSystemServiceReturnsForbidden() {
        TenantContext.set(101L, 501L);
        reject = true;
        DemoService proxy = proxy();

        assertThatThrownBy(proxy::createSupplier)
                .isInstanceOf(BusinessException.class)
                .hasMessage("当前套餐最多支持20家供应商");
    }

    private DemoService proxy() {
        AspectJProxyFactory factory = new AspectJProxyFactory(new DemoService());
        factory.addAspect(new PlanLimitAspect(planClient(), List.of(usageProvider())));
        return factory.getProxy();
    }

    private SystemPlanLimitFeignClient planClient() {
        return request -> {
            requests.add(request);
            if (reject) {
                return R.fail(ResultCode.FORBIDDEN.getCode(), "当前套餐最多支持20家供应商");
            }
            SystemPlanLimitCheckResponse response = new SystemPlanLimitCheckResponse();
            response.setFeatureCode(request.getFeatureCode());
            response.setCurrentUsage(request.getCurrentUsage());
            return R.ok(response);
        };
    }

    private PlanUsageProvider usageProvider() {
        return new PlanUsageProvider() {
            @Override
            public boolean supports(String featureCode) {
                return "supplier.max".equals(featureCode);
            }

            @Override
            public Integer currentUsage(Long tenantId, String featureCode) {
                return 19;
            }
        };
    }

    static class DemoService {

        @PlanLimit(feature = "supplier.max", bizName = "供应商")
        String createSupplier() {
            return "ok";
        }
    }
}
