package com.lyf.supplychain.warehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyf.supplychain.common.api.PageQuery;
import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.common.context.TenantContext;
import com.lyf.supplychain.common.exception.BusinessException;
import com.lyf.supplychain.warehouse.constant.WmsConstants;
import com.lyf.supplychain.warehouse.entity.Inventory;
import com.lyf.supplychain.warehouse.entity.TransferOrder;
import com.lyf.supplychain.warehouse.entity.TransferOrderItem;
import com.lyf.supplychain.warehouse.mapper.InventoryMapper;
import com.lyf.supplychain.warehouse.mapper.TransferOrderItemMapper;
import com.lyf.supplychain.warehouse.mapper.TransferOrderMapper;
import com.lyf.supplychain.warehouse.request.InventoryAdjustRequest;
import com.lyf.supplychain.warehouse.request.TransferOrderRequest;
import com.lyf.supplychain.warehouse.request.WmsItemRequest;
import com.lyf.supplychain.warehouse.service.TransferOrderService;
import com.lyf.supplychain.warehouse.service.WmsInventoryService;
import com.lyf.supplychain.warehouse.service.WmsNumberService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Objects;

/**
 * 仓库调拨服务实现。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Service
public class TransferOrderServiceImpl extends ServiceImpl<TransferOrderMapper, TransferOrder>
        implements TransferOrderService {

    private final TransferOrderItemMapper itemMapper;
    private final InventoryMapper inventoryMapper;
    private final WmsNumberService numberService;
    private final WmsInventoryService inventoryService;

    public TransferOrderServiceImpl(TransferOrderItemMapper itemMapper,
                                    InventoryMapper inventoryMapper,
                                    WmsNumberService numberService,
                                    WmsInventoryService inventoryService) {
        this.itemMapper = itemMapper;
        this.inventoryMapper = inventoryMapper;
        this.numberService = numberService;
        this.inventoryService = inventoryService;
    }

    /**
     * 分页查询调拨单。
     *
     * @param query 分页参数
     * @return 调拨单分页结果
     */
    @Override
    public PageResult<TransferOrder> pageTransfers(PageQuery query) {
        query.normalize();
        return PageResult.from(page(Page.of(query.getPageNum(), query.getPageSize())));
    }

    /**
     * 创建调拨单。
     *
     * @param request 调拨请求
     * @return 调拨单ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(TransferOrderRequest request) {
        TransferOrder order = new TransferOrder();
        BeanUtils.copyProperties(request, order);
        order.setTenantId(TenantContext.getTenantId());
        order.setTransferNo(numberService.nextNo("TRF"));
        order.setStatus(WmsConstants.TRANSFER_DRAFT);
        save(order);
        for (WmsItemRequest itemRequest : request.getItems()) {
            TransferOrderItem item = new TransferOrderItem();
            item.setTenantId(TenantContext.getTenantId());
            item.setTransferId(order.getId());
            item.setSkuId(itemRequest.getSkuId());
            item.setSkuCode(itemRequest.getSkuCode());
            item.setSkuName(itemRequest.getSkuName());
            item.setTransferQty(itemRequest.getQuantity());
            item.setShippedQty(0);
            item.setReceivedQty(0);
            item.setFromLocationId(itemRequest.getLocationId());
            itemMapper.insert(item);
        }
        return order.getId();
    }

    /**
     * 审核通过调拨单。
     *
     * @param id 调拨单ID
     */
    @Override
    public void approve(Long id) {
        TransferOrder order = getById(id);
        if (order == null) {
            BusinessException.throwException("调拨单不存在");
        }
        order.setStatus(WmsConstants.TRANSFER_APPROVED);
        updateById(order);
    }

    /**
     * 确认调拨发货，扣减来源仓并增加目标仓在途库存。
     *
     * @param id 调拨单ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void ship(Long id) {
        TransferOrder order = getById(id);
        if (order == null) {
            BusinessException.throwException("调拨单不存在");
        }
        if (!Objects.equals(order.getStatus(), WmsConstants.TRANSFER_APPROVED)) {
            BusinessException.throwException("只有已审核调拨单可以发货");
        }
        for (TransferOrderItem item : items(id)) {
            InventoryAdjustRequest adjust = new InventoryAdjustRequest();
            adjust.setWarehouseId(order.getFromWarehouseId());
            adjust.setLocationId(item.getFromLocationId());
            adjust.setSkuId(item.getSkuId());
            adjust.setSkuCode(item.getSkuCode());
            adjust.setSkuName(item.getSkuName());
            adjust.setChangeQty(-item.getTransferQty());
            adjust.setLogType(WmsConstants.LOG_TRANSFER_OUT);
            adjust.setRefType("TRANSFER");
            adjust.setRefNo(order.getTransferNo());
            adjust.setRefId(order.getId());
            adjust.setRemark("调拨出库，目标仓同步增加在途库存");
            inventoryService.applyChange(adjust);
            increaseInTransit(order.getToWarehouseId(), item, item.getTransferQty());
            item.setShippedQty(item.getTransferQty());
            itemMapper.updateById(item);
        }
        order.setShipDate(LocalDate.now());
        order.setStatus(WmsConstants.TRANSFER_SHIPPING);
        updateById(order);
    }

    /**
     * 确认调拨到货，减少在途库存并增加目标仓实物库存。
     *
     * @param id 调拨单ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void receive(Long id) {
        TransferOrder order = getById(id);
        if (order == null) {
            BusinessException.throwException("调拨单不存在");
        }
        for (TransferOrderItem item : items(id)) {
            increaseInTransit(order.getToWarehouseId(), item, -item.getShippedQty());
            InventoryAdjustRequest adjust = new InventoryAdjustRequest();
            adjust.setWarehouseId(order.getToWarehouseId());
            adjust.setLocationId(item.getToLocationId());
            adjust.setSkuId(item.getSkuId());
            adjust.setSkuCode(item.getSkuCode());
            adjust.setSkuName(item.getSkuName());
            adjust.setChangeQty(item.getShippedQty());
            adjust.setLogType(WmsConstants.LOG_TRANSFER_IN);
            adjust.setRefType("TRANSFER");
            adjust.setRefNo(order.getTransferNo());
            adjust.setRefId(order.getId());
            adjust.setRemark("调拨入库，减少在途库存并增加实物库存");
            inventoryService.applyChange(adjust);
            item.setReceivedQty(item.getShippedQty());
            itemMapper.updateById(item);
        }
        order.setArriveDate(LocalDate.now());
        order.setStatus(WmsConstants.TRANSFER_DONE);
        updateById(order);
    }

    private java.util.List<TransferOrderItem> items(Long transferId) {
        return itemMapper.selectList(new LambdaQueryWrapper<TransferOrderItem>()
                .eq(TransferOrderItem::getTransferId, transferId));
    }

    private void increaseInTransit(Long warehouseId, TransferOrderItem item, Integer changeQty) {
        Inventory inventory = inventoryMapper.selectOne(new LambdaQueryWrapper<Inventory>()
                .eq(Inventory::getWarehouseId, warehouseId)
                .eq(Inventory::getSkuId, item.getSkuId())
                .isNull(Inventory::getLocationId)
                .last("limit 1"));
        if (inventory == null) {
            inventory = new Inventory();
            inventory.setTenantId(TenantContext.getTenantId());
            inventory.setWarehouseId(warehouseId);
            inventory.setSkuId(item.getSkuId());
            inventory.setSkuCode(item.getSkuCode());
            inventory.setSkuName(item.getSkuName());
            inventory.setQuantity(0);
            inventory.setFrozenQty(0);
            inventory.setInTransitQty(0);
            inventory.setDefectiveQty(0);
            inventory.setReservedQty(0);
            inventory.setSafetyStock(0);
            inventory.setAvgCost(java.math.BigDecimal.ZERO);
            inventory.setTotalCost(java.math.BigDecimal.ZERO);
            inventoryMapper.insert(inventory);
        }
        inventory.setInTransitQty(Math.max(0, inventory.getInTransitQty() + changeQty));
        inventoryMapper.updateById(inventory);
    }
}
