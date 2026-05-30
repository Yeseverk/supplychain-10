package com.lyf.supplychain.gateway.filter;

import cn.hutool.core.util.StrUtil;
import com.lyf.supplychain.gateway.config.GatewaySecurityProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 网关认证入口过滤器。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final GatewaySecurityProperties properties;

    public AuthGlobalFilter(GatewaySecurityProperties properties) {
        this.properties = properties;
    }

    /**
     * 对非白名单 API 做基础 Token 存在性校验。
     *
     * @param exchange 当前请求交换对象
     * @param chain    网关过滤器链
     * @return 过滤结果
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!properties.isEnabled()) {
            return chain.filter(exchange);
        }
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        if (properties.isWhitePath(path) || (!path.startsWith("/api/") && !path.startsWith("/system/"))) {
            return chain.filter(exchange);
        }
        String authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StrUtil.isBlank(authorization)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        return chain.filter(exchange);
    }

    /**
     * 认证过滤器优先级。
     *
     * @return 过滤器顺序
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }
}
