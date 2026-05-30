package com.lyf.supplychain.order.service;

/**
 * 平台 Webhook 签名校验器。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
public interface PlatformWebhookSignatureVerifier {

    /**
     * 校验平台 Webhook 签名。
     *
     * @param platform  平台编码
     * @param rawData   原始报文
     * @param signature 平台签名
     */
    void verify(String platform, String rawData, String signature);
}
