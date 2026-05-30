package com.lyf.supplychain.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单明细实体。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
@TableName("order_item")
public class OrderItem {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long tenantId;

    private Long orderId;

    private String orderNo;

    private Long skuId;

    private String skuCode;

    private String skuName;

    private String platformSkuId;

    private Integer quantity;

    private BigDecimal unitPrice;

    private BigDecimal discount;

    private BigDecimal amount;

    private String currency;

    private Integer refundedQty;
}
