package com.lyf.supplychain.purchase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 采购收货单明细实体。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Data
@TableName("purchase_receipt_item")
public class PurchaseReceiptItem {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long tenantId;
    private Long receiptId;
    private Long poItemId;
    private Long skuId;
    private String skuCode;
    private String skuName;
    private Integer expectedQty;
    private Integer actualQty;
    private Integer passQty;
    private Integer rejectQty;
    private String rejectReason;
    private Long locationId;
    private Integer status;
}
