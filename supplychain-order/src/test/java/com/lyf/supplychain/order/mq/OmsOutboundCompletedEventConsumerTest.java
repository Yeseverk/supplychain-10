package com.lyf.supplychain.order.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lyf.supplychain.common.event.WmsOutboundCompletedEvent;
import com.lyf.supplychain.common.idempotent.RedisIdempotentService;
import com.lyf.supplychain.common.redis.CommonRedisKeys;
import com.lyf.supplychain.order.config.OmsOutboundEventProperties;
import com.lyf.supplychain.order.service.OrderMainService;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * OMS 出库完成事件消费者测试。
 *
 * @author liyunfei
 * @date 2026-05-24
 */
class OmsOutboundCompletedEventConsumerTest {

    @Test
    void onMessageShouldUpdateOrderWhenFirstConsume() throws Exception {
        RedisIdempotentService idempotentService = mock(RedisIdempotentService.class);
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        OrderMainService orderMainService = mock(OrderMainService.class);
        OmsOutboundCompletedEventConsumer consumer = newConsumer(idempotentService, redisTemplate, orderMainService);
        when(idempotentService.markIfAbsent(eq("wms:outbound:completed"), eq("SO-001:OUT-001"), any(Duration.class)))
                .thenReturn(true);

        consumer.onMessage(message("SO-001", "OUT-001"));

        verify(orderMainService).outboundCallback("SO-001", "OUT-001");
    }

    @Test
    void onMessageShouldSkipWhenDuplicateConsume() throws Exception {
        RedisIdempotentService idempotentService = mock(RedisIdempotentService.class);
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        OrderMainService orderMainService = mock(OrderMainService.class);
        OmsOutboundCompletedEventConsumer consumer = newConsumer(idempotentService, redisTemplate, orderMainService);
        when(idempotentService.markIfAbsent(eq("wms:outbound:completed"), eq("SO-001:OUT-001"), any(Duration.class)))
                .thenReturn(false);

        consumer.onMessage(message("SO-001", "OUT-001"));

        verify(orderMainService, never()).outboundCallback(any(), any());
    }

    @Test
    void onMessageShouldDeleteIdempotentKeyWhenBusinessFails() throws Exception {
        RedisIdempotentService idempotentService = mock(RedisIdempotentService.class);
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        OrderMainService orderMainService = mock(OrderMainService.class);
        OmsOutboundCompletedEventConsumer consumer = newConsumer(idempotentService, redisTemplate, orderMainService);
        when(idempotentService.markIfAbsent(eq("wms:outbound:completed"), eq("SO-001:OUT-001"), any(Duration.class)))
                .thenReturn(true);
        org.mockito.Mockito.doThrow(new IllegalStateException("failed"))
                .when(orderMainService).outboundCallback("SO-001", "OUT-001");

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> consumer.onMessage(message("SO-001", "OUT-001")))
                .isInstanceOf(IllegalStateException.class);

        verify(redisTemplate).delete(CommonRedisKeys.idempotent("wms:outbound:completed", "SO-001:OUT-001"));
    }

    private OmsOutboundCompletedEventConsumer newConsumer(RedisIdempotentService idempotentService,
                                                          StringRedisTemplate redisTemplate,
                                                          OrderMainService orderMainService) {
        OmsOutboundEventProperties properties = new OmsOutboundEventProperties();
        properties.setIdempotentTtlDays(7L);
        return new OmsOutboundCompletedEventConsumer(new ObjectMapper(), idempotentService,
                redisTemplate, properties, orderMainService);
    }

    private String message(String orderNo, String outboundNo) throws Exception {
        WmsOutboundCompletedEvent event = new WmsOutboundCompletedEvent();
        event.setOrderNo(orderNo);
        event.setOutboundNo(outboundNo);
        return new ObjectMapper().writeValueAsString(event);
    }
}
