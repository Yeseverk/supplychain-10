package com.lyf.supplychain.warehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.common.context.TenantContext;
import com.lyf.supplychain.common.exception.BusinessException;
import com.lyf.supplychain.warehouse.constant.WmsConstants;
import com.lyf.supplychain.warehouse.entity.Inventory;
import com.lyf.supplychain.warehouse.entity.StocktakeItem;
import com.lyf.supplychain.warehouse.entity.StocktakeTask;
import com.lyf.supplychain.warehouse.mapper.InventoryMapper;
import com.lyf.supplychain.warehouse.mapper.StocktakeItemMapper;
import com.lyf.supplychain.warehouse.mapper.StocktakeTaskMapper;
import com.lyf.supplychain.warehouse.request.InventoryAdjustRequest;
import com.lyf.supplychain.warehouse.request.StocktakeAuditRequest;
import com.lyf.supplychain.warehouse.request.StocktakeCountRequest;
import com.lyf.supplychain.warehouse.request.StocktakeTaskRequest;
import com.lyf.supplychain.warehouse.request.WmsItemRequest;
import com.lyf.supplychain.warehouse.request.WmsPageQuery;
import com.lyf.supplychain.warehouse.service.StocktakeService;
import com.lyf.supplychain.warehouse.service.WarehouseLocationService;
import com.lyf.supplychain.warehouse.service.WmsInventoryService;
import com.lyf.supplychain.warehouse.service.WmsNumberService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 库存盘点服务实现。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Service
public class StocktakeServiceImpl extends ServiceImpl<StocktakeTaskMapper, StocktakeTask>
        implements StocktakeService {

    private final StocktakeItemMapper itemMapper;
    private final InventoryMapper inventoryMapper;
    private final WmsNumberService numberService;
    private final WarehouseLocationService locationService;
    private final WmsInventoryService inventoryService;

    public StocktakeServiceImpl(StocktakeItemMapper itemMapper,
                                InventoryMapper inventoryMapper,
                                WmsNumberService numberService,
                                WarehouseLocationService locationService,
                                WmsInventoryService inventoryService) {
        this.itemMapper = itemMapper;
        this.inventoryMapper = inventoryMapper;
        this.numberService = numberService;
        this.locationService = locationService;
        this.inventoryService = inventoryService;
    }

    /**
     * 分页查询盘点任务。
     *
     * @param query 分页参数
     * @return 盘点任务分页结果
     */
    @Override
    public PageResult<StocktakeTask> pageStocktake(WmsPageQuery query) {
        query.normalize();
        LambdaQueryWrapper<StocktakeTask> wrapper = new LambdaQueryWrapper<StocktakeTask>()
                .orderByDesc(StocktakeTask::getCreateTime);
        if (query.getStatus() != null) {
            wrapper.eq(StocktakeTask::getStatus, query.getStatus());
        }
        if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
            String keyword = query.getKeyword().trim();
            wrapper.and(item -> item.like(StocktakeTask::getTaskNo, keyword)
                    .or().like(StocktakeTask::getTaskName, keyword)
                    .or().like(StocktakeTask::getRemark, keyword));
        }
        return PageResult.from(page(Page.of(query.getPageNum(), query.getPageSize()), wrapper));
    }

    /**
     * 创建盘点任务并生成账面快照。
     *
     * @param request 创建请求
     * @return 盘点任务ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(StocktakeTaskRequest request) {
        StocktakeTask task = new StocktakeTask();
        BeanUtils.copyProperties(request, task);
        task.setTenantId(TenantContext.getTenantId());
        task.setTaskNo(numberService.nextNo("ST"));
        task.setStatus(WmsConstants.STOCKTAKE_DOING);
        task.setStartTime(LocalDateTime.now());
        task.setDiffSkuCount(0);
        task.setProfitQty(0);
        task.setLossQty(0);
        task.setProfitAmount(BigDecimal.ZERO);
        task.setLossAmount(BigDecimal.ZERO);
        save(task);
        java.util.List<Inventory> inventories = inventoryMapper.selectList(new LambdaQueryWrapper<Inventory>()
                .eq(Inventory::getWarehouseId, request.getWarehouseId())
                .isNotNull(Inventory::getLocationId));
        for (Inventory inventory : inventories) {
            StocktakeItem item = new StocktakeItem();
            item.setTenantId(TenantContext.getTenantId());
            item.setTaskId(task.getId());
            item.setWarehouseId(inventory.getWarehouseId());
            item.setLocationId(inventory.getLocationId());
            item.setSkuId(inventory.getSkuId());
            item.setSkuCode(inventory.getSkuCode());
            item.setSkuName(inventory.getSkuName());
            item.setBookQty(inventory.getQuantity());
            item.setIsAdjusted(0);
            itemMapper.insert(item);
        }
        task.setTotalSkuCount(inventories.size());
        updateById(task);
        locationService.lockWarehouseLocations(request.getWarehouseId());
        return task.getId();
    }

    /**
     * 查询盘点任务明细。
     *
     * @param id 盘点任务ID
     * @return 盘点明细列表
     */
    @Override
    public java.util.List<StocktakeItem> items(Long id) {
        StocktakeTask task = getById(id);
        if (task == null) {
            BusinessException.throwException(13010, "盘点任务不存在");
        }
        return itemMapper.selectList(new LambdaQueryWrapper<StocktakeItem>()
                .eq(StocktakeItem::getTaskId, id)
                .orderByAsc(StocktakeItem::getSkuCode));
    }

    /**
     * 提交实盘数据。
     *
     * @param id      盘点任务ID
     * @param request 实盘请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void count(Long id, StocktakeCountRequest request) {
        StocktakeTask task = getById(id);
        if (task == null) {
            BusinessException.throwException(13010, "盘点任务不存在");
        }
        for (WmsItemRequest countItem : request.getItems()) {
            StocktakeItem item = itemMapper.selectById(countItem.getItemId());
            if (item == null) {
                BusinessException.throwException(13011, "该SKU未被纳入本次盘点");
            }
            int actualQty = countItem.getActualQty() == null ? countItem.getQuantity() : countItem.getActualQty();
            if (actualQty < 0) {
                BusinessException.throwException(13013, "实盘数量不能为负数");
            }
            item.setActualQty(actualQty);
            item.setDiffQty(actualQty - item.getBookQty());
            item.setDiffReason(countItem.getRemark());
            item.setPickerId(request.getPickerId());
            item.setPickTime(LocalDateTime.now());
            itemMapper.updateById(item);
        }
        task.setStatus(WmsConstants.STOCKTAKE_WAIT_AUDIT);
        updateById(task);
    }

    /**
     * 审核盘点差异并执行库存调整。
     *
     * @param id      盘点任务ID
     * @param request 审核请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(Long id, StocktakeAuditRequest request) {
        StocktakeTask task = getById(id);
        if (task == null) {
            BusinessException.throwException(13010, "盘点任务不存在");
        }
        int profitQty = 0;
        int lossQty = 0;
        int diffSkuCount = 0;
        for (StocktakeItem item : itemMapper.selectList(new LambdaQueryWrapper<StocktakeItem>()
                .eq(StocktakeItem::getTaskId, id))) {
            if (item.getDiffQty() == null || item.getDiffQty() == 0 || Objects.equals(item.getIsAdjusted(), 1)) {
                continue;
            }
            InventoryAdjustRequest adjust = new InventoryAdjustRequest();
            adjust.setWarehouseId(item.getWarehouseId());
            adjust.setLocationId(item.getLocationId());
            adjust.setSkuId(item.getSkuId());
            adjust.setSkuCode(item.getSkuCode());
            adjust.setSkuName(item.getSkuName());
            adjust.setChangeQty(item.getDiffQty());
            adjust.setLogType(item.getDiffQty() > 0 ? WmsConstants.LOG_STOCKTAKE_PROFIT : WmsConstants.LOG_STOCKTAKE_LOSS);
            adjust.setRefType("STOCKTAKE");
            adjust.setRefNo(task.getTaskNo());
            adjust.setRefId(task.getId());
            adjust.setOperatorId(request.getAuditorId());
            adjust.setOperatorName("盘点审核人");
            adjust.setRemark(item.getDiffReason());
            inventoryService.applyChange(adjust);
            item.setIsAdjusted(1);
            item.setAdjustTime(LocalDateTime.now());
            itemMapper.updateById(item);
            diffSkuCount++;
            if (item.getDiffQty() > 0) {
                profitQty += item.getDiffQty();
            } else {
                lossQty += Math.abs(item.getDiffQty());
            }
        }
        task.setAuditorId(request.getAuditorId());
        task.setAuditTime(LocalDateTime.now());
        task.setEndTime(LocalDateTime.now());
        task.setDiffSkuCount(diffSkuCount);
        task.setProfitQty(profitQty);
        task.setLossQty(lossQty);
        task.setStatus(WmsConstants.STOCKTAKE_DONE);
        updateById(task);
        locationService.unlockWarehouseLocations(task.getWarehouseId());
    }
}
