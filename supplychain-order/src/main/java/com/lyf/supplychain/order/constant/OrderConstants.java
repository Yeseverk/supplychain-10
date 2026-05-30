package com.lyf.supplychain.order.constant;

import java.util.Map;
import java.util.Set;

/**
 * 订单模块业务常量。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
public final class OrderConstants {

    public static final int PENDING = 0;

    public static final int RISK_REVIEW = 1;

    public static final int WAIT_STOCK = 2;

    public static final int STOCKING = 3;

    public static final int WAIT_SHIP = 4;

    public static final int SHIPPED = 5;

    public static final int IN_TRANSIT = 6;

    public static final int SIGNED = 7;

    public static final int COMPLETED = 8;

    public static final int AFTER_SALE = 9;

    public static final int CANCELED = 10;

    public static final int REFUND_PENDING = 0;

    public static final int REFUND_APPROVED = 1;

    public static final int REFUND_RETURN_RECEIVED = 2;

    public static final int REFUND_COMPLETED = 3;

    public static final int REFUND_REJECTED = 4;

    public static final String REDIS_STOCK_KEY_PREFIX = "sku:stock:";

    public static final Map<Integer, Set<Integer>> STATE_WHITE_LIST = Map.of(
            PENDING, Set.of(RISK_REVIEW, WAIT_STOCK, WAIT_SHIP, CANCELED),
            RISK_REVIEW, Set.of(WAIT_STOCK, WAIT_SHIP, CANCELED),
            WAIT_STOCK, Set.of(STOCKING, WAIT_SHIP, CANCELED),
            STOCKING, Set.of(WAIT_SHIP, CANCELED),
            WAIT_SHIP, Set.of(SHIPPED, CANCELED, AFTER_SALE),
            SHIPPED, Set.of(IN_TRANSIT, AFTER_SALE),
            IN_TRANSIT, Set.of(SIGNED, AFTER_SALE),
            SIGNED, Set.of(COMPLETED, AFTER_SALE),
            AFTER_SALE, Set.of(COMPLETED)
    );

    private OrderConstants() {
    }

    /**
     * 构建 Redis 库存 Key。
     *
     * @param tenantId    租户ID
     * @param skuId       SKU ID
     * @param warehouseId 仓库ID
     * @return Redis Key
     */
    public static String stockKey(Long tenantId, Long skuId, Long warehouseId) {
        return REDIS_STOCK_KEY_PREFIX + tenantId + ":" + skuId + ":" + warehouseId;
    }
}
