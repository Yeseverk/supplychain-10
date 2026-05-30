package com.lyf.supplychain.warehouse.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lyf.supplychain.common.feign.warehouse.WarehouseInboundRequest;
import com.lyf.supplychain.common.feign.warehouse.WarehouseOutboundRequest;
import com.lyf.supplychain.warehouse.entity.WarehouseInventory;

/**
 * 仓库库存服务。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
public interface WarehouseInventoryService extends IService<WarehouseInventory> {

    /**
     * 采购入库增加库存并写入库存流水。
     *
     * @param request 入库请求
     */
    void inbound(WarehouseInboundRequest request);

    /**
     * 采购退货出库扣减库存并写入库存流水。
     *
     * @param request 出库请求
     */
    void outbound(WarehouseOutboundRequest request);
}
