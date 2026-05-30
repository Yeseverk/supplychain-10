package com.lyf.supplychain.warehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.common.context.TenantContext;
import com.lyf.supplychain.common.event.WmsOutboundCompletedEvent;
import com.lyf.supplychain.common.exception.BusinessException;
import com.lyf.supplychain.warehouse.constant.WmsConstants;
import com.lyf.supplychain.warehouse.entity.Inventory;
import com.lyf.supplychain.warehouse.entity.OutboundOrder;
import com.lyf.supplychain.warehouse.entity.OutboundOrderItem;
import com.lyf.supplychain.warehouse.mapper.OutboundOrderItemMapper;
import com.lyf.supplychain.warehouse.mapper.OutboundOrderMapper;
import com.lyf.supplychain.warehouse.request.InventoryAdjustRequest;
import com.lyf.supplychain.warehouse.request.OutboundOrderRequest;
import com.lyf.supplychain.warehouse.request.PickProgressRequest;
import com.lyf.supplychain.warehouse.request.WmsItemRequest;
import com.lyf.supplychain.warehouse.request.WmsPageQuery;
import com.lyf.supplychain.warehouse.service.OutboundCompletedEventPublisher;
import com.lyf.supplychain.warehouse.service.OutboundOrderService;
import com.lyf.supplychain.warehouse.service.WmsInventoryService;
import com.lyf.supplychain.warehouse.service.WmsNumberService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 出库单服务实现。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Service
public class OutboundOrderServiceImpl extends ServiceImpl<OutboundOrderMapper, OutboundOrder>
        implements OutboundOrderService {

    private final OutboundOrderItemMapper itemMapper;
    private final WmsNumberService numberService;
    private final WmsInventoryService inventoryService;
    private final OutboundCompletedEventPublisher outboundCompletedEventPublisher;

    public OutboundOrderServiceImpl(OutboundOrderItemMapper itemMapper,
                                    WmsNumberService numberService,
                                    WmsInventoryService inventoryService,
                                    OutboundCompletedEventPublisher outboundCompletedEventPublisher) {
        this.itemMapper = itemMapper;
        this.numberService = numberService;
        this.inventoryService = inventoryService;
        this.outboundCompletedEventPublisher = outboundCompletedEventPublisher;
    }

    /**
     * 分页查询出库单。
     *
     * @param query 分页参数
     * @return 出库单分页结果
     */
    @Override
    public PageResult<OutboundOrder> pageOutbound(WmsPageQuery query) {
        query.normalize();
        LambdaQueryWrapper<OutboundOrder> wrapper = new LambdaQueryWrapper<OutboundOrder>()
                .orderByDesc(OutboundOrder::getCreateTime);
        if (query.getStatus() != null) {
            wrapper.eq(OutboundOrder::getStatus, query.getStatus());
        }
        if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
            String keyword = query.getKeyword().trim();
            wrapper.and(item -> item.like(OutboundOrder::getOutboundNo, keyword)
                    .or().like(OutboundOrder::getRefNo, keyword)
                    .or().like(OutboundOrder::getWarehouseName, keyword)
                    .or().like(OutboundOrder::getRemark, keyword));
        }
        return PageResult.from(page(Page.of(query.getPageNum(), query.getPageSize()), wrapper));
    }

    /**
     * 创建出库单并按 FIFO 分配库位。
     *
     * @param request 创建请求
     * @return 出库单ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(OutboundOrderRequest request) {
        if (hasText(request.getRefType()) && hasText(request.getRefNo())) {
            OutboundOrder exists = getOne(new LambdaQueryWrapper<OutboundOrder>()
                    .eq(OutboundOrder::getRefType, request.getRefType())
                    .eq(OutboundOrder::getRefNo, request.getRefNo())
                    .last("limit 1"));
            if (exists != null) {
                return exists.getId();
            }
        }
        OutboundOrder order = new OutboundOrder();
        BeanUtils.copyProperties(request, order);
        order.setTenantId(TenantContext.getTenantId());
        order.setOutboundNo(numberService.nextNo("OUT"));
        order.setStatus(WmsConstants.OUTBOUND_WAIT_PICK);
        order.setTotalQty(request.getItems().stream().mapToInt(WmsItemRequest::getQuantity).sum());
        save(order);
        for (WmsItemRequest itemRequest : request.getItems()) {
            List<Inventory> allocations = inventoryService.allocateFifo(request.getWarehouseId(),
                    itemRequest.getSkuId(), itemRequest.getQuantity());
            for (Inventory allocation : allocations) {
                OutboundOrderItem item = new OutboundOrderItem();
                item.setTenantId(TenantContext.getTenantId());
                item.setOutboundId(order.getId());
                item.setSkuId(itemRequest.getSkuId());
                item.setSkuCode(itemRequest.getSkuCode());
                item.setSkuName(itemRequest.getSkuName());
                item.setQuantity(allocation.getAvailableQty());
                item.setPickedQty(0);
                item.setLocationId(allocation.getLocationId());
                item.setPickStatus(0);
                item.setRemark("FIFO分配库位，按入库时间和路径排序");
                itemMapper.insert(item);
            }
        }
        return order.getId();
    }

    /**
     * 查询拣货单明细。
     *
     * @param id 出库单ID
     * @return 拣货明细
     */
    @Override
    public List<OutboundOrderItem> pickList(Long id) {
        return itemMapper.selectList(new LambdaQueryWrapper<OutboundOrderItem>()
                .eq(OutboundOrderItem::getOutboundId, id)
                .orderByAsc(OutboundOrderItem::getLocationCode, OutboundOrderItem::getSkuCode));
    }

    /**
     * 更新拣货进度。
     *
     * @param id      出库单ID
     * @param request 拣货进度请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePick(Long id, PickProgressRequest request) {
        OutboundOrder order = getById(id);
        if (order == null) {
            BusinessException.throwException("出库单不存在");
        }
        order.setStatus(WmsConstants.OUTBOUND_PICKING);
        order.setPickUserId(request.getPickUserId());
        if (order.getPickStartTime() == null) {
            order.setPickStartTime(LocalDateTime.now());
        }
        for (WmsItemRequest itemRequest : request.getItems()) {
            OutboundOrderItem item = itemMapper.selectById(itemRequest.getItemId());
            if (item != null) {
                item.setPickedQty(itemRequest.getActualQty() == null ? itemRequest.getQuantity() : itemRequest.getActualQty());
                item.setPickStatus(Objects.equals(item.getPickedQty(), item.getQuantity()) ? 1 : 2);
                itemMapper.updateById(item);
            }
        }
        boolean allPicked = pickList(id).stream().allMatch(item -> item.getPickedQty() >= item.getQuantity());
        if (allPicked) {
            order.setStatus(WmsConstants.OUTBOUND_WAIT_REVIEW);
            order.setPickEndTime(LocalDateTime.now());
        }
        updateById(order);
    }

    /**
     * 确认出库并扣减库存。
     *
     * @param id 出库单ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirm(Long id) {
        OutboundOrder order = getById(id);
        if (order == null) {
            BusinessException.throwException("出库单不存在");
        }
        if (Objects.equals(order.getStatus(), WmsConstants.OUTBOUND_DONE)) {
            return;
        }
        for (OutboundOrderItem item : pickList(id)) {
            InventoryAdjustRequest adjust = new InventoryAdjustRequest();
            adjust.setWarehouseId(order.getWarehouseId());
            adjust.setLocationId(item.getLocationId());
            adjust.setSkuId(item.getSkuId());
            adjust.setSkuCode(item.getSkuCode());
            adjust.setSkuName(item.getSkuName());
            adjust.setChangeQty(-Math.abs(item.getQuantity()));
            adjust.setLogType(order.getOutboundType() == 2 ? WmsConstants.LOG_TRANSFER_OUT : WmsConstants.LOG_SALE_OUT);
            adjust.setRefType(order.getRefType());
            adjust.setRefNo(order.getOutboundNo());
            adjust.setRefId(order.getId());
            adjust.setOperatorId(order.getPickUserId());
            adjust.setOperatorName("仓管员");
            adjust.setRemark("确认出库，CAS扣减库存防止超卖");
            inventoryService.applyChange(adjust);
            item.setPickedQty(item.getQuantity());
            item.setPickStatus(1);
            itemMapper.updateById(item);
        }
        order.setActualDate(LocalDate.now());
        order.setStatus(WmsConstants.OUTBOUND_DONE);
        updateById(order);
        publishOutboundCompletedAfterCommit(order, pickList(id));
    }

    private void publishOutboundCompletedAfterCommit(OutboundOrder order, List<OutboundOrderItem> items) {
        if (!"SALES_ORDER".equals(order.getRefType())) {
            return;
        }
        WmsOutboundCompletedEvent event = buildOutboundCompletedEvent(order, items);
        Runnable publishTask = () -> outboundCompletedEventPublisher.publish(event);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publishTask.run();
                }
            });
            return;
        }
        publishTask.run();
    }

    private WmsOutboundCompletedEvent buildOutboundCompletedEvent(OutboundOrder order, List<OutboundOrderItem> items) {
        WmsOutboundCompletedEvent event = new WmsOutboundCompletedEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setTenantId(TenantContext.getTenantId());
        event.setOutboundId(order.getId());
        event.setOutboundNo(order.getOutboundNo());
        event.setOrderId(order.getRefId());
        event.setOrderNo(order.getRefNo());
        event.setOutboundDate(order.getActualDate());
        event.setOccurredTime(LocalDateTime.now());
        event.setItems(items.stream().map(this::buildOutboundCompletedItem).toList());
        return event;
    }

    private WmsOutboundCompletedEvent.Item buildOutboundCompletedItem(OutboundOrderItem item) {
        WmsOutboundCompletedEvent.Item eventItem = new WmsOutboundCompletedEvent.Item();
        eventItem.setSkuId(item.getSkuId());
        eventItem.setSkuCode(item.getSkuCode());
        eventItem.setSkuName(item.getSkuName());
        eventItem.setQuantity(item.getQuantity());
        eventItem.setLocationId(item.getLocationId());
        return eventItem;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
