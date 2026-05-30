package com.lyf.supplychain.order.controller;

import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.order.request.WebhookRequest;
import com.lyf.supplychain.order.service.OrderMainService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 平台 Webhook 接口。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@RestController
@RequestMapping({"/api/oms/webhook", "/oms/webhook"})
public class OrderWebhookController {

    private final OrderMainService orderService;

    public OrderWebhookController(OrderMainService orderService) {
        this.orderService = orderService;
    }

    /**
     * 亚马逊 Webhook 接收。
     *
     * @param request Webhook 请求
     * @return 无数据响应
     */
    @PostMapping("/amazon")
    public R<Void> amazon(@Valid @RequestBody WebhookRequest request,
                          @RequestHeader Map<String, String> headers) {
        fillSignature(request, headers);
        orderService.webhook("AMAZON", request);
        return R.ok();
    }

    /**
     * TikTok Webhook 接收。
     *
     * @param request Webhook 请求
     * @return 无数据响应
     */
    @PostMapping("/tiktok")
    public R<Void> tiktok(@Valid @RequestBody WebhookRequest request,
                          @RequestHeader Map<String, String> headers) {
        fillSignature(request, headers);
        orderService.webhook("TIKTOK", request);
        return R.ok();
    }

    /**
     * Shopify Webhook 接收。
     *
     * @param request Webhook 请求
     * @param headers 请求头
     * @return 无数据响应
     */
    @PostMapping("/shopify")
    public R<Void> shopify(@Valid @RequestBody WebhookRequest request,
                           @RequestHeader Map<String, String> headers) {
        fillSignature(request, headers);
        orderService.webhook("SHOPIFY", request);
        return R.ok();
    }

    /**
     * eBay Webhook 接收。
     *
     * @param request Webhook 请求
     * @param headers 请求头
     * @return 无数据响应
     */
    @PostMapping("/ebay")
    public R<Void> ebay(@Valid @RequestBody WebhookRequest request,
                        @RequestHeader Map<String, String> headers) {
        fillSignature(request, headers);
        orderService.webhook("EBAY", request);
        return R.ok();
    }

    private void fillSignature(WebhookRequest request, Map<String, String> headers) {
        if (request.getSignature() != null && !request.getSignature().isBlank()) {
            return;
        }
        request.setSignature(firstHeader(headers,
                "X-Shopify-Hmac-SHA256",
                "X-Tiktok-Signature",
                "X-Signature",
                "Authorization"));
    }

    private String firstHeader(Map<String, String> headers, String... names) {
        for (String name : names) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(name)) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }
}
