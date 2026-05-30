package com.lyf.supplychain.warehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyf.supplychain.common.context.TenantContext;
import com.lyf.supplychain.warehouse.constant.WmsConstants;
import com.lyf.supplychain.warehouse.entity.Warehouse;
import com.lyf.supplychain.warehouse.entity.WarehouseLocation;
import com.lyf.supplychain.warehouse.mapper.WarehouseLocationMapper;
import com.lyf.supplychain.warehouse.request.LocationBatchRequest;
import com.lyf.supplychain.warehouse.request.LocationRequest;
import com.lyf.supplychain.warehouse.service.WarehouseLocationService;
import com.lyf.supplychain.warehouse.service.WarehouseService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 仓库库位服务实现。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Service
public class WarehouseLocationServiceImpl extends ServiceImpl<WarehouseLocationMapper, WarehouseLocation>
        implements WarehouseLocationService {

    private final WarehouseService warehouseService;

    public WarehouseLocationServiceImpl(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    /**
     * 查询仓库下的库位列表。
     *
     * @param warehouseId 仓库ID
     * @return 库位列表
     */
    @Override
    public List<WarehouseLocation> listByWarehouse(Long warehouseId) {
        return list(new LambdaQueryWrapper<WarehouseLocation>()
                .eq(WarehouseLocation::getWarehouseId, warehouseId)
                .orderByAsc(WarehouseLocation::getZone, WarehouseLocation::getRowNo,
                        WarehouseLocation::getColumnNo, WarehouseLocation::getFloorNo));
    }

    /**
     * 新增单个库位。
     *
     * @param warehouseId 仓库ID
     * @param request     库位请求
     * @return 库位ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(Long warehouseId, LocationRequest request) {
        WarehouseLocation location = buildLocation(warehouseId, request);
        save(location);
        increaseLocationCount(warehouseId, 1);
        return location.getId();
    }

    /**
     * 批量生成库位。
     *
     * @param warehouseId 仓库ID
     * @param request     批量创建请求
     * @return 新增库位数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer batchCreate(Long warehouseId, LocationBatchRequest request) {
        int count = 0;
        for (int row = request.getRowStart(); row <= request.getRowEnd(); row++) {
            for (int column = request.getColumnStart(); column <= request.getColumnEnd(); column++) {
                for (int floor = request.getFloorStart(); floor <= request.getFloorEnd(); floor++) {
                    LocationRequest item = new LocationRequest();
                    item.setZone(request.getZone());
                    item.setRowNo(row);
                    item.setColumnNo(column);
                    item.setFloorNo(floor);
                    item.setLocationType(request.getLocationType());
                    save(buildLocation(warehouseId, item));
                    count++;
                }
            }
        }
        increaseLocationCount(warehouseId, count);
        return count;
    }

    /**
     * 查询仓库空闲库位。
     *
     * @param warehouseId 仓库ID
     * @return 空闲库位列表
     */
    @Override
    public List<WarehouseLocation> available(Long warehouseId) {
        return list(new LambdaQueryWrapper<WarehouseLocation>()
                .eq(WarehouseLocation::getWarehouseId, warehouseId)
                .eq(WarehouseLocation::getIsOccupied, WmsConstants.DISABLED)
                .eq(WarehouseLocation::getStatus, WmsConstants.ENABLED)
                .orderByAsc(WarehouseLocation::getZone, WarehouseLocation::getRowNo,
                        WarehouseLocation::getColumnNo, WarehouseLocation::getFloorNo));
    }

    /**
     * 锁定仓库下的所有正常库位，用于盘点。
     *
     * @param warehouseId 仓库ID
     */
    @Override
    public void lockWarehouseLocations(Long warehouseId) {
        update(new LambdaUpdateWrapper<WarehouseLocation>()
                .eq(WarehouseLocation::getWarehouseId, warehouseId)
                .eq(WarehouseLocation::getStatus, WmsConstants.ENABLED)
                .set(WarehouseLocation::getStatus, WmsConstants.LOCKED));
    }

    /**
     * 解除仓库下库位锁定。
     *
     * @param warehouseId 仓库ID
     */
    @Override
    public void unlockWarehouseLocations(Long warehouseId) {
        update(new LambdaUpdateWrapper<WarehouseLocation>()
                .eq(WarehouseLocation::getWarehouseId, warehouseId)
                .eq(WarehouseLocation::getStatus, WmsConstants.LOCKED)
                .set(WarehouseLocation::getStatus, WmsConstants.ENABLED));
    }

    private WarehouseLocation buildLocation(Long warehouseId, LocationRequest request) {
        WarehouseLocation location = new WarehouseLocation();
        BeanUtils.copyProperties(request, location);
        location.setTenantId(TenantContext.getTenantId());
        location.setWarehouseId(warehouseId);
        location.setLocationCode("%s-%02d-%02d-%02d".formatted(request.getZone(), request.getRowNo(),
                request.getColumnNo(), request.getFloorNo()));
        location.setLocationType(request.getLocationType() == null ? 1 : request.getLocationType());
        location.setIsOccupied(WmsConstants.DISABLED);
        location.setStatus(WmsConstants.ENABLED);
        return location;
    }

    private void increaseLocationCount(Long warehouseId, int count) {
        Warehouse warehouse = warehouseService.getById(warehouseId);
        if (warehouse != null) {
            warehouse.setTotalLocations((warehouse.getTotalLocations() == null ? 0 : warehouse.getTotalLocations()) + count);
            warehouseService.updateById(warehouse);
        }
    }
}
