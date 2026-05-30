package com.lyf.supplychain.purchase.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lyf.supplychain.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 采购退货单实体。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("purchase_return")
public class PurchaseReturn extends BaseEntity {

    private String returnNo;
    private Long poId;
    private String poNo;
    private Long supplierId;
    private Long warehouseId;
    private Integer returnReason;
    private Integer returnQty;
    private BigDecimal returnAmount;
    private Integer status;
    private Integer handleType;
    private String supplierTrackingNo;
    private String evidenceUrls;
    private String remark;
}
