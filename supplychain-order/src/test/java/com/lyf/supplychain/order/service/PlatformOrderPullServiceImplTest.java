package com.lyf.supplychain.order.service;

import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.order.config.OrderPlatformPullProperties;
import com.lyf.supplychain.order.entity.OrderLog;
import com.lyf.supplychain.order.entity.OrderMain;
import com.lyf.supplychain.order.model.PlatformOrderPullContext;
import com.lyf.supplychain.order.model.PlatformOrderPullResult;
import com.lyf.supplychain.order.model.PlatformPulledOrder;
import com.lyf.supplychain.order.request.OrderCancelRequest;
import com.lyf.supplychain.order.request.OrderCreateRequest;
import com.lyf.supplychain.order.request.OrderFlagRequest;
import com.lyf.supplychain.order.request.OrderMergeRequest;
import com.lyf.supplychain.order.request.OrderPageQuery;
import com.lyf.supplychain.order.request.OrderSplitRequest;
import com.lyf.supplychain.order.request.WebhookRequest;
import com.lyf.supplychain.order.service.impl.PlatformOrderPullServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 平台订单主动拉取服务测试。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
class PlatformOrderPullServiceImplTest {

    @Test
    void pullOrdersShouldOnlyHandleCurrentShardPlatforms() {
        OrderPlatformPullProperties properties = new OrderPlatformPullProperties();
        properties.setPlatforms(List.of("AMAZON", "EBAY"));
        FakeOrderMainService orderService = new FakeOrderMainService();
        PlatformOrderPullService service = new PlatformOrderPullServiceImpl(
                properties,
                orderService,
                List.of(new FixedPullClient("AMAZON"), new FixedPullClient("EBAY")));

        List<PlatformOrderPullResult> results = service.pullOrders(1, 2);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getPlatform()).isEqualTo("EBAY");
        assertThat(results.get(0).getImportedCount()).isEqualTo(1);
        assertThat(orderService.importedPlatforms).containsExactly("EBAY");
    }

    private static class FixedPullClient implements PlatformOrderPullClient {

        private final String platform;

        private FixedPullClient(String platform) {
            this.platform = platform;
        }

        @Override
        public String platform() {
            return platform;
        }

        @Override
        public List<PlatformPulledOrder> pullOrders(PlatformOrderPullContext context) {
            return List.of(PlatformPulledOrder.builder()
                    .platform(platform)
                    .platformOrderNo(platform + "-001")
                    .rawData("{\"id\":\"" + platform + "-001\"}")
                    .build());
        }
    }

    private static class FakeOrderMainService implements OrderMainService {

        private final List<String> importedPlatforms = new ArrayList<>();

        @Override
        public Long importPlatformOrder(String platform, String rawData) {
            importedPlatforms.add(platform);
            return 1L;
        }

        @Override
        public PageResult<OrderMain> page(OrderPageQuery query) {
            throw new UnsupportedOperationException();
        }

        @Override
        public OrderMain detail(Long id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Long create(OrderCreateRequest request) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void cancel(Long id, OrderCancelRequest request) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void approve(Long id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void reject(Long id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void flag(Long id, OrderFlagRequest request) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void sync(Long id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<OrderLog> logs(Long id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Long split(Long id, OrderSplitRequest request) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Long merge(OrderMergeRequest request) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void webhook(String platform, WebhookRequest request) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void outboundCallback(String orderNo, String outboundNo) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void logisticsCallback(String orderNo, String waybillNo, String trackingNo, Integer logisticsStatus) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Map<String, Object> overview() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Map<String, Object> today() {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Map<String, Object>> syncLogs() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int scanDeliveryWarnings() {
            throw new UnsupportedOperationException();
        }
    }
}
