package com.lyf.supplychain.purchase.service;

import com.lyf.supplychain.common.feign.warehouse.WarehouseInventoryWarningResponse;
import org.springframework.stereotype.Component;

/**
 * 采购自动补货数量计算器。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Component
public class PurchaseReplenishmentQuantityCalculator {

    /**
     * 根据库存预警、在途数量、安全库存和补货倍数计算建议采购量。
     *
     * @param warning               库存预警
     * @param safetyStockMultiplier 安全库存补货倍数
     * @return 建议采购量
     */
    public int calculate(WarehouseInventoryWarningResponse warning, Integer safetyStockMultiplier) {
        int availableQty = safe(warning.getAvailableQty());
        int inTransitQty = safe(warning.getInTransitQty());
        int safetyStock = safe(warning.getSafetyStock());
        int multiplier = safetyStockMultiplier == null || safetyStockMultiplier <= 0 ? 2 : safetyStockMultiplier;
        int targetQty = warning.getReorderPoint() != null && warning.getReorderPoint() > 0
                ? warning.getReorderPoint()
                : safetyStock * multiplier;
        int suggestQty = targetQty - availableQty - inTransitQty;
        if (warning.getMaxStock() != null && warning.getMaxStock() > 0) {
            suggestQty = Math.min(suggestQty, warning.getMaxStock() - availableQty - inTransitQty);
        }
        return Math.max(suggestQty, 0);
    }

    private int safe(Integer value) {
        return value == null ? 0 : value;
    }
}
