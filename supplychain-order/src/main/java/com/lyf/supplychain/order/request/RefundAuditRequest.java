package com.lyf.supplychain.order.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 退款审核请求。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
public class RefundAuditRequest {

    @NotNull(message = "审核结果不能为空")
    private Boolean approved;

    private BigDecimal actualRefundAmount;

    private String rejectReason;
}
