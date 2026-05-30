package com.lyf.supplychain.logistics.constant;

/**
 * 物流模块业务常量。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
public final class LogisticsConstants {

    public static final int STATUS_ENABLED = 1;

    public static final int STATUS_DISABLED = 0;

    public static final int WAYBILL_WAIT_PICKUP = 0;

    public static final int WAYBILL_PICKED = 1;

    public static final int WAYBILL_IN_TRANSIT = 2;

    public static final int WAYBILL_SIGNED = 7;

    public static final int WAYBILL_EXCEPTION = 8;

    public static final int WAYBILL_CANCELED = 10;

    public static final int RETURN_ARRIVED = 2;

    public static final int RECONCILE_AUTO_CONFIRMED = 0;

    public static final int RECONCILE_PENDING_REVIEW = 1;

    public static final int RECONCILE_UNMATCHED = 2;

    private LogisticsConstants() {
    }
}
