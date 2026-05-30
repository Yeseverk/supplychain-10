package com.lyf.supplychain.warehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyf.supplychain.common.exception.BusinessException;
import com.lyf.supplychain.common.feign.warehouse.WarehouseInboundRequest;
import com.lyf.supplychain.common.feign.warehouse.WarehouseOutboundRequest;
import com.lyf.supplychain.common.feign.warehouse.WarehouseStockItem;
import com.lyf.supplychain.warehouse.entity.WarehouseInventory;
import com.lyf.supplychain.warehouse.entity.WarehouseInventoryLog;
import com.lyf.supplychain.warehouse.mapper.WarehouseInventoryLogMapper;
import com.lyf.supplychain.warehouse.mapper.WarehouseInventoryMapper;
import com.lyf.supplychain.warehouse.service.WarehouseInventoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * 仓库库存服务实现。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Service
public class WarehouseInventoryServiceImpl extends ServiceImpl<WarehouseInventoryMapper, WarehouseInventory>
        implements WarehouseInventoryService {

    private final WarehouseInventoryLogMapper logMapper;

    public WarehouseInventoryServiceImpl(WarehouseInventoryLogMapper logMapper) {
        this.logMapper = logMapper;
    }

    /**
     * 采购入库增加库存并写入库存流水。
     *
     * @param request 入库请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void inbound(WarehouseInboundRequest request) {
        for (WarehouseStockItem item : request.getItems()) {
            changeStock(request.getTenantId(), request.getWarehouseId(), request.getBizNo(),
                    request.getBizType(), item, Math.abs(item.getQuantity()));
        }
    }

    /**
     * 采购退货出库扣减库存并写入库存流水。
     *
     * @param request 出库请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void outbound(WarehouseOutboundRequest request) {
        for (WarehouseStockItem item : request.getItems()) {
            changeStock(request.getTenantId(), request.getWarehouseId(), request.getBizNo(),
                    request.getBizType(), item, -Math.abs(item.getQuantity()));
        }
    }

    private void changeStock(Long tenantId, Long warehouseId, String bizNo, String bizType,
                             WarehouseStockItem item, Integer changeQty) {
        WarehouseInventory inventory = getOne(new LambdaQueryWrapper<WarehouseInventory>()
                .eq(WarehouseInventory::getTenantId, tenantId)
                .eq(WarehouseInventory::getWarehouseId, warehouseId)
                .eq(WarehouseInventory::getSkuId, item.getSkuId())
                .eq(Objects.nonNull(item.getLocationId()), WarehouseInventory::getLocationId, item.getLocationId())
                .last("limit 1"));
        if (inventory == null) {
            inventory = new WarehouseInventory();
            inventory.setTenantId(tenantId);
            inventory.setWarehouseId(warehouseId);
            inventory.setLocationId(item.getLocationId());
            inventory.setSkuId(item.getSkuId());
            inventory.setSkuCode(item.getSkuCode());
            inventory.setSkuName(item.getSkuName());
            inventory.setQuantity(0);
            inventory.setLockedQuantity(0);
            save(inventory);
        }
        int beforeQty = inventory.getQuantity();
        int afterQty = beforeQty + changeQty;
        if (afterQty < 0) {
            BusinessException.throwException("库存不足，无法出库");
        }
        inventory.setQuantity(afterQty);
        updateById(inventory);
        WarehouseInventoryLog log = new WarehouseInventoryLog();
        log.setTenantId(tenantId);
        log.setWarehouseId(warehouseId);
        log.setLocationId(item.getLocationId());
        log.setSkuId(item.getSkuId());
        log.setSkuCode(item.getSkuCode());
        log.setSkuName(item.getSkuName());
        log.setBizNo(bizNo);
        log.setBizType(bizType);
        log.setChangeQty(changeQty);
        log.setBeforeQty(beforeQty);
        log.setAfterQty(afterQty);
        logMapper.insert(log);
    }
}
