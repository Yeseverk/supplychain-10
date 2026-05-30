package com.lyf.supplychain.warehouse.service;

import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.warehouse.entity.Inventory;
import com.lyf.supplychain.warehouse.entity.InventoryLog;
import com.lyf.supplychain.warehouse.request.InventoryAdjustRequest;
import com.lyf.supplychain.warehouse.request.WmsPageQuery;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * WMS 库存业务服务。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
public interface WmsInventoryService {

    /**
     * 分页查询库存。
     *
     * @param query 分页参数
     * @return 库存分页结果
     */
    PageResult<Inventory> pageInventory(WmsPageQuery query);

    /**
     * 查询 SKU 多仓库存详情。
     *
     * @param skuId SKU ID
     * @return 库存列表
     */
    List<Inventory> skuDetail(Long skuId);

    /**
     * 查询库存预警列表。
     *
     * @return 预警库存列表
     */
    List<Inventory> warnings();

    /**
     * 查询库存流水。
     *
     * @param query 分页参数
     * @return 库存流水分页结果
     */
    PageResult<InventoryLog> pageLogs(WmsPageQuery query);

    /**
     * 查询指定时间点的历史库存快照。
     *
     * @param warehouseId 仓库ID
     * @param skuId       SKU ID
     * @param targetTime  目标时间
     * @return 快照信息
     */
    Map<String, Object> snapshot(Long warehouseId, Long skuId, LocalDateTime targetTime);

    /**
     * 人工调整库存并写入不可篡改流水。
     *
     * @param request 调整请求
     */
    void adjust(InventoryAdjustRequest request);

    /**
     * 修改库存并写入库存流水。
     *
     * @param request 调整请求
     */
    void applyChange(InventoryAdjustRequest request);

    /**
     * 执行 FIFO 出库库位分配。
     *
     * @param warehouseId 仓库ID
     * @param skuId       SKU ID
     * @param quantity    出库数量
     * @return 库存分配结果
     */
    List<Inventory> allocateFifo(Long warehouseId, Long skuId, Integer quantity);
}
