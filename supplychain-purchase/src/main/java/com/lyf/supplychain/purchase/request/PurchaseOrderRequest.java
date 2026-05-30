package com.lyf.supplychain.purchase.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 采购订单保存请求。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Data
public class PurchaseOrderRequest {

    private Long reqId;
    private Long inquiryId;

    @NotNull(message = "供应商ID不能为空")
    private Long supplierId;

    @NotBlank(message = "供应商名称不能为空")
    private String supplierName;

    @NotNull(message = "仓库ID不能为空")
    private Long warehouseId;

    @NotBlank(message = "仓库名称不能为空")
    private String warehouseName;

    private BigDecimal totalAmount;
    private BigDecimal taxAmount;
    private String currency;
    private BigDecimal exchangeRate;
    private Integer paymentType;
    private Integer paymentDays;
    private LocalDate orderDate;
    private LocalDate expectedDate;
    private LocalDate confirmedDate;
    private String contractNo;
    private String invoiceNo;
    private String remark;

    @Valid
    @NotEmpty(message = "采购订单明细不能为空")
    private List<PurchaseItemRequest> items;
}
