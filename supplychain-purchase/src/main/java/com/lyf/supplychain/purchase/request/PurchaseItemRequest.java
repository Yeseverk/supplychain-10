package com.lyf.supplychain.purchase.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 采购明细请求。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Data
public class PurchaseItemRequest {

    @NotNull(message = "SKU ID不能为空")
    private Long skuId;

    @NotBlank(message = "SKU编码不能为空")
    private String skuCode;

    @NotBlank(message = "SKU名称不能为空")
    private String skuName;

    private String spec;

    private String unit;

    @NotNull(message = "数量不能为空")
    private Integer quantity;

    private Integer currentStock;

    private Integer safetyStock;

    private Integer inTransitQty;

    private BigDecimal refPrice;

    private BigDecimal unitPrice;

    private LocalDate expectDate;

    private String remark;
}
