package com.lyf.supplychain.purchase.model;

import lombok.Data;

/**
 * 采购审批策略决策结果。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Data
public class PurchaseApprovalDecision {

    private boolean autoApprove;

    private Integer approvalLevel;

    private String approvalRole;

    private String description;
}
