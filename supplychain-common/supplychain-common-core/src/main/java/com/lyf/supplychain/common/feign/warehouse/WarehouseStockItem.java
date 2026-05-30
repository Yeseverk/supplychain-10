package com.lyf.supplychain.common.feign.warehouse;

import lombok.Data;

/**
 * 仓储库存变更明细。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Data
public class WarehouseStockItem {

    private Long skuId;

    private String skuCode;

    private String skuName;

    private Long locationId;

    private Integer quantity;
}
