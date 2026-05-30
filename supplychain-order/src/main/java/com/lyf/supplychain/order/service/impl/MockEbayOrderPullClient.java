package com.lyf.supplychain.order.service.impl;

import com.lyf.supplychain.order.config.OrderPlatformPullProperties;
import com.lyf.supplychain.order.model.PlatformOrderPullContext;
import com.lyf.supplychain.order.model.PlatformPulledOrder;
import com.lyf.supplychain.order.service.PlatformOrderPullClient;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * eBay 订单拉取模拟客户端。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Component
public class MockEbayOrderPullClient implements PlatformOrderPullClient {

    private final OrderPlatformPullProperties properties;

    public MockEbayOrderPullClient(OrderPlatformPullProperties properties) {
        this.properties = properties;
    }

    /**
     * 当前客户端支持 eBay 平台。
     *
     * @return 平台编码
     */
    @Override
    public String platform() {
        return "EBAY";
    }

    /**
     * 模拟 eBay Trading API 拉单结果。
     *
     * @param context 拉取上下文
     * @return 原始订单列表
     */
    @Override
    public List<PlatformPulledOrder> pullOrders(PlatformOrderPullContext context) {
        if (!Boolean.TRUE.equals(properties.getMockEnabled())) {
            return List.of();
        }
        String orderNo = "EBAY-MOCK-" + context.getEndTime().toLocalDate();
        return List.of(PlatformPulledOrder.builder()
                .platform(platform())
                .platformOrderNo(orderNo)
                .rawData(mockRawData(orderNo))
                .build());
    }

    private String mockRawData(String orderNo) {
        return """
                {
                  "order_id": "%s",
                  "store_id": 2,
                  "warehouse_id": 1,
                  "currency": "USD",
                  "created_at": "2026-05-25T09:00:00",
                  "items": [
                    {
                      "sku_id": 1002,
                      "sku_code": "SKU-EBAY-1002",
                      "sku_name": "eBay模拟商品",
                      "quantity": 1,
                      "unit_price": "18.50"
                    }
                  ],
                  "address": {
                    "receiver_name": "eBay Buyer",
                    "phone": "10000000001",
                    "country_code": "US",
                    "country_name": "United States",
                    "city": "San Jose",
                    "address_line1": "2025 Market Street",
                    "zip_code": "95125"
                  }
                }
                """.formatted(orderNo);
    }
}
