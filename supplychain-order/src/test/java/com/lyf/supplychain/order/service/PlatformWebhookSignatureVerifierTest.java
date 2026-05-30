package com.lyf.supplychain.order.service;

import com.lyf.supplychain.common.exception.BusinessException;
import com.lyf.supplychain.order.config.OrderWebhookProperties;
import com.lyf.supplychain.order.service.impl.HmacPlatformWebhookSignatureVerifier;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 平台 Webhook 签名校验器测试。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
class PlatformWebhookSignatureVerifierTest {

    @Test
    void verifyShouldPassBase64HmacSignature() throws Exception {
        OrderWebhookProperties properties = properties("SHOPIFY", "shopify-secret", "BASE64");
        PlatformWebhookSignatureVerifier verifier = new HmacPlatformWebhookSignatureVerifier(properties);
        String rawData = "{\"id\":\"SHP-001\"}";

        verifier.verify("SHOPIFY", rawData, sign(rawData, "shopify-secret", true));
    }

    @Test
    void verifyShouldRejectInvalidSignature() {
        OrderWebhookProperties properties = properties("TIKTOK", "tiktok-secret", "HEX");
        PlatformWebhookSignatureVerifier verifier = new HmacPlatformWebhookSignatureVerifier(properties);

        assertThatThrownBy(() -> verifier.verify("TIKTOK", "{\"id\":\"TT-001\"}", "bad-signature"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Webhook 签名验证失败");
    }

    private OrderWebhookProperties properties(String platform, String secret, String encoding) {
        OrderWebhookProperties properties = new OrderWebhookProperties();
        OrderWebhookProperties.Platform config = new OrderWebhookProperties.Platform();
        config.setSecret(secret);
        config.setAlgorithm("HMAC_SHA256");
        config.setEncoding(encoding);
        properties.getPlatforms().put(platform, config);
        return properties;
    }

    private String sign(String rawData, String secret, boolean base64) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] digest = mac.doFinal(rawData.getBytes(StandardCharsets.UTF_8));
        return base64 ? Base64.getEncoder().encodeToString(digest) : HexFormat.of().formatHex(digest);
    }
}
