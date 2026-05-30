package com.lyf.supplychain.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyf.supplychain.common.api.PageQuery;
import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.common.context.TenantContext;
import com.lyf.supplychain.common.exception.BusinessException;
import com.lyf.supplychain.order.entity.OrderItem;
import com.lyf.supplychain.order.entity.OrderMain;
import com.lyf.supplychain.order.entity.PlatformInventoryAllocation;
import com.lyf.supplychain.order.mapper.PlatformInventoryAllocationMapper;
import com.lyf.supplychain.order.request.OrderCreateRequest;
import com.lyf.supplychain.order.request.OrderItemRequest;
import com.lyf.supplychain.order.request.PlatformInventoryAllocationAdjustRequest;
import com.lyf.supplychain.order.request.PlatformInventoryAllocationRequest;
import com.lyf.supplychain.order.service.PlatformInventoryAllocationService;
import com.lyf.supplychain.order.service.PlatformInventorySyncGateway;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 多平台库存分配服务实现。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Service
public class PlatformInventoryAllocationServiceImpl implements PlatformInventoryAllocationService {

    private static final int STATUS_ENABLED = 1;
    private static final int SYNC_SUCCESS = 1;
    private static final int SYNC_FAILED = 2;

    private final PlatformInventoryAllocationMapper allocationMapper;
    private final PlatformInventorySyncGateway syncGateway;

    public PlatformInventoryAllocationServiceImpl(PlatformInventoryAllocationMapper allocationMapper,
                                                  PlatformInventorySyncGateway syncGateway) {
        this.allocationMapper = allocationMapper;
        this.syncGateway = syncGateway;
    }

    /**
     * 分页查询平台库存分配。
     *
     * @param query 分页参数
     * @return 平台库存分配分页数据
     */
    @Override
    public PageResult<PlatformInventoryAllocation> page(PageQuery query) {
        query.normalize();
        return PageResult.from(allocationMapper.selectPage(Page.of(query.getPageNum(), query.getPageSize()),
                new LambdaQueryWrapper<PlatformInventoryAllocation>().orderByDesc(PlatformInventoryAllocation::getUpdateTime)));
    }

