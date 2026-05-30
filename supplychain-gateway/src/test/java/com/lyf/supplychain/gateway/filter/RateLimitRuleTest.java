package com.lyf.supplychain.gateway.filter;

import com.lyf.supplychain.gateway.config.GatewaySecurityProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 网关限流规则单元测试。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
class RateLimitRuleTest {

    @Test
    void resolveLimitShouldUseSpecialRuleBeforeDefaultRule() {
        GatewaySecurityProperties properties = new GatewaySecurityProperties();

        assertThat(properties.resolveLimit("/api/auth/login")).isEqualTo(5);
        assertThat(properties.resolveLimit("/api/srm/suppliers/1/certs/upload")).isEqualTo(3);
        assertThat(properties.resolveLimit("/api/bi/dashboard/ai-query")).isEqualTo(10);
        assertThat(properties.resolveLimit("/api/pms/orders")).isEqualTo(120);
    }
}
