package com.lyf.supplychain.warehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.core.util.StrUtil;
import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.common.context.TenantContext;
import com.lyf.supplychain.common.exception.BusinessException;
import com.lyf.supplychain.common.security.annotation.DataScope;
import com.lyf.supplychain.common.security.datascope.DataScopeQueryHelper;
import com.lyf.supplychain.common.security.datascope.DataScopeResource;
import com.lyf.supplychain.warehouse.constant.WmsConstants;
import com.lyf.supplychain.warehouse.entity.Inventory;
import com.lyf.supplychain.warehouse.entity.InventoryLog;
import com.lyf.supplychain.warehouse.mapper.InventoryLogMapper;
import com.lyf.supplychain.warehouse.mapper.InventoryMapper;
import com.lyf.supplychain.warehouse.request.InventoryAdjustRequest;
import com.lyf.supplychain.warehouse.request.WmsPageQuery;
import com.lyf.supplychain.warehouse.service.WmsInventoryCacheService;
import com.lyf.supplychain.warehouse.service.WmsInventoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * WMS 库存业务服务实现。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Service
public class WmsInventoryServiceImpl implements WmsInventoryService {

    private final InventoryMapper inventoryMapper;
    private final InventoryLogMapper inventoryLogMapper;
    private final WmsInventoryCacheService inventoryCacheService;

    public WmsInventoryServiceImpl(InventoryMapper inventoryMapper,
                                   InventoryLogMapper inventoryLogMapper,
                                   WmsInventoryCacheService inventoryCacheService) {
        this.inventoryMapper = inventoryMapper;
        this.inventoryLogMapper = inventoryLogMapper;
        this.inventoryCacheService = inventoryCacheService;
    }

    /**
     * 分页查询库存。
     *
     * @param query 分页参数
     * @return 库存分页结果
     */
    @Override
    @DataScope(resource = DataScopeResource.WAREHOUSE)
    public PageResult<Inventory> pageInventory(WmsPageQuery query) {
        query.normalize();
        QueryWrapper<Inventory> wrapper = DataScopeQueryHelper.apply(new QueryWrapper<Inventory>(),
                "create_by", null, "warehouse_id", null);
        if (StrUtil.isNotBlank(query.getKeyword())) {
            wrapper.and(w -> w
                    .like("sku_code", query.getKeyword())
                    .or().like("sku_name", query.getKeyword()));
        }
        wrapper.orderByDesc("update_time");
        Page<Inventory> page = inventoryMapper.selectPage(Page.of(query.getPageNum(), query.getPageSize()),
                wrapper);
        page.getRecords().forEach(this::fillAvailableQty);
        return PageResult.from(page);
    }

    /**
     * 查询 SKU 多仓库存详情。
     *
     * @param skuId SKU ID
     * @return 库存列表
     */
    @Override
    public List<Inventory> skuDetail(Long skuId) {
        return inventoryCacheService.getSkuDetail(TenantContext.getTenantId(), skuId, () -> {
            List<Inventory> inventories = inventoryMapper.selectList(new LambdaQueryWrapper<Inventory>()
                    .eq(Inventory::getSkuId, skuId)
                    .orderByAsc(Inventory::getWarehouseId));
            inventories.forEach(this::fillAvailableQty);
            return inventories;
        });
    }

    /**
     * 查询库存预警列表。
     *
     * @return 预警库存列表
     */
    @Override
    public List<Inventory> warnings() {
        return inventoryMapper.selectList(new LambdaQueryWrapper<Inventory>())
                .stream()
                .peek(this::fillAvailableQty)
                .filter(inventory -> inventory.getAvailableQty() <= inventory.getSafetyStock()
                        || (inventory.getMaxStock() != null && inventory.getQuantity() > inventory.getMaxStock()))
                .toList();
    }

