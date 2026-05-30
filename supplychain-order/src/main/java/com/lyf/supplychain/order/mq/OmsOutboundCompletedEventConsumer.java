package com.lyf.supplychain.order.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lyf.supplychain.common.context.TenantContext;
import com.lyf.supplychain.common.event.WmsOutboundCompletedEvent;
import com.lyf.supplychain.common.idempotent.RedisIdempotentService;
import com.lyf.supplychain.common.redis.CommonRedisKeys;
import com.lyf.supplychain.order.config.OmsOutboundEventProperties;
import com.lyf.supplychain.order.service.OrderMainService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * OMS 出库完成事件消费者。
 *
 * @author liyunfei
 * @date 2026-05-24
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "supplychain.order.outbound-event", name = "mode", havingValue = "rocketmq")
@RocketMQMessageListener(
        topic = "${supplychain.order.outbound-event.topic:wms-outbound-topic}",
        consumerGroup = "${supplychain.order.outbound-event.consumer-group:supplychain-order-outbound-consumer}",
        maxReconsumeTimes = 16
)
public class OmsOutboundCompletedEventConsumer implements RocketMQListener<String> {

    private static final String IDEMPOTENT_SCENE = "wms:outbound:completed";

    private final ObjectMapper objectMapper;
    private final RedisIdempotentService idempotentService;
    private final StringRedisTemplate redisTemplate;
    private final OmsOutboundEventProperties properties;
    private final OrderMainService orderMainService;

    public OmsOutboundCompletedEventConsumer(ObjectMapper objectMapper,
                                             RedisIdempotentService idempotentService,
                                             StringRedisTemplate redisTemplate,
                                             OmsOutboundEventProperties properties,
                                             OrderMainService orderMainService) {
        this.objectMapper = objectMapper;
        this.idempotentService = idempotentService;
        this.redisTemplate = redisTemplate;
        this.properties = properties;
        this.orderMainService = orderMainService;
    }

    /**
     * 消费 WMS 出库完成事件并推进 OMS 订单状态。
     *
     * @param message RocketMQ 消息体
     */
    @Override
    public void onMessage(String message) {
        WmsOutboundCompletedEvent event;
        try {
            event = objectMapper.readValue(message, WmsOutboundCompletedEvent.class);
        } catch (Exception exception) {
            log.error("WMS出库完成事件反序列化失败，message={}", message, exception);
            throw new IllegalArgumentException("WMS出库完成事件反序列化失败", exception);
        }
        Long tenantId = event.getTenantId();
        String idempotentToken = event.getOrderNo() + ":" + event.getOutboundNo();
        boolean firstConsume = idempotentService.markIfAbsent(tenantId, IDEMPOTENT_SCENE, idempotentToken,
                Duration.ofDays(properties.getIdempotentTtlDays()));
        if (!firstConsume) {
            log.info("WMS outbound event duplicated, orderNo={}, outboundNo={}", event.getOrderNo(), event.getOutboundNo());
            return;
        }
        Long previousTenantId = TenantContext.getTenantId();
        Long previousUserId = TenantContext.getUserId();
        try {
            TenantContext.set(tenantId, previousUserId);
            orderMainService.outboundCallback(event.getOrderNo(), event.getOutboundNo());
            log.info("WMS outbound event consumed, orderNo={}, outboundNo={}", event.getOrderNo(), event.getOutboundNo());
        } catch (Exception exception) {
            redisTemplate.delete(CommonRedisKeys.idempotent(tenantId, IDEMPOTENT_SCENE, idempotentToken));
            log.error("WMS outbound event consume failed, orderNo={}, outboundNo={}", event.getOrderNo(), event.getOutboundNo(), exception);
            throw exception;
        } finally {
            if (previousTenantId == null) {
                TenantContext.clear();
            } else {
                TenantContext.set(previousTenantId, previousUserId);
            }
        }
    }
}
