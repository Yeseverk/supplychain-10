package com.lyf.supplychain.purchase.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 采购退货请求。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Data
public class PurchaseReturnRequest {

    @NotNull(message = "采购单ID不能为空")
    private Long poId;

    @NotNull(message = "退货原因不能为空")
    private Integer returnReason;

    @NotNull(message = "退货数量不能为空")
    private Integer returnQty;

    @NotNull(message = "退货金额不能为空")
    private BigDecimal returnAmount;

    private Integer handleType;

    private String supplierTrackingNo;

    private String evidenceUrls;

    private String remark;
}
