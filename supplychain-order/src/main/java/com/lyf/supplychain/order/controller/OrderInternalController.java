package com.lyf.supplychain.order.controller;

import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.common.feign.order.OrderLogisticsCallbackRequest;
import com.lyf.supplychain.common.feign.order.OrderOutboundCallbackRequest;
import com.lyf.supplychain.order.service.OrderMainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单内部边界接口。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@RestController
@RequestMapping("/internal/oms")
public class OrderInternalController {

    private static final Logger log = LoggerFactory.getLogger(OrderInternalController.class);

    private final OrderMainService orderMainService;

    public OrderInternalController(OrderMainService orderMainService) {
        this.orderMainService = orderMainService;
    }

    /**
     * 接收 WMS 出库完成回调。
     *
     * @param request 出库回调请求
     * @return 无数据响应
     */
    @PostMapping("/outbound/callback")
    public R<Void> outboundCallback(@RequestBody OrderOutboundCallbackRequest request) {
        log.info("OMS收到WMS出库完成回调，orderNo={}, outboundNo={}", request.getOrderNo(), request.getOutboundNo());
        orderMainService.outboundCallback(request.getOrderNo(), request.getOutboundNo());
        return R.ok();
    }

    /**
     * 接收 TMS 物流状态回调。
     *
     * @param request 物流回调请求
     * @return 无数据响应
     */
    @PostMapping("/logistics/callback")
    public R<Void> logisticsCallback(@RequestBody OrderLogisticsCallbackRequest request) {
        log.info("OMS收到TMS物流回调，orderNo={}, trackingNo={}, status={}",
                request.getOrderNo(), request.getTrackingNo(), request.getLogisticsStatus());
        orderMainService.logisticsCallback(request.getOrderNo(), request.getWaybillNo(),
                request.getTrackingNo(), request.getLogisticsStatus());
        return R.ok();
    }
}
