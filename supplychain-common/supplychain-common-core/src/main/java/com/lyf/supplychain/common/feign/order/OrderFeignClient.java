package com.lyf.supplychain.common.feign.order;

import com.lyf.supplychain.common.api.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 订单服务 Feign 客户端。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@FeignClient(name = "supplychain-order", path = "/internal/oms")
public interface OrderFeignClient {

    /**
     * 接收 WMS 出库完成回调。
     *
     * @param request 出库回调请求
     * @return 无数据响应
     */
    @PostMapping("/outbound/callback")
    R<Void> outboundCallback(@RequestBody OrderOutboundCallbackRequest request);

    /**
     * 接收 TMS 物流状态回调。
     *
     * @param request 物流回调请求
     * @return 无数据响应
     */
    @PostMapping("/logistics/callback")
    R<Void> logisticsCallback(@RequestBody OrderLogisticsCallbackRequest request);
}