    /**
     * 创建平台库存分配并初始化可售库存。
     *
     * @param request 保存请求
     * @return 分配记录ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(PlatformInventoryAllocationRequest request) {
        PlatformInventoryAllocation allocation = new PlatformInventoryAllocation();
        allocation.setTenantId(TenantContext.getTenantId());
        allocation.setSkuId(request.getSkuId());
        allocation.setSkuCode(request.getSkuCode());
        allocation.setPlatform(request.getPlatform());
        allocation.setStoreId(normalizeStoreId(request.getStoreId()));
        allocation.setAllocatedQty(request.getAllocatedQty());
        allocation.setFrozenQty(0);
        allocation.setSoldQty(0);
        allocation.setAvailableQty(request.getAllocatedQty());
        allocation.setAllocationRatio(request.getAllocationRatio());
        allocation.setStatus(STATUS_ENABLED);
        allocation.setLastSyncStatus(0);
        allocationMapper.insert(allocation);
        return allocation.getId();
    }

    /**
     * 调整平台库存分配，并按已冻结和已售数量重算可售库存。
     *
     * @param id      分配记录ID
     * @param request 调整请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adjust(Long id, PlatformInventoryAllocationAdjustRequest request) {
        PlatformInventoryAllocation allocation = detail(id);
        int frozenQty = value(allocation.getFrozenQty());
        int soldQty = value(allocation.getSoldQty());
        int availableQty = request.getAllocatedQty() - frozenQty - soldQty;
        if (availableQty < 0) {
            BusinessException.throwException(15012, "调整后的平台库存小于已冻结和已售数量");
        }
        allocation.setAllocatedQty(request.getAllocatedQty());
        allocation.setAvailableQty(availableQty);
        allocation.setAllocationRatio(request.getAllocationRatio());
        allocationMapper.updateById(allocation);
    }

    /**
     * 同步单个平台库存到外部平台。
     *
     * @param id 分配记录ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sync(Long id) {
        PlatformInventoryAllocation allocation = detail(id);
        try {
            allocation.setLastSyncMessage(syncGateway.syncInventory(allocation));
            allocation.setLastSyncStatus(SYNC_SUCCESS);
        } catch (RuntimeException exception) {
            allocation.setLastSyncStatus(SYNC_FAILED);
            allocation.setLastSyncMessage(exception.getMessage());
            throw exception;
        } finally {
            allocation.setLastSyncTime(LocalDateTime.now());
            allocationMapper.updateById(allocation);
        }
    }

    /**
     * 订单创建时冻结平台可售配额，使用 CAS 条件避免并发超卖。
     *
     * @param request 订单创建请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void freezeForOrder(OrderCreateRequest request) {
        for (OrderItemRequest item : request.getItems()) {
            if (!hasAllocation(request.getPlatform(), request.getStoreId(), item.getSkuId())) {
                continue;
            }
            int updated = allocationMapper.update(null, baseUpdate(request.getPlatform(), request.getStoreId(), item.getSkuId())
                    .ge(PlatformInventoryAllocation::getAvailableQty, item.getQuantity())
                    .setSql("available_qty = available_qty - " + item.getQuantity())
                    .setSql("frozen_qty = frozen_qty + " + item.getQuantity()));
            if (updated == 0) {
                BusinessException.throwException(15012, "平台库存配额不足或未配置，SKU=" + item.getSkuCode());
            }
        }
    }

    /**
     * 订单取消时释放已冻结的平台库存。
     *
     * @param order 订单主表
     * @param items 订单明细
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void releaseForCancel(OrderMain order, List<OrderItem> items) {
        for (OrderItem item : items) {
            if (!hasAllocation(order.getPlatform(), order.getStoreId(), item.getSkuId())) {
                continue;
            }
            int updated = allocationMapper.update(null, baseUpdate(order.getPlatform(), order.getStoreId(), item.getSkuId())
                    .ge(PlatformInventoryAllocation::getFrozenQty, item.getQuantity())
                    .setSql("frozen_qty = frozen_qty - " + item.getQuantity())
                    .setSql("available_qty = available_qty + " + item.getQuantity()));
            if (updated == 0) {
                BusinessException.throwException(15013, "平台冻结库存释放失败，SKU=" + item.getSkuCode());
            }
        }
    }

    /**
     * 订单出库后确认平台库存售出。
     *
     * @param order 订单主表
     * @param items 订单明细
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmShipment(OrderMain order, List<OrderItem> items) {
        for (OrderItem item : items) {
            if (!hasAllocation(order.getPlatform(), order.getStoreId(), item.getSkuId())) {
                continue;
            }
            int updated = allocationMapper.update(null, baseUpdate(order.getPlatform(), order.getStoreId(), item.getSkuId())
                    .ge(PlatformInventoryAllocation::getFrozenQty, item.getQuantity())
                    .setSql("frozen_qty = frozen_qty - " + item.getQuantity())
                    .setSql("sold_qty = sold_qty + " + item.getQuantity()));
            if (updated == 0) {
                BusinessException.throwException(15014, "平台库存销售确认失败，SKU=" + item.getSkuCode());
            }
        }
    }

    private PlatformInventoryAllocation detail(Long id) {
        PlatformInventoryAllocation allocation = allocationMapper.selectById(id);
        if (allocation == null) {
            BusinessException.throwException(15011, "平台库存分配不存在");
        }
        return allocation;
    }

    private LambdaUpdateWrapper<PlatformInventoryAllocation> baseUpdate(String platform, Long storeId, Long skuId) {
        LambdaUpdateWrapper<PlatformInventoryAllocation> wrapper = new LambdaUpdateWrapper<PlatformInventoryAllocation>()
                .eq(PlatformInventoryAllocation::getTenantId, TenantContext.getTenantId())
                .eq(PlatformInventoryAllocation::getPlatform, platform)
                .eq(PlatformInventoryAllocation::getSkuId, skuId)
                .eq(PlatformInventoryAllocation::getStatus, STATUS_ENABLED);
        wrapper.eq(PlatformInventoryAllocation::getStoreId, normalizeStoreId(storeId));
        return wrapper;
    }

    private boolean hasAllocation(String platform, Long storeId, Long skuId) {
        return allocationMapper.selectCount(new LambdaQueryWrapper<PlatformInventoryAllocation>()
                .eq(PlatformInventoryAllocation::getTenantId, TenantContext.getTenantId())
                .eq(PlatformInventoryAllocation::getPlatform, platform)
                .eq(PlatformInventoryAllocation::getStoreId, normalizeStoreId(storeId))
                .eq(PlatformInventoryAllocation::getSkuId, skuId)
                .eq(PlatformInventoryAllocation::getStatus, STATUS_ENABLED)) > 0;
    }

    private Long normalizeStoreId(Long storeId) {
        return storeId == null ? 0L : storeId;
    }

    private int value(Integer value) {
        return value == null ? 0 : value;
    }
}
