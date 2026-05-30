package com.lyf.supplychain.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 租户与用户请求头清洗过滤器。
 *
 * @author liyunfei
 * @date 2026-05-15
 */
@Component
public class TenantHeaderGlobalFilter implements GlobalFilter, Ordered {

    private static final String TENANT_ID_HEADER = "X-Tenant-Id";

    private static final String USER_ID_HEADER = "X-User-Id";

    private static final String USERNAME_HEADER = "X-Username";

    private static final String INTERNAL_REQUEST_HEADER = "X-Internal-Request";

    /**
     * 移除客户端可伪造的身份头，避免绕过登录态进行租户或用户越权。
     *
     * @param exchange 当前网关交换对象
     * @param chain    网关过滤器链
     * @return 异步过滤结果
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpRequest mutatedRequest = request.mutate()
                .headers(headers -> {
                    headers.remove(TENANT_ID_HEADER);
                    headers.remove(USER_ID_HEADER);
                    headers.remove(USERNAME_HEADER);
                    headers.remove(INTERNAL_REQUEST_HEADER);
                })
                .build();
        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    /**
     * 设置过滤器优先级，保证租户头在路由转发前准备好。
     *
     * @return 过滤器顺序
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 100;
    }
}
