package com.lyf.supplychain.warehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.common.context.TenantContext;
import com.lyf.supplychain.common.exception.BusinessException;
import com.lyf.supplychain.warehouse.constant.WmsConstants;
import com.lyf.supplychain.warehouse.entity.InboundOrder;
import com.lyf.supplychain.warehouse.entity.InboundOrderItem;
import com.lyf.supplychain.warehouse.entity.WarehouseLocation;
import com.lyf.supplychain.warehouse.mapper.InboundOrderItemMapper;
import com.lyf.supplychain.warehouse.mapper.InboundOrderMapper;
import com.lyf.supplychain.warehouse.mapper.WarehouseLocationMapper;
import com.lyf.supplychain.warehouse.request.InboundConfirmRequest;
import com.lyf.supplychain.warehouse.request.InboundOrderRequest;
import com.lyf.supplychain.warehouse.request.InventoryAdjustRequest;
import com.lyf.supplychain.warehouse.request.WmsItemRequest;
import com.lyf.supplychain.warehouse.request.WmsPageQuery;
import com.lyf.supplychain.warehouse.service.InboundOrderService;
import com.lyf.supplychain.warehouse.service.WmsInventoryService;
import com.lyf.supplychain.warehouse.service.WmsNumberService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * 入库单服务实现。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Service
public class InboundOrderServiceImpl extends ServiceImpl<InboundOrderMapper, InboundOrder>
        implements InboundOrderService {

    private final InboundOrderItemMapper itemMapper;
    private final WarehouseLocationMapper locationMapper;
    private final WmsNumberService numberService;
    private final WmsInventoryService inventoryService;

    public InboundOrderServiceImpl(InboundOrderItemMapper itemMapper,
                                   WarehouseLocationMapper locationMapper,
                                   WmsNumberService numberService,
                                   WmsInventoryService inventoryService) {
        this.itemMapper = itemMapper;
        this.locationMapper = locationMapper;
        this.numberService = numberService;
        this.inventoryService = inventoryService;
    }

    /**
     * 分页查询入库单。
     *
     * @param query 分页参数
     * @return 入库单分页结果
     */
    @Override
    public PageResult<InboundOrder> pageInbound(WmsPageQuery query) {
        query.normalize();
        LambdaQueryWrapper<InboundOrder> wrapper = new LambdaQueryWrapper<InboundOrder>()
                .orderByDesc(InboundOrder::getCreateTime);
        if (query.getStatus() != null) {
            wrapper.eq(InboundOrder::getStatus, query.getStatus());
        }
        if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
            String keyword = query.getKeyword().trim();
            wrapper.and(item -> item.like(InboundOrder::getInboundNo, keyword)
                    .or().like(InboundOrder::getRefNo, keyword)
                    .or().like(InboundOrder::getWarehouseName, keyword)
                    .or().like(InboundOrder::getRemark, keyword));
        }
        return PageResult.from(page(Page.of(query.getPageNum(), query.getPageSize()), wrapper));
    }

    /**
     * 查询入库单明细。
     *
     * @param id 入库单ID
     * @return 入库明细列表
     */
    @Override
    public List<InboundOrderItem> listItems(Long id) {
        InboundOrder order = getById(id);
        if (order == null) {
            BusinessException.throwException("入库单不存在");
        }
        return itemMapper.selectList(new LambdaQueryWrapper<InboundOrderItem>()
                .eq(InboundOrderItem::getInboundId, id));
    }

    /**
     * 创建入库单。
     *
     * @param request 入库单请求
     * @return 入库单ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(InboundOrderRequest request) {
        InboundOrder order = new InboundOrder();
        BeanUtils.copyProperties(request, order);
        order.setTenantId(TenantContext.getTenantId());
        order.setInboundNo(numberService.nextNo("IN"));
        order.setStatus(WmsConstants.INBOUND_PENDING);
        order.setTotalSkuCount(request.getItems().size());
        order.setTotalQty(request.getItems().stream().mapToInt(WmsItemRequest::getQuantity).sum());
        order.setActualQty(0);
        save(order);
        for (WmsItemRequest itemRequest : request.getItems()) {
            InboundOrderItem item = new InboundOrderItem();
            item.setTenantId(TenantContext.getTenantId());
            item.setInboundId(order.getId());
            item.setSkuId(itemRequest.getSkuId());
            item.setSkuCode(itemRequest.getSkuCode());
            item.setSkuName(itemRequest.getSkuName());
            item.setExpectedQty(itemRequest.getQuantity());
            item.setActualQty(0);
            item.setDefectiveQty(0);
            item.setStatus(0);
            item.setRemark(itemRequest.getRemark());
            itemMapper.insert(item);
        }
        return order.getId();
    }

    /**
     * 确认入库并更新库存、成本、库位和流水。
     *
     * @param id      入库单ID
     * @param request 确认请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirm(Long id, InboundConfirmRequest request) {
        InboundOrder order = getById(id);
        if (order == null) {
            BusinessException.throwException("入库单不存在");
        }
        if (Objects.equals(order.getStatus(), WmsConstants.INBOUND_DONE)) {
            return;
        }
        int totalActual = 0;
        for (WmsItemRequest confirmItem : request.getItems()) {
            InboundOrderItem item = mustFindItem(id, confirmItem);
            WarehouseLocation location = locationMapper.selectById(confirmItem.getLocationId());
            if (location == null || Objects.equals(location.getStatus(), WmsConstants.LOCKED)) {
                BusinessException.throwException(13002, "库位不存在、停用或已被锁定");
            }
            int actualQty = confirmItem.getActualQty() == null ? confirmItem.getQuantity() : confirmItem.getActualQty();
            int defectiveQty = confirmItem.getDefectiveQty() == null ? 0 : confirmItem.getDefectiveQty();
            item.setActualQty(actualQty);
            item.setDefectiveQty(defectiveQty);
            item.setLocationId(confirmItem.getLocationId());
            item.setLocationCode(confirmItem.getLocationCode());
            item.setUnitCost(confirmItem.getUnitCost());
            item.setStatus(WmsConstants.INBOUND_DONE);
            itemMapper.updateById(item);
            applyInboundChange(order, item, actualQty, confirmItem, request);
            if (defectiveQty > 0) {
                applyDefectiveChange(order, item, defectiveQty, confirmItem, request);
            }
            location.setIsOccupied(WmsConstants.ENABLED);
            locationMapper.updateById(location);
            totalActual += actualQty;
        }
        order.setActualQty(totalActual);
        order.setActualDate(request.getActualDate() == null ? LocalDate.now() : request.getActualDate());
        order.setOperatorId(request.getOperatorId());
        order.setStatus(WmsConstants.INBOUND_DONE);
        updateById(order);
    }

    private InboundOrderItem mustFindItem(Long inboundId, WmsItemRequest confirmItem) {
        InboundOrderItem item = confirmItem.getItemId() == null ? null : itemMapper.selectById(confirmItem.getItemId());
        if (item == null) {
            item = itemMapper.selectOne(new LambdaQueryWrapper<InboundOrderItem>()
                    .eq(InboundOrderItem::getInboundId, inboundId)
                    .eq(InboundOrderItem::getSkuId, confirmItem.getSkuId())
                    .last("limit 1"));
        }
        if (item == null) {
            BusinessException.throwException("入库明细不存在");
        }
        return item;
    }

    private void applyInboundChange(InboundOrder order, InboundOrderItem item, int actualQty,
                                    WmsItemRequest confirmItem, InboundConfirmRequest request) {
        InventoryAdjustRequest adjust = new InventoryAdjustRequest();
        adjust.setWarehouseId(order.getWarehouseId());
        adjust.setLocationId(confirmItem.getLocationId());
        adjust.setSkuId(item.getSkuId());
        adjust.setSkuCode(item.getSkuCode());
        adjust.setSkuName(item.getSkuName());
        adjust.setChangeQty(actualQty);
        adjust.setUnitCost(confirmItem.getUnitCost());
        adjust.setLogType(order.getInboundType() == 2 ? WmsConstants.LOG_TRANSFER_IN : WmsConstants.LOG_PURCHASE_IN);
        adjust.setRefType(order.getRefType());
        adjust.setRefNo(order.getInboundNo());
        adjust.setRefId(order.getId());
        adjust.setOperatorId(request.getOperatorId());
        adjust.setOperatorName(request.getOperatorName());
        adjust.setRemark(confirmItem.getRemark());
        inventoryService.applyChange(adjust);
    }

    private void applyDefectiveChange(InboundOrder order, InboundOrderItem item, int defectiveQty,
                                      WmsItemRequest confirmItem, InboundConfirmRequest request) {
        InventoryAdjustRequest adjust = new InventoryAdjustRequest();
        adjust.setWarehouseId(order.getWarehouseId());
        adjust.setLocationId(confirmItem.getLocationId());
        adjust.setSkuId(item.getSkuId());
        adjust.setSkuCode(item.getSkuCode());
        adjust.setSkuName(item.getSkuName());
        adjust.setChangeQty(defectiveQty);
        adjust.setUnitCost(confirmItem.getUnitCost());
        adjust.setLogType(WmsConstants.LOG_RETURN_IN);
        adjust.setRefType(order.getRefType());
        adjust.setRefNo(order.getInboundNo());
        adjust.setRefId(order.getId());
        adjust.setOperatorId(request.getOperatorId());
        adjust.setOperatorName(request.getOperatorName());
        adjust.setRemark("不良品入库：" + confirmItem.getRemark());
        inventoryService.applyChange(adjust);
    }
}
