package com.lyf.supplychain.purchase.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lyf.supplychain.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 采购收货单实体。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("purchase_receipt")
public class PurchaseReceipt extends BaseEntity {

    private String receiptNo;
    private Long poId;
    private String poNo;
    private Long supplierId;
    private Long warehouseId;
    private LocalDate receiveDate;
    private Long receiverId;
    private String receiverName;
    private Integer status;
    private Integer totalQty;
    private Integer passQty;
    private Integer rejectQty;
    private Integer isOnTime;
    private String remark;
}
