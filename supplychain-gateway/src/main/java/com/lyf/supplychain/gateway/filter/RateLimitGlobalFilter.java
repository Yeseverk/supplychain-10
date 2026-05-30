package com.lyf.supplychain.gateway.filter;

import cn.hutool.core.util.StrUtil;
import com.lyf.supplychain.gateway.config.GatewaySecurityProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

/**
 * Redis 滑动窗口限流过滤器。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Component
public class RateLimitGlobalFilter implements GlobalFilter, Ordered {

    private static final RedisScript<Long> SLIDING_WINDOW_SCRIPT = RedisScript.of("""
            local key = KEYS[1]
            local now = tonumber(ARGV[1])
            local window = tonumber(ARGV[2])
            local limit = tonumber(ARGV[3])
            redis.call('ZREMRANGEBYSCORE', key, 0, now - window)
            local current = redis.call('ZCARD', key)
            if current >= limit then
                return 0
            end
            redis.call('ZADD', key, now, now)
            redis.call('EXPIRE', key, window)
            return 1
            """, Long.class);

    private final GatewaySecurityProperties properties;

    private final ReactiveStringRedisTemplate redisTemplate;

    public RateLimitGlobalFilter(GatewaySecurityProperties properties, ReactiveStringRedisTemplate redisTemplate) {
        this.properties = properties;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 对 API 请求执行分钟级滑动窗口限流。
     *
     * @param exchange 当前请求交换对象
     * @param chain    网关过滤器链
     * @return 过滤结果
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (!properties.getRateLimit().isEnabled() || !path.startsWith("/api/")) {
            return chain.filter(exchange);
        }
        String identity = resolveIdentity(exchange);
        String key = "security:rate-limit:" + identity + ":" + path;
        long now = System.currentTimeMillis();
        int windowMillis = (int) Duration.ofSeconds(properties.getRateLimit().getWindowSeconds()).toMillis();
        int limit = properties.resolveLimit(path);
        return redisTemplate.execute(SLIDING_WINDOW_SCRIPT, List.of(key), List.of(
                        String.valueOf(now),
                        String.valueOf(windowMillis),
                        String.valueOf(limit)))
                .next()
                .defaultIfEmpty(1L)
                .flatMap(allowed -> {
                    if (allowed == 1L) {
                        return chain.filter(exchange);
                    }
                    exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                    return exchange.getResponse().setComplete();
                });
    }

    /**
     * 限流过滤器优先级。
     *
     * @return 过滤器顺序
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 20;
    }

    private String resolveIdentity(ServerWebExchange exchange) {
        String authorization = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StrUtil.isNotBlank(authorization)) {
            return Integer.toHexString(authorization.hashCode());
        }
        return exchange.getRequest().getRemoteAddress() == null
                ? "anonymous"
                : exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
    }
}
