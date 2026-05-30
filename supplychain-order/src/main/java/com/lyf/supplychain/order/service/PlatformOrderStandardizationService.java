package com.lyf.supplychain.order.service;

import com.lyf.supplychain.order.request.OrderCreateRequest;

/**
 * 平台订单标准化服务。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
public interface PlatformOrderStandardizationService {

    /**
     * 将平台原始订单报文转换为 OMS 标准创建请求。
     *
     * @param platform 平台编码
     * @param rawData  原始报文
     * @return 标准订单创建请求
     */
    OrderCreateRequest standardize(String platform, String rawData);
}
