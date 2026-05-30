package com.lyf.supplychain.order.service;

import com.lyf.supplychain.order.model.PlatformOrderPullContext;
import com.lyf.supplychain.order.model.PlatformPulledOrder;

import java.util.List;

/**
 * 平台订单主动拉取客户端。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
public interface PlatformOrderPullClient {

    /**
     * 当前客户端支持的平台编码。
     *
     * @return 平台编码
     */
    String platform();

    /**
     * 从外部平台拉取原始订单。
     *
     * @param context 拉取上下文
     * @return 原始订单列表
     */
    List<PlatformPulledOrder> pullOrders(PlatformOrderPullContext context);
}
