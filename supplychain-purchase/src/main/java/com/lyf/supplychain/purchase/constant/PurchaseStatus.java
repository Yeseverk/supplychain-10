package com.lyf.supplychain.purchase.constant;

/**
 * 采购模块状态常量。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
public final class PurchaseStatus {

    public static final int DRAFT = 0;
    public static final int PENDING = 1;
    public static final int APPROVED = 2;
    public static final int REJECTED = 3;
    public static final int CONVERTED = 4;
    public static final int CANCELLED = 5;

    public static final int PO_WAIT_CONFIRM = 1;
    public static final int PO_CONFIRMED = 2;
    public static final int PO_SHIPPING = 3;
    public static final int PO_PART_RECEIVED = 4;
    public static final int PO_ALL_RECEIVED = 5;
    public static final int PO_RECONCILED = 6;
    public static final int PO_SETTLED = 7;
    public static final int PO_CANCELLED = 8;

    private PurchaseStatus() {
    }
}
