package com.lyf.supplychain.warehouse.constant;

/**
 * WMS 业务常量。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
public final class WmsConstants {

    public static final int ENABLED = 1;
    public static final int DISABLED = 0;
    public static final int LOCKED = 2;

    public static final int INBOUND_PENDING = 0;
    public static final int INBOUND_DOING = 1;
    public static final int INBOUND_DONE = 2;
    public static final int INBOUND_CANCELLED = 3;

    public static final int OUTBOUND_ALLOCATING = 0;
    public static final int OUTBOUND_WAIT_PICK = 1;
    public static final int OUTBOUND_PICKING = 2;
    public static final int OUTBOUND_WAIT_REVIEW = 3;
    public static final int OUTBOUND_DONE = 4;
    public static final int OUTBOUND_CANCELLED = 5;

    public static final int TRANSFER_DRAFT = 0;
    public static final int TRANSFER_APPROVED = 1;
    public static final int TRANSFER_SHIPPING = 2;
    public static final int TRANSFER_ARRIVED = 3;
    public static final int TRANSFER_DONE = 4;

    public static final int STOCKTAKE_PENDING = 0;
    public static final int STOCKTAKE_DOING = 1;
    public static final int STOCKTAKE_WAIT_AUDIT = 2;
    public static final int STOCKTAKE_DONE = 3;

    public static final int LOG_PURCHASE_IN = 1;
    public static final int LOG_SALE_OUT = 2;
    public static final int LOG_TRANSFER_IN = 3;
    public static final int LOG_TRANSFER_OUT = 4;
    public static final int LOG_STOCKTAKE_PROFIT = 5;
    public static final int LOG_STOCKTAKE_LOSS = 6;
    public static final int LOG_RETURN_IN = 7;
    public static final int LOG_DAMAGE_OUT = 8;
    public static final int LOG_FREEZE = 11;
    public static final int LOG_UNFREEZE = 12;

    private WmsConstants() {
    }
}
