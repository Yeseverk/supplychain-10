package com.lyf.supplychain.purchase.constant;

import java.util.Set;

/**
 * 采购订单状态机。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
public final class PurchaseOrderStateMachine {

    private static final Set<Transition> ALLOWED_TRANSITIONS = Set.of(
            transition(PurchaseStatus.DRAFT, PurchaseStatus.PO_WAIT_CONFIRM),
            transition(PurchaseStatus.PO_WAIT_CONFIRM, PurchaseStatus.PO_CONFIRMED),
            transition(PurchaseStatus.PO_CONFIRMED, PurchaseStatus.PO_SHIPPING),
            transition(PurchaseStatus.PO_SHIPPING, PurchaseStatus.PO_PART_RECEIVED),
            transition(PurchaseStatus.PO_SHIPPING, PurchaseStatus.PO_ALL_RECEIVED),
            transition(PurchaseStatus.PO_PART_RECEIVED, PurchaseStatus.PO_ALL_RECEIVED),
            transition(PurchaseStatus.PO_ALL_RECEIVED, PurchaseStatus.PO_RECONCILED),
            transition(PurchaseStatus.PO_ALL_RECEIVED, PurchaseStatus.PO_SETTLED),
            transition(PurchaseStatus.PO_RECONCILED, PurchaseStatus.PO_SETTLED)
    );

    private static final Set<Integer> CANCEL_ALLOWED_STATUSES = Set.of(
            PurchaseStatus.DRAFT,
            PurchaseStatus.PO_WAIT_CONFIRM,
            PurchaseStatus.PO_CONFIRMED,
            PurchaseStatus.PO_SHIPPING,
            PurchaseStatus.PO_PART_RECEIVED
    );

    private PurchaseOrderStateMachine() {
    }

    /**
     * 判断采购订单状态是否允许流转。
     *
     * @param fromStatus 当前状态
     * @param toStatus   目标状态
     * @return 是否允许流转
     */
    public static boolean canTransit(Integer fromStatus, Integer toStatus) {
        return ALLOWED_TRANSITIONS.contains(transition(fromStatus, toStatus));
    }

    /**
     * 判断采购订单是否允许取消。
     *
     * @param status 当前状态
     * @return 是否允许取消
     */
    public static boolean canCancel(Integer status) {
        return CANCEL_ALLOWED_STATUSES.contains(status);
    }

    private static Transition transition(Integer fromStatus, Integer toStatus) {
        return new Transition(fromStatus, toStatus);
    }

    private record Transition(Integer fromStatus, Integer toStatus) {
    }
}
