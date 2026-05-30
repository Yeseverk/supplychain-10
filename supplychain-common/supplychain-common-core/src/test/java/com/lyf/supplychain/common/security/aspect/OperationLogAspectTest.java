package com.lyf.supplychain.common.security.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.common.feign.system.SystemAuditLogFeignClient;
import com.lyf.supplychain.common.feign.system.SystemAuditLogRecordRequest;
import com.lyf.supplychain.common.security.annotation.OperationLog;
import com.lyf.supplychain.common.security.context.SecurityContextHolder;
import com.lyf.supplychain.common.security.model.LoginUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 操作审计日志切面单元测试。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
class OperationLogAspectTest {

    private final List<SystemAuditLogRecordRequest> records = new ArrayList<>();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clear();
    }

    @Test
    void recordShouldMaskSensitiveParamsAndSendAuditLog() {
        SecurityContextHolder.setLoginUser(LoginUser.builder()
                .tenantId(101L)
                .userId(501L)
                .username("finance01")
                .build());
        DemoService proxy = proxy();

        R<String> result = proxy.pay("PAY-001", "secret-token");

        assertThat(result.getData()).isEqualTo("ok");
        assertThat(records).hasSize(1);
        SystemAuditLogRecordRequest record = records.get(0);
        assertThat(record.getTenantId()).isEqualTo(101L);
        assertThat(record.getUsername()).isEqualTo("finance01");
        assertThat(record.getModule()).isEqualTo("财务应付");
        assertThat(record.getAction()).isEqualTo("确认付款");
        assertThat(record.getRequestParams()).contains("******");
        assertThat(record.getRequestParams()).doesNotContain("secret-token");
        assertThat(record.getStatus()).isEqualTo(1);
        assertThat(record.getResponseCode()).isEqualTo(200);
    }

    private DemoService proxy() {
        AspectJProxyFactory factory = new AspectJProxyFactory(new DemoService());
        factory.addAspect(new OperationLogAspect(auditClient(), new ObjectMapper(), Runnable::run));
        return factory.getProxy();
    }

    private SystemAuditLogFeignClient auditClient() {
        return request -> {
            records.add(request);
            return R.ok(1L);
        };
    }

    static class DemoService {

        @OperationLog(module = "财务应付", action = "确认付款")
        R<String> pay(String payableNo, String token) {
            return R.ok("ok");
        }
    }
}
