package com.lyf.supplychain.common.event;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * WMS 出库完成事件。
 *
 * @author liyunfei
 * @date 2026-05-24
 */
@Data
public class WmsOutboundCompletedEvent {

    private String eventId;

    private Long tenantId;

    private Long outboundId;

    private String outboundNo;

    private Long orderId;

    private String orderNo;

    private LocalDate outboundDate;

    private LocalDateTime occurredTime;

    private List<Item> items;

    /**
     * 出库完成事件明细。
     *
     * @author liyunfei
     * @date 2026-05-24
     */
    @Data
    public static class Item {

        private Long skuId;

        private String skuCode;

        private String skuName;

        private Integer quantity;

        private Long locationId;
    }
}
