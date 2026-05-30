package com.lyf.supplychain.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 网关安全配置属性。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
@Component
@ConfigurationProperties(prefix = "supplychain.gateway.security")
public class GatewaySecurityProperties {

    private boolean enabled = true;

    private List<String> whiteList = new ArrayList<>(List.of(
            "/api/auth/login",
            "/api/auth/refresh",
            "/api/saas/tenants/register",
            "/actuator/health",
            "/actuator/info",
            "/internal/",
            "/webhook/"
    ));

    private RateLimit rateLimit = new RateLimit();

    /**
     * 根据请求路径解析分钟级限流阈值。
     *
     * @param path 请求路径
     * @return 限流阈值
     */
    public int resolveLimit(String path) {
        if (path == null) {
            return rateLimit.getDefaultLimit();
        }
        if (path.contains("/api/auth/login")) {
            return rateLimit.getLoginLimit();
        }
        if (path.contains("/upload")) {
            return rateLimit.getUploadLimit();
        }
        if (path.contains("/ai") || path.contains("/ai-query")) {
            return rateLimit.getAiLimit();
        }
        return rateLimit.getDefaultLimit();
    }

    /**
     * 判断请求路径是否在白名单内。
     *
     * @param path 请求路径
     * @return 是否白名单
     */
    public boolean isWhitePath(String path) {
        if (path == null) {
            return false;
        }
        return whiteList.stream().anyMatch(path::startsWith);
    }

    /**
     * 网关滑动窗口限流配置。
     *
     * @author liyunfei
     * @date 2026-05-20
     */
    @Data
    public static class RateLimit {

        private boolean enabled = true;

        private int windowSeconds = 60;

        private int loginLimit = 5;

        private int uploadLimit = 3;

        private int aiLimit = 10;

        private int defaultLimit = 120;
    }
}
