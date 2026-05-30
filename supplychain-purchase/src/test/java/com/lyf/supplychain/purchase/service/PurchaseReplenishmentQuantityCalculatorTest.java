package com.lyf.supplychain.purchase.service;

import com.lyf.supplychain.common.feign.warehouse.WarehouseInventoryWarningResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 采购自动补货数量计算测试。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
class PurchaseReplenishmentQuantityCalculatorTest {

    private final PurchaseReplenishmentQuantityCalculator calculator = new PurchaseReplenishmentQuantityCalculator();

    @Test
    void shouldCalculateSuggestQtyBySafetyStockAndInTransitQty() {
        WarehouseInventoryWarningResponse warning = new WarehouseInventoryWarningResponse();
        warning.setAvailableQty(30);
        warning.setInTransitQty(20);
        warning.setSafetyStock(100);

        int suggestQty = calculator.calculate(warning, 2);

        assertThat(suggestQty).isEqualTo(150);
    }

    @Test
    void shouldNotExceedMaxStockWhenMaxStockConfigured() {
        WarehouseInventoryWarningResponse warning = new WarehouseInventoryWarningResponse();
        warning.setAvailableQty(30);
        warning.setInTransitQty(20);
        warning.setSafetyStock(100);
        warning.setMaxStock(120);

        int suggestQty = calculator.calculate(warning, 2);

        assertThat(suggestQty).isEqualTo(70);
    }
}
