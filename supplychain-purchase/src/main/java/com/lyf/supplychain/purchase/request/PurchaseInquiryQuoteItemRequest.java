package com.lyf.supplychain.purchase.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 供应商询价报价明细请求。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Data
public class PurchaseInquiryQuoteItemRequest {

    @NotNull(message = "询价明细ID不能为空")
    private Long inquiryItemId;

    @NotNull(message = "报价单价不能为空")
    @DecimalMin(value = "0.0001", message = "报价单价必须大于0")
    private BigDecimal quotedPrice;

    @NotNull(message = "可供数量不能为空")
    private Integer quotedQty;

    @NotNull(message = "交货天数不能为空")
    private Integer deliveryDays;

    private Integer minOrderQty;

    private String remark;
}
