package com.lyf.supplychain.supplier.constant;

import java.util.Set;

/**
 * 供应商状态流转白名单。
 *
 * @author liyunfei
 * @date 2026-05-16
 */
public final class SupplierStateMachine {

    private static final Set<Transition> ALLOWED_TRANSITIONS = Set.of(
            transition(SupplierStatus.DRAFT, SupplierStatus.PENDING_AUDIT),
            transition(SupplierStatus.PENDING_AUDIT, SupplierStatus.APPROVED),
            transition(SupplierStatus.PENDING_AUDIT, SupplierStatus.REJECTED),
            transition(SupplierStatus.PENDING_AUDIT, SupplierStatus.DRAFT),
            transition(SupplierStatus.REJECTED, SupplierStatus.DRAFT),
            transition(SupplierStatus.APPROVED, SupplierStatus.DISABLED),
            transition(SupplierStatus.DISABLED, SupplierStatus.APPROVED)
    );

    private static final Set<Integer> DELETE_ALLOWED_STATUSES = Set.of(
            SupplierStatus.DRAFT.getCode(),
            SupplierStatus.APPROVED.getCode(),
            SupplierStatus.REJECTED.getCode()
    );

    private SupplierStateMachine() {
    }

    /**
     * 判断状态流转是否在白名单中。
     *
     * @param fromStatus 原状态
     * @param toStatus   目标状态
     * @return 是否允许流转
     */
    public static boolean canTransit(Integer fromStatus, Integer toStatus) {
        return ALLOWED_TRANSITIONS.contains(new Transition(fromStatus, toStatus));
    }

    /**
     * 判断当前状态是否允许逻辑删除。
     *
     * @param status 当前状态
     * @return 是否允许逻辑删除
     */
    public static boolean canDelete(Integer status) {
        return DELETE_ALLOWED_STATUSES.contains(status);
    }

    private static Transition transition(SupplierStatus fromStatus, SupplierStatus toStatus) {
        return new Transition(fromStatus.getCode(), toStatus.getCode());
    }

    private record Transition(Integer fromStatus, Integer toStatus) {
    }
}
