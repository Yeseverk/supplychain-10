package com.lyf.supplychain.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * OMS 平台 Webhook 配置。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Data
@Component
@ConfigurationProperties(prefix = "supplychain.order.webhook")
public class OrderWebhookProperties {

    /**
     * 是否启用签名校验。
     */
    private Boolean signatureEnabled = true;

    /**
     * 未配置平台密钥时是否阻断请求。
     */
    private Boolean rejectMissingSecret = false;

    /**
     * 平台签名配置。
     */
    private Map<String, Platform> platforms = new HashMap<>();

    /**
     * 单个平台 Webhook 签名配置。
     */
    @Data
    public static class Platform {

        /**
         * 签名密钥。
         */
        private String secret;

        /**
         * 签名算法，目前支持 HMAC_SHA256。
         */
        private String algorithm = "HMAC_SHA256";

        /**
         * 签名编码格式：BASE64 或 HEX。
         */
        private String encoding = "HEX";
    }
}
