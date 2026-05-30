package com.lyf.supplychain.purchase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 采购申请单明细实体。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Data
@TableName("purchase_requisition_item")
public class PurchaseRequisitionItem {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long tenantId;
    private Long reqId;
    private Long skuId;
    private String skuCode;
    private String skuName;
    private Integer quantity;
    private Integer currentStock;
    private Integer safetyStock;
    private Integer inTransitQty;
    private BigDecimal refPrice;
    private LocalDate expectDate;
    private String remark;
}
