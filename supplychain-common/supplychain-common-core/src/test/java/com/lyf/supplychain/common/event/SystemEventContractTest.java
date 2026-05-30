package com.lyf.supplychain.common.event;

import com.lyf.supplychain.common.feign.system.SystemEventPublishRequest;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 系统事件契约测试。
 *
 * @author liyunfei
 * @date 2026-05-21
 */
class SystemEventContractTest {

    @Test
    void notificationEventContractShouldKeepStableFields() {
        SystemEventPublishRequest request = new SystemEventPublishRequest();
        request.setTenantId(101L);
        request.setEventId("event-101");
        request.setEventType(EventConstants.EventType.SYSTEM_NOTIFICATION);
        request.setSourceService(EventConstants.SourceService.SUPPLIER);
        request.setBizType("SUPPLIER_RISK");
        request.setBizId("risk-1");
        request.setIdempotentKey("SUPPLIER_RISK:risk-1");
        request.setPayload("{\"title\":\"供应商风险提醒\"}");
        request.setOccurredTime(LocalDateTime.of(2026, 5, 21, 9, 0));

        assertThat(request.getEventType()).isEqualTo("SYSTEM_NOTIFICATION");
        assertThat(request.getSourceService()).isEqualTo("supplychain-supplier");
        assertThat(request.getIdempotentKey()).isEqualTo("SUPPLIER_RISK:risk-1");
    }
}
