package com.lyf.supplychain.order.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lyf.supplychain.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款单实体。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("order_refund")
public class OrderRefund extends BaseEntity {

    private String refundNo;

    private Long orderId;

    private String orderNo;

    private String platformRefundNo;

    private Integer refundType;

    private String refundReason;

    private String reasonDetail;

    private BigDecimal refundAmount;

    private BigDecimal actualRefundAmount;

    private String currency;

    private Integer status;

    private LocalDateTime applyTime;

    private LocalDateTime auditTime;

    private LocalDateTime completeTime;

    private String evidenceUrls;

    private String returnTrackingNo;
}
