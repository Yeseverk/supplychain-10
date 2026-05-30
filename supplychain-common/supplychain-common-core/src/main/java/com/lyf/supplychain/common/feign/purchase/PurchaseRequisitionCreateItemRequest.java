package com.lyf.supplychain.common.feign.purchase;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 采购申请明细创建请求。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Data
public class PurchaseRequisitionCreateItemRequest {

    private Long skuId;
    private String skuCode;
    private String skuName;
    private String spec;
    private String unit;
    private Integer quantity;
    private Integer currentStock;
    private Integer safetyStock;
    private Integer inTransitQty;
    private BigDecimal refPrice;
    private BigDecimal unitPrice;
    private LocalDate expectDate;
    private String remark;
}
