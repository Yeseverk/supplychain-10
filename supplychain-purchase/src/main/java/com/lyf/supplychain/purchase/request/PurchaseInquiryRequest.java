package com.lyf.supplychain.purchase.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 询价单创建请求。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Data
public class PurchaseInquiryRequest {

    private Long reqId;

    @NotNull(message = "供应商ID不能为空")
    private Long supplierId;

    @NotBlank(message = "供应商名称不能为空")
    private String supplierName;

    private LocalDateTime quoteDeadline;

    private String remark;

    @Valid
    @NotEmpty(message = "询价明细不能为空")
    private List<PurchaseItemRequest> items;
}
