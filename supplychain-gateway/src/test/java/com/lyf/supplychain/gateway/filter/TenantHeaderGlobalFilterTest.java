package com.lyf.supplychain.gateway.filter;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 租户与用户请求头清洗过滤器测试。
 *
 * @author liyunfei
 * @date 2026-05-15
 */
class TenantHeaderGlobalFilterTest {

    @Test
    void filterShouldRemoveClientForgedIdentityHeaders() {
        TenantHeaderGlobalFilter filter = new TenantHeaderGlobalFilter();
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/system/users/1")
                .header("X-Tenant-Id", "101")
                .header("X-User-Id", "501")
                .header("X-Username", "attacker")
                .header("X-Internal-Request", "supplychain")
                .header("X-Tenant-Code", "TC-20260528-7012")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        AtomicReference<ServerWebExchange> capturedExchange = new AtomicReference<>();
        GatewayFilterChain chain = nextExchange -> {
            capturedExchange.set(nextExchange);
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();

        assertThat(capturedExchange.get().getRequest().getHeaders().getFirst("X-Tenant-Id")).isNull();
        assertThat(capturedExchange.get().getRequest().getHeaders().getFirst("X-User-Id")).isNull();
        assertThat(capturedExchange.get().getRequest().getHeaders().getFirst("X-Username")).isNull();
        assertThat(capturedExchange.get().getRequest().getHeaders().getFirst("X-Internal-Request")).isNull();
        assertThat(capturedExchange.get().getRequest().getHeaders().getFirst("X-Tenant-Code")).isEqualTo("TC-20260528-7012");
    }
}
