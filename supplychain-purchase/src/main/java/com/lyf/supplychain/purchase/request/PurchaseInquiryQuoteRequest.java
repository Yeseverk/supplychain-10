package com.lyf.supplychain.purchase.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 供应商询价报价请求。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Data
public class PurchaseInquiryQuoteRequest {

    private Integer quoteValidDays;

    private String supplierRemark;

    @Valid
    @NotEmpty(message = "报价明细不能为空")
    private List<PurchaseInquiryQuoteItemRequest> items;
}
