package com.lyf.supplychain.logistics.controller;

import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.logistics.request.WebhookTrackRequest;
import com.lyf.supplychain.logistics.service.LogisticsService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 物流商轨迹 Webhook 控制器。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@RestController
public class LogisticsWebhookController {

    private final LogisticsService logisticsService;

    public LogisticsWebhookController(LogisticsService logisticsService) {
        this.logisticsService = logisticsService;
    }

    /**
     * 接收 DHL 轨迹推送。
     *
     * @param request 轨迹请求
     * @return 无数据响应
     */
    @PostMapping({"/api/tms/webhook/dhl", "/tms/webhook/dhl"})
    public R<Void> dhl(@Valid @RequestBody WebhookTrackRequest request) {
        logisticsService.webhookTrack("DHL", request);
        return R.ok();
    }

    /**
     * 接收 FedEx 轨迹推送。
     *
     * @param request 轨迹请求
     * @return 无数据响应
     */
    @PostMapping({"/api/tms/webhook/fedex", "/tms/webhook/fedex"})
    public R<Void> fedex(@Valid @RequestBody WebhookTrackRequest request) {
        logisticsService.webhookTrack("FEDEX", request);
        return R.ok();
    }
}
