package com.lyf.supplychain.common.feign.order;

import lombok.Data;

import java.time.LocalDate;

/**
 * 订单出库回调请求。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Data
public class OrderOutboundCallbackRequest {

    private Long outboundId;

    private String outboundNo;

    private Long orderId;

    private String orderNo;

    private LocalDate outboundDate;
}
