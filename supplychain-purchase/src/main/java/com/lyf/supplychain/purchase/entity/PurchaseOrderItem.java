package com.lyf.supplychain.purchase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 采购订单明细实体。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Data
@TableName("purchase_order_item")
public class PurchaseOrderItem {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long tenantId;
    private Long poId;
    private Long skuId;
    private String skuCode;
    private String skuName;
    private String spec;
    private String unit;
    private Integer quantity;
    private Integer receivedQty;
    private BigDecimal unitPrice;
    private BigDecimal amount;
    private LocalDate expectDate;
    private String remark;
}
