package com.lyf.supplychain.common.feign.order;

import lombok.Data;

/**
 * 订单物流回调请求。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
public class OrderLogisticsCallbackRequest {

    private String orderNo;

    private String trackingNo;

    private String waybillNo;

    private Integer logisticsStatus;
}
