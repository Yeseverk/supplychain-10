package com.lyf.supplychain.order.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lyf.supplychain.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 订单主表实体。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("order_main")
public class OrderMain extends BaseEntity {

    private String orderNo;

    private String platform;

    private String platformOrderNo;

    private Long storeId;

    private BigDecimal totalAmount;

    private BigDecimal discountAmount;

    private BigDecimal shippingFee;

    private BigDecimal paymentAmount;

    private String currency;

    private BigDecimal exchangeRate;

    private BigDecimal cnyAmount;

    private BigDecimal platformFee;

    private Integer status;

    private String cancelReason;

    private Integer isAbnormal;

    private String abnormalReason;

    private Long warehouseId;

    private String logisticsChannel;

    private String waybillNo;

    private LocalDateTime shipTime;

    private LocalDate deliveryDeadline;

    private LocalDateTime signedTime;

    private LocalDateTime platformOrderTime;

    private LocalDateTime platformPayTime;
}
