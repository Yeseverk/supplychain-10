package com.lyf.supplychain.order.service.impl;

import com.lyf.supplychain.order.config.OrderPlatformPullProperties;
import com.lyf.supplychain.order.model.PlatformOrderPullContext;
import com.lyf.supplychain.order.model.PlatformPulledOrder;
import com.lyf.supplychain.order.service.PlatformOrderPullClient;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Amazon 订单拉取模拟客户端。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Component
public class MockAmazonOrderPullClient implements PlatformOrderPullClient {

    private final OrderPlatformPullProperties properties;

    public MockAmazonOrderPullClient(OrderPlatformPullProperties properties) {
        this.properties = properties;
    }

    /**
     * 当前客户端支持 Amazon 平台。
     *
     * @return 平台编码
     */
    @Override
    public String platform() {
        return "AMAZON";
    }

    /**
     * 模拟 Amazon SP-API 拉单结果。
     *
     * @param context 拉取上下文
     * @return 原始订单列表
     */
    @Override
    public List<PlatformPulledOrder> pullOrders(PlatformOrderPullContext context) {
        if (!Boolean.TRUE.equals(properties.getMockEnabled())) {
            return List.of();
        }
        String orderNo = "AMZ-MOCK-" + context.getEndTime().toLocalDate();
        return List.of(PlatformPulledOrder.builder()
                .platform(platform())
                .platformOrderNo(orderNo)
                .rawData(mockRawData(orderNo))
                .build());
    }

    private String mockRawData(String orderNo) {
        return """
                {
                  "id": "%s",
                  "store_id": 1,
                  "warehouse_id": 1,
                  "currency": "USD",
                  "created_at": "2026-05-25T09:00:00",
                  "line_items": [
                    {
                      "sku_id": 1001,
                      "sku": "SKU-AMZ-1001",
                      "title": "Amazon模拟商品",
                      "quantity": 1,
                      "price": "29.99"
                    }
                  ],
                  "shipping_address": {
                    "name": "Amazon Buyer",
                    "phone": "10000000000",
                    "country_code": "US",
                    "country": "United States",
                    "city": "Seattle",
                    "address1": "1 Amazon Way",
                    "zip": "98109"
                  }
                }
                """.formatted(orderNo);
    }
}
