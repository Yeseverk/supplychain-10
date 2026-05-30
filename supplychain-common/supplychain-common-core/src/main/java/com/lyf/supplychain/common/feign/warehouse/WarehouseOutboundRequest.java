package com.lyf.supplychain.common.feign.warehouse;

import lombok.Data;

import java.util.List;

/**
 * 仓储出库请求。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Data
public class WarehouseOutboundRequest {

    private Long tenantId;

    private Long warehouseId;

    private String bizNo;

    private String bizType;

    private List<WarehouseStockItem> items;
}
