package com.lyf.supplychain.purchase.service;

import com.lyf.supplychain.purchase.config.PurchaseApprovalProperties;
import com.lyf.supplychain.purchase.model.PurchaseApprovalDecision;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 采购金额分级审批策略。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Component
public class PurchaseApprovalPolicy {

    private static final String ROLE_AUTO = "AUTO";

    private static final String ROLE_PURCHASE_MANAGER = "PURCHASE_MANAGER";

    private static final String ROLE_FINANCE_MANAGER = "FINANCE_MANAGER";

    private final PurchaseApprovalProperties properties;

    public PurchaseApprovalPolicy(PurchaseApprovalProperties properties) {
        this.properties = properties;
    }

    /**
     * 根据采购申请金额判断审批层级。
     *
     * @param totalAmount 采购申请金额
     * @return 审批决策
     */
    public PurchaseApprovalDecision decide(BigDecimal totalAmount) {
        BigDecimal safeAmount = totalAmount == null ? BigDecimal.ZERO : totalAmount;
        if (safeAmount.compareTo(properties.getApproveFreeAmount()) <= 0) {
            return decision(true, 0, ROLE_AUTO, "小额采购自动审批通过");
        }
        if (safeAmount.compareTo(properties.getManagerApproveAmount()) <= 0) {
            return decision(false, 1, ROLE_PURCHASE_MANAGER, "中额采购需要采购负责人审批");
        }
        return decision(false, 2, ROLE_FINANCE_MANAGER, "大额采购需要财务负责人审批");
    }

    private PurchaseApprovalDecision decision(boolean autoApprove, Integer approvalLevel, String approvalRole, String description) {
        PurchaseApprovalDecision decision = new PurchaseApprovalDecision();
        decision.setAutoApprove(autoApprove);
        decision.setApprovalLevel(approvalLevel);
        decision.setApprovalRole(approvalRole);
        decision.setDescription(description);
        return decision;
    }
}
