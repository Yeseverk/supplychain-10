package com.lyf.supplychain.purchase.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 采购审批请求。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Data
public class PurchaseAuditRequest {

    @NotNull(message = "审批人ID不能为空")
    private Long auditUserId;

    private String auditRemark;
}
