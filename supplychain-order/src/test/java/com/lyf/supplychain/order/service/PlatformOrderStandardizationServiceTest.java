package com.lyf.supplychain.order.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lyf.supplychain.order.request.OrderCreateRequest;
import com.lyf.supplychain.order.service.impl.JsonPlatformOrderStandardizationService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 平台订单标准化服务测试。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
class PlatformOrderStandardizationServiceTest {

    @Test
    void standardizeShouldMapCommonPlatformOrderJson() {
        PlatformOrderStandardizationService service =
                new JsonPlatformOrderStandardizationService(new ObjectMapper());
        String rawData = """
                {
                  "id": "SHP-001",
                  "store_id": 7,
                  "warehouse_id": 3,
                  "currency": "USD",
                  "created_at": "2026-05-25T09:10:00",
                  "line_items": [
                    {
                      "sku_id": 1001,
                      "sku": "SKU-1001",
                      "title": "无线耳机",
                      "quantity": 2,
                      "price": "19.99",
                      "discount": "1.00"
                    }
                  ],
                  "shipping_address": {
                    "name": "Tom",
                    "phone": "123456",
                    "country_code": "US",
                    "country": "United States",
                    "city": "Seattle",
                    "address1": "1 Main Street",
                    "zip": "98101"
                  }
                }
                """;

        OrderCreateRequest request = service.standardize("SHOPIFY", rawData);

        assertThat(request.getPlatform()).isEqualTo("SHOPIFY");
        assertThat(request.getPlatformOrderNo()).isEqualTo("SHP-001");
        assertThat(request.getWarehouseId()).isEqualTo(3L);
        assertThat(request.getItems()).hasSize(1);
        assertThat(request.getItems().get(0).getSkuCode()).isEqualTo("SKU-1001");
        assertThat(request.getAddress().getCountryCode()).isEqualTo("US");
    }
}
