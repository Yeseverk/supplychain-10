package com.lyf.supplychain.warehouse.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lyf.supplychain.warehouse.entity.WarehouseLocation;
import com.lyf.supplychain.warehouse.request.LocationBatchRequest;
import com.lyf.supplychain.warehouse.request.LocationRequest;

import java.util.List;

/**
 * 仓库库位服务。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
public interface WarehouseLocationService extends IService<WarehouseLocation> {

    /**
     * 查询仓库下的库位列表。
     *
     * @param warehouseId 仓库ID
     * @return 库位列表
     */
    List<WarehouseLocation> listByWarehouse(Long warehouseId);

    /**
     * 新增单个库位。
     *
     * @param warehouseId 仓库ID
     * @param request     库位请求
     * @return 库位ID
     */
    Long create(Long warehouseId, LocationRequest request);

    /**
     * 批量生成库位。
     *
     * @param warehouseId 仓库ID
     * @param request     批量创建请求
     * @return 新增库位数量
     */
    Integer batchCreate(Long warehouseId, LocationBatchRequest request);

    /**
     * 查询仓库空闲库位。
     *
     * @param warehouseId 仓库ID
     * @return 空闲库位列表
     */
    List<WarehouseLocation> available(Long warehouseId);

    /**
     * 锁定仓库下的所有正常库位，用于盘点。
     *
     * @param warehouseId 仓库ID
     */
    void lockWarehouseLocations(Long warehouseId);

    /**
     * 解除仓库下库位锁定。
     *
     * @param warehouseId 仓库ID
     */
    void unlockWarehouseLocations(Long warehouseId);
}
