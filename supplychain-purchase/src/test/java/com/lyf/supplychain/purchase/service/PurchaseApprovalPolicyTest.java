package com.lyf.supplychain.purchase.service;

import com.lyf.supplychain.purchase.config.PurchaseApprovalProperties;
import com.lyf.supplychain.purchase.model.PurchaseApprovalDecision;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 采购金额分级审批策略测试。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
class PurchaseApprovalPolicyTest {

    private final PurchaseApprovalPolicy policy = new PurchaseApprovalPolicy(properties());

    @Test
    void shouldAutoApproveSmallAmount() {
        PurchaseApprovalDecision decision = policy.decide(new BigDecimal("8000"));

        assertThat(decision.isAutoApprove()).isTrue();
        assertThat(decision.getApprovalLevel()).isEqualTo(0);
        assertThat(decision.getApprovalRole()).isEqualTo("AUTO");
    }

    @Test
    void shouldRequireManagerApproveForMiddleAmount() {
        PurchaseApprovalDecision decision = policy.decide(new BigDecimal("30000"));

        assertThat(decision.isAutoApprove()).isFalse();
        assertThat(decision.getApprovalLevel()).isEqualTo(1);
        assertThat(decision.getApprovalRole()).isEqualTo("PURCHASE_MANAGER");
    }

    @Test
    void shouldRequireFinanceApproveForLargeAmount() {
        PurchaseApprovalDecision decision = policy.decide(new BigDecimal("80000"));

        assertThat(decision.isAutoApprove()).isFalse();
        assertThat(decision.getApprovalLevel()).isEqualTo(2);
        assertThat(decision.getApprovalRole()).isEqualTo("FINANCE_MANAGER");
    }

    private PurchaseApprovalProperties properties() {
        PurchaseApprovalProperties properties = new PurchaseApprovalProperties();
        properties.setApproveFreeAmount(new BigDecimal("10000"));
        properties.setManagerApproveAmount(new BigDecimal("50000"));
        return properties;
    }
}
