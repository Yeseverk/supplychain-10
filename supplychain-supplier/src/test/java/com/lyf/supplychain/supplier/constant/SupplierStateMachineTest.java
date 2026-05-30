package com.lyf.supplychain.supplier.constant;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 供应商状态机测试。
 *
 * @author liyunfei
 * @date 2026-05-16
 */
class SupplierStateMachineTest {

    @Test
    void shouldAllowAuditWhitelistTransitions() {
        assertThat(SupplierStateMachine.canTransit(SupplierStatus.DRAFT.getCode(), SupplierStatus.PENDING_AUDIT.getCode())).isTrue();
        assertThat(SupplierStateMachine.canTransit(SupplierStatus.PENDING_AUDIT.getCode(), SupplierStatus.APPROVED.getCode())).isTrue();
        assertThat(SupplierStateMachine.canTransit(SupplierStatus.PENDING_AUDIT.getCode(), SupplierStatus.REJECTED.getCode())).isTrue();
        assertThat(SupplierStateMachine.canTransit(SupplierStatus.PENDING_AUDIT.getCode(), SupplierStatus.DRAFT.getCode())).isTrue();
        assertThat(SupplierStateMachine.canTransit(SupplierStatus.REJECTED.getCode(), SupplierStatus.DRAFT.getCode())).isTrue();
        assertThat(SupplierStateMachine.canTransit(SupplierStatus.APPROVED.getCode(), SupplierStatus.DISABLED.getCode())).isTrue();
        assertThat(SupplierStateMachine.canTransit(SupplierStatus.DISABLED.getCode(), SupplierStatus.APPROVED.getCode())).isTrue();
    }

    @Test
    void shouldRejectTransitionsOutsideWhitelist() {
        assertThat(SupplierStateMachine.canTransit(SupplierStatus.DRAFT.getCode(), SupplierStatus.APPROVED.getCode())).isFalse();
        assertThat(SupplierStateMachine.canTransit(SupplierStatus.REJECTED.getCode(), SupplierStatus.APPROVED.getCode())).isFalse();
        assertThat(SupplierStateMachine.canTransit(SupplierStatus.DISABLED.getCode(), SupplierStatus.PENDING_AUDIT.getCode())).isFalse();
    }

    @Test
    void shouldAllowLogicDeleteOnlyForWhitelistStatuses() {
        assertThat(SupplierStateMachine.canDelete(SupplierStatus.DRAFT.getCode())).isTrue();
        assertThat(SupplierStateMachine.canDelete(SupplierStatus.APPROVED.getCode())).isTrue();
        assertThat(SupplierStateMachine.canDelete(SupplierStatus.REJECTED.getCode())).isTrue();
        assertThat(SupplierStateMachine.canDelete(SupplierStatus.PENDING_AUDIT.getCode())).isFalse();
        assertThat(SupplierStateMachine.canDelete(SupplierStatus.DISABLED.getCode())).isFalse();
    }
}
