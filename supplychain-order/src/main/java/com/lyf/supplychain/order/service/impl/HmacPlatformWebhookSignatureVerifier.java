package com.lyf.supplychain.order.service.impl;

import com.lyf.supplychain.common.exception.BusinessException;
import com.lyf.supplychain.order.config.OrderWebhookProperties;
import com.lyf.supplychain.order.service.PlatformWebhookSignatureVerifier;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;

/**
 * 基于 HMAC 的平台 Webhook 签名校验器。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Component
public class HmacPlatformWebhookSignatureVerifier implements PlatformWebhookSignatureVerifier {

    private final OrderWebhookProperties properties;

    public HmacPlatformWebhookSignatureVerifier(OrderWebhookProperties properties) {
        this.properties = properties;
    }

    /**
     * 根据平台远程配置校验 Webhook 签名。
     *
     * @param platform  平台编码
     * @param rawData   原始报文
     * @param signature 平台签名
     */
    @Override
    public void verify(String platform, String rawData, String signature) {
        if (!Boolean.TRUE.equals(properties.getSignatureEnabled())) {
            return;
        }
        if (signature == null || signature.isBlank()) {
            BusinessException.throwException(15010, "Webhook 签名不能为空");
        }
        OrderWebhookProperties.Platform config = properties.getPlatforms().get(normalize(platform));
        if (config == null || config.getSecret() == null || config.getSecret().isBlank()) {
            if (Boolean.TRUE.equals(properties.getRejectMissingSecret())) {
                BusinessException.throwException(15010, "Webhook 平台密钥未配置");
            }
            return;
        }
        String expected = sign(rawData, config);
        if (!MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                signature.trim().getBytes(StandardCharsets.UTF_8))) {
            BusinessException.throwException(15010, "Webhook 签名验证失败");
        }
    }

    private String sign(String rawData, OrderWebhookProperties.Platform config) {
        try {
            String algorithm = normalize(config.getAlgorithm());
            String jdkAlgorithm = switch (algorithm) {
                case "HMAC_SHA256" -> "HmacSHA256";
                default -> throw new IllegalArgumentException("不支持的Webhook签名算法：" + config.getAlgorithm());
            };
            Mac mac = Mac.getInstance(jdkAlgorithm);
            mac.init(new SecretKeySpec(config.getSecret().getBytes(StandardCharsets.UTF_8), jdkAlgorithm));
            byte[] digest = mac.doFinal((rawData == null ? "" : rawData).getBytes(StandardCharsets.UTF_8));
            if ("BASE64".equals(normalize(config.getEncoding()))) {
                return Base64.getEncoder().encodeToString(digest);
            }
            return HexFormat.of().formatHex(digest);
        } catch (Exception exception) {
            BusinessException.throwException(15010, "Webhook 签名计算失败");
            return "";
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().replace("-", "_").toUpperCase(Locale.ROOT);
    }
}
