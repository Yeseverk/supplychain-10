package com.lyf.supplychain.common.feign.warehouse;

import lombok.Data;

/**
 * 仓储库存预警响应。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Data
public class WarehouseInventoryWarningResponse {

    private Long tenantId;

    private Long warehouseId;

    private Long locationId;

    private Long skuId;

    private String skuCode;

    private String skuName;

    private Integer quantity;

    private Integer availableQty;

    private Integer inTransitQty;

    private Integer safetyStock;

    private Integer maxStock;

    private Integer reorderPoint;
}