    /**
     * 查询库存流水。
     *
     * @param query 分页参数
     * @return 库存流水分页结果
     */
    @Override
    public PageResult<InventoryLog> pageLogs(WmsPageQuery query) {
        query.normalize();
        LambdaQueryWrapper<InventoryLog> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(query.getKeyword())) {
            wrapper.and(w -> w
                    .like(InventoryLog::getSkuCode, query.getKeyword())
                    .or().like(InventoryLog::getSkuName, query.getKeyword())
                    .or().like(InventoryLog::getRefNo, query.getKeyword())
                    .or().like(InventoryLog::getBatchNo, query.getKeyword())
                    .or().like(InventoryLog::getOperatorName, query.getKeyword())
                    .or().like(InventoryLog::getRemark, query.getKeyword()));
        }
        if (query.getStatus() != null) {
            wrapper.eq(InventoryLog::getLogType, query.getStatus());
        }
        wrapper.orderByDesc(InventoryLog::getOperateTime);
        Page<InventoryLog> page = inventoryLogMapper.selectPage(Page.of(query.getPageNum(), query.getPageSize()),
                wrapper);
        return PageResult.from(page);
    }

    /**
     * 查询指定时间点的历史库存快照。
     *
     * @param warehouseId 仓库ID
     * @param skuId       SKU ID
     * @param targetTime  目标时间
     * @return 快照信息
     */
    @Override
    public Map<String, Object> snapshot(Long warehouseId, Long skuId, LocalDateTime targetTime) {
        InventoryLog log = inventoryLogMapper.selectOne(new LambdaQueryWrapper<InventoryLog>()
                .eq(InventoryLog::getWarehouseId, warehouseId)
                .eq(InventoryLog::getSkuId, skuId)
                .le(InventoryLog::getOperateTime, targetTime == null ? LocalDateTime.now() : targetTime)
                .orderByDesc(InventoryLog::getOperateTime)
                .last("limit 1"));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("warehouseId", warehouseId);
        result.put("skuId", skuId);
        result.put("snapshotTime", targetTime);
        result.put("quantity", log == null ? 0 : log.getAfterQty());
        result.put("refNo", log == null ? null : log.getRefNo());
        return result;
    }

    /**
     * 人工调整库存并写入不可篡改流水。
     *
     * @param request 调整请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adjust(InventoryAdjustRequest request) {
        if (request.getLogType() == null) {
            request.setLogType(request.getChangeQty() >= 0 ? WmsConstants.LOG_STOCKTAKE_PROFIT : WmsConstants.LOG_STOCKTAKE_LOSS);
        }
        applyChange(request);
    }

    /**
     * 修改库存并写入库存流水。
     *
     * @param request 调整请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyChange(InventoryAdjustRequest request) {
        Inventory inventory = findOrCreateInventory(request);
        int beforeQty = inventory.getQuantity() == null ? 0 : inventory.getQuantity();
        int afterQty = beforeQty + request.getChangeQty();
        if (afterQty < 0) {
            BusinessException.throwException(13004, "库存不足，无法出库");
        }
        if (request.getChangeQty() < 0) {
            int updated = inventoryMapper.update(null, new LambdaUpdateWrapper<Inventory>()
                    .eq(Inventory::getId, inventory.getId())
                    .ge(Inventory::getQuantity, Math.abs(request.getChangeQty()))
                    .setSql("quantity = quantity + (" + request.getChangeQty() + ")")
                    .set(Inventory::getLastOutboundTime, LocalDateTime.now()));
            if (updated <= 0) {
                BusinessException.throwException(13004, "库存不足，无法出库");
            }
            inventory.setQuantity(afterQty);
        } else {
            inventory.setQuantity(afterQty);
            inventory.setLastInboundTime(LocalDateTime.now());
            updateWeightedCost(inventory, beforeQty, request.getChangeQty(), request.getUnitCost());
            inventoryMapper.updateById(inventory);
        }
        writeLog(request, beforeQty, afterQty);
        inventoryCacheService.evictSkuDetailAfterCommit(TenantContext.getTenantId(), request.getSkuId());
    }

    /**
     * 执行 FIFO 出库库位分配。
     *
     * @param warehouseId 仓库ID
     * @param skuId       SKU ID
     * @param quantity    出库数量
     * @return 库存分配结果
     */
    @Override
    public List<Inventory> allocateFifo(Long warehouseId, Long skuId, Integer quantity) {
        List<Inventory> inventories = inventoryMapper.selectList(new LambdaQueryWrapper<Inventory>()
                .eq(Inventory::getWarehouseId, warehouseId)
                .eq(Inventory::getSkuId, skuId)
                .isNotNull(Inventory::getLocationId)
                .gt(Inventory::getQuantity, 0)
                .orderByAsc(Inventory::getLastInboundTime, Inventory::getLocationId));
        int remaining = quantity;
        for (Inventory inventory : inventories) {
            int pickQty = Math.min(remaining, inventory.getQuantity());
            inventory.setAvailableQty(pickQty);
            remaining -= pickQty;
            if (remaining == 0) {
                break;
            }
        }
        if (remaining > 0) {
            BusinessException.throwException(13004, "库存不足，无法完成FIFO分配");
        }
        return inventories.stream().filter(item -> item.getAvailableQty() != null && item.getAvailableQty() > 0).toList();
    }

    private Inventory findOrCreateInventory(InventoryAdjustRequest request) {
        Inventory inventory = inventoryMapper.selectOne(new LambdaQueryWrapper<Inventory>()
                .eq(Inventory::getWarehouseId, request.getWarehouseId())
                .eq(Objects.nonNull(request.getLocationId()), Inventory::getLocationId, request.getLocationId())
                .isNull(Objects.isNull(request.getLocationId()), Inventory::getLocationId)
                .eq(Inventory::getSkuId, request.getSkuId())
                .last("limit 1"));
        if (inventory != null) {
            return inventory;
        }
        inventory = new Inventory();
        inventory.setTenantId(TenantContext.getTenantId());
        inventory.setWarehouseId(request.getWarehouseId());
        inventory.setLocationId(request.getLocationId());
        inventory.setSkuId(request.getSkuId());
        inventory.setSkuCode(request.getSkuCode());
        inventory.setSkuName(request.getSkuName());
        inventory.setQuantity(0);
        inventory.setFrozenQty(0);
        inventory.setInTransitQty(0);
        inventory.setDefectiveQty(0);
        inventory.setReservedQty(0);
        inventory.setSafetyStock(0);
        inventory.setAvgCost(BigDecimal.ZERO);
        inventory.setTotalCost(BigDecimal.ZERO);
        inventoryMapper.insert(inventory);
        return inventory;
    }

    private void updateWeightedCost(Inventory inventory, int beforeQty, int addQty, BigDecimal unitCost) {
        BigDecimal oldCost = inventory.getTotalCost() == null ? BigDecimal.ZERO : inventory.getTotalCost();
        BigDecimal addCost = (unitCost == null ? BigDecimal.ZERO : unitCost).multiply(BigDecimal.valueOf(addQty));
        BigDecimal totalCost = oldCost.add(addCost);
        inventory.setTotalCost(totalCost);
        inventory.setAvgCost(inventory.getQuantity() == 0 ? BigDecimal.ZERO :
                totalCost.divide(BigDecimal.valueOf(inventory.getQuantity()), 4, RoundingMode.HALF_UP));
    }

    private void writeLog(InventoryAdjustRequest request, int beforeQty, int afterQty) {
        InventoryLog log = new InventoryLog();
        log.setTenantId(TenantContext.getTenantId());
        log.setLogType(request.getLogType());
        log.setWarehouseId(request.getWarehouseId());
        log.setLocationId(request.getLocationId());
        log.setSkuId(request.getSkuId());
        log.setSkuCode(request.getSkuCode());
        log.setSkuName(request.getSkuName());
        log.setChangeQty(request.getChangeQty());
        log.setBeforeQty(beforeQty);
        log.setAfterQty(afterQty);
        log.setRefType(request.getRefType());
        log.setRefNo(request.getRefNo());
        log.setRefId(request.getRefId());
        log.setOperatorId(request.getOperatorId() == null ? 0L : request.getOperatorId());
        log.setOperatorName(request.getOperatorName() == null ? "系统" : request.getOperatorName());
        log.setOperateTime(LocalDateTime.now());
        log.setRemark(request.getRemark());
        inventoryLogMapper.insert(log);
    }

    private void fillAvailableQty(Inventory inventory) {
        inventory.setAvailableQty((inventory.getQuantity() == null ? 0 : inventory.getQuantity())
                - (inventory.getFrozenQty() == null ? 0 : inventory.getFrozenQty())
                - (inventory.getDefectiveQty() == null ? 0 : inventory.getDefectiveQty())
                - (inventory.getReservedQty() == null ? 0 : inventory.getReservedQty()));
    }
}
