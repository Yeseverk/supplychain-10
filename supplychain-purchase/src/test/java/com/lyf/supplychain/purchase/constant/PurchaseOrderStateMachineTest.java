package com.lyf.supplychain.purchase.constant;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 采购订单状态机测试。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
class PurchaseOrderStateMachineTest {

    @Test
    void shouldAllowBusinessDrivenTransitions() {
        assertThat(PurchaseOrderStateMachine.canTransit(PurchaseStatus.DRAFT, PurchaseStatus.PO_WAIT_CONFIRM)).isTrue();
        assertThat(PurchaseOrderStateMachine.canTransit(PurchaseStatus.PO_WAIT_CONFIRM, PurchaseStatus.PO_CONFIRMED)).isTrue();
        assertThat(PurchaseOrderStateMachine.canTransit(PurchaseStatus.PO_CONFIRMED, PurchaseStatus.PO_SHIPPING)).isTrue();
        assertThat(PurchaseOrderStateMachine.canTransit(PurchaseStatus.PO_SHIPPING, PurchaseStatus.PO_PART_RECEIVED)).isTrue();
        assertThat(PurchaseOrderStateMachine.canTransit(PurchaseStatus.PO_PART_RECEIVED, PurchaseStatus.PO_ALL_RECEIVED)).isTrue();
        assertThat(PurchaseOrderStateMachine.canTransit(PurchaseStatus.PO_ALL_RECEIVED, PurchaseStatus.PO_RECONCILED)).isTrue();
        assertThat(PurchaseOrderStateMachine.canTransit(PurchaseStatus.PO_RECONCILED, PurchaseStatus.PO_SETTLED)).isTrue();
    }

    @Test
    void shouldRejectIllegalJumpTransitions() {
        assertThat(PurchaseOrderStateMachine.canTransit(PurchaseStatus.DRAFT, PurchaseStatus.PO_ALL_RECEIVED)).isFalse();
        assertThat(PurchaseOrderStateMachine.canTransit(PurchaseStatus.PO_CANCELLED, PurchaseStatus.PO_CONFIRMED)).isFalse();
        assertThat(PurchaseOrderStateMachine.canCancel(PurchaseStatus.PO_ALL_RECEIVED)).isFalse();
        assertThat(PurchaseOrderStateMachine.canCancel(PurchaseStatus.PO_SHIPPING)).isTrue();
    }
}
