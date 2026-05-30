package com.lyf.supplychain.purchase.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.common.context.TenantContext;
import com.lyf.supplychain.common.exception.BusinessException;
import com.lyf.supplychain.purchase.constant.PurchaseOrderStateMachine;
import com.lyf.supplychain.purchase.constant.PurchaseStatus;
import com.lyf.supplychain.purchase.entity.PurchaseOrder;
import com.lyf.supplychain.purchase.entity.PurchaseOrderItem;
import com.lyf.supplychain.purchase.entity.PurchaseReceipt;
import com.lyf.supplychain.purchase.mapper.PurchaseOrderItemMapper;
import com.lyf.supplychain.purchase.mapper.PurchaseOrderMapper;
import com.lyf.supplychain.purchase.mapper.PurchaseReceiptMapper;
import com.lyf.supplychain.purchase.request.PurchaseItemRequest;
import com.lyf.supplychain.purchase.request.PurchaseOrderPageQuery;
import com.lyf.supplychain.purchase.request.PurchaseOrderRequest;
import com.lyf.supplychain.purchase.response.PurchaseOrderDetailResponse;
import com.lyf.supplychain.purchase.service.PurchaseNumberService;
import com.lyf.supplychain.purchase.service.PurchaseOrderService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * 采购订单服务实现。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Service
public class PurchaseOrderServiceImpl extends ServiceImpl<PurchaseOrderMapper, PurchaseOrder>
        implements PurchaseOrderService {

    private final PurchaseOrderItemMapper itemMapper;
    private final PurchaseReceiptMapper receiptMapper;
    private final PurchaseNumberService numberService;

    public PurchaseOrderServiceImpl(PurchaseOrderItemMapper itemMapper,
                                    PurchaseReceiptMapper receiptMapper,
                                    PurchaseNumberService numberService) {
        this.itemMapper = itemMapper;
        this.receiptMapper = receiptMapper;
        this.numberService = numberService;
    }

    /**
     * 分页查询采购订单。
     *
     * @param query 查询条件
     * @return 采购订单分页结果
     */
    @Override
    public PageResult<PurchaseOrder> pageOrders(PurchaseOrderPageQuery query) {
        query.normalize();
        Page<PurchaseOrder> page = page(Page.of(query.getPageNum(), query.getPageSize()),
                new LambdaQueryWrapper<PurchaseOrder>()
                        .eq(Objects.nonNull(query.getStatus()), PurchaseOrder::getStatus, query.getStatus())
                        .eq(Objects.nonNull(query.getSupplierId()), PurchaseOrder::getSupplierId, query.getSupplierId())
                        .like(Objects.nonNull(query.getPoNo()), PurchaseOrder::getPoNo, query.getPoNo())
                        .orderByDesc(PurchaseOrder::getCreateTime));
        return PageResult.from(page);
    }

    @Override
    public PurchaseOrderDetailResponse detail(Long id) {
        PurchaseOrder order = mustGet(id);
        PurchaseOrderDetailResponse response = new PurchaseOrderDetailResponse();
        response.setOrder(order);
        response.setItems(itemMapper.selectList(new LambdaQueryWrapper<PurchaseOrderItem>()
                .eq(PurchaseOrderItem::getPoId, id)));
        response.setReceipts(receiptMapper.selectList(new LambdaQueryWrapper<PurchaseReceipt>()
                .eq(PurchaseReceipt::getPoId, id)
                .orderByDesc(PurchaseReceipt::getReceiveDate)));
        return response;
    }

    /**
     * 创建采购订单和订单明细。
     *
     * @param request 创建请求
     * @return 采购订单ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(PurchaseOrderRequest request) {
        PurchaseOrder order = new PurchaseOrder();
        BeanUtils.copyProperties(request, order);
        order.setTenantId(TenantContext.getTenantId());
        order.setPoNo(numberService.nextNo("PO"));
        order.setStatus(PurchaseStatus.DRAFT);
        order.setOrderDate(request.getOrderDate() == null ? LocalDate.now() : request.getOrderDate());
        order.setCurrency(request.getCurrency() == null ? "CNY" : request.getCurrency());
        order.setExchangeRate(request.getExchangeRate() == null ? BigDecimal.ONE : request.getExchangeRate());
        order.setTaxAmount(request.getTaxAmount() == null ? BigDecimal.ZERO : request.getTaxAmount());
        order.setPaidAmount(BigDecimal.ZERO);
        order.setTotalAmount(calcTotalAmount(request));
        save(order);
        saveItems(order.getId(), request);
        return order.getId();
    }

    /**
     * 修改草稿状态的采购订单。
     *
     * @param id      采购订单ID
     * @param request 修改请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDraft(Long id, PurchaseOrderRequest request) {
        PurchaseOrder order = mustGet(id);
        if (!Objects.equals(order.getStatus(), PurchaseStatus.DRAFT)) {
            BusinessException.throwException("只有草稿状态的采购订单可以修改");
        }
        BeanUtils.copyProperties(request, order);
        order.setId(id);
        order.setTotalAmount(calcTotalAmount(request));
        updateById(order);
        itemMapper.delete(new LambdaQueryWrapper<PurchaseOrderItem>().eq(PurchaseOrderItem::getPoId, id));
        saveItems(id, request);
    }

    /**
     * 发送采购订单给供应商确认。
     *
     * @param id 采购订单ID
     */
    @Override
    public void send(Long id) {
        transit(id, PurchaseStatus.PO_WAIT_CONFIRM, "只有草稿状态的采购订单可以发送");
    }

    /**
     * 确认采购订单。
     *
     * @param id 采购订单ID
     */
    @Override
    public void confirm(Long id) {
        transit(id, PurchaseStatus.PO_CONFIRMED, "只有待确认状态的采购订单可以确认");
    }

    /**
     * 标记采购订单发货中。
     *
     * @param id 采购订单ID
     */
    @Override
    public void markShipping(Long id) {
        transit(id, PurchaseStatus.PO_SHIPPING, "只有已确认状态的采购订单可以标记发货");
    }

    /**
     * 标记采购订单已对账。
     *
     * @param id 采购订单ID
     */
    @Override
    public void reconcile(Long id) {
        transit(id, PurchaseStatus.PO_RECONCILED, "只有全部到货状态的采购订单可以对账");
    }

    /**
     * 取消未完结的采购订单。
     *
     * @param id 采购订单ID
     */
    @Override
    public void cancel(Long id) {
        PurchaseOrder order = mustGet(id);
        if (!PurchaseOrderStateMachine.canCancel(order.getStatus())) {
            BusinessException.throwException("已到货或已结清的采购订单不能取消");
        }
        update(new LambdaUpdateWrapper<PurchaseOrder>()
                .eq(PurchaseOrder::getId, id)
                .eq(PurchaseOrder::getStatus, order.getStatus())
                .set(PurchaseOrder::getStatus, PurchaseStatus.PO_CANCELLED));
    }

    /**
     * 查询供应商历史采购订单价格。
     *
     * @param supplierId 供应商ID
     * @param skuId      SKU ID
     * @return 历史采购订单列表
     */
    @Override
    public List<PurchaseOrder> priceHistory(Long supplierId, Long skuId) {
        List<Long> poIds = itemMapper.selectList(new LambdaQueryWrapper<PurchaseOrderItem>()
                        .eq(PurchaseOrderItem::getSkuId, skuId)
                        .select(PurchaseOrderItem::getPoId))
                .stream()
                .map(PurchaseOrderItem::getPoId)
                .distinct()
                .toList();
        if (poIds.isEmpty()) {
            return List.of();
        }
        return list(new LambdaQueryWrapper<PurchaseOrder>()
                .eq(PurchaseOrder::getSupplierId, supplierId)
                .in(PurchaseOrder::getId, poIds)
                .orderByDesc(PurchaseOrder::getOrderDate));
    }

    /**
     * 标记采购订单已结清。
     *
     * @param poId 采购订单ID
     */
    @Override
    public void markSettled(Long poId) {
        transit(poId, PurchaseStatus.PO_SETTLED, "只有全部到货或已对账状态的采购订单可以结清");
    }

    private void transit(Long id, Integer targetStatus, String message) {
        PurchaseOrder order = mustGet(id);
        if (!PurchaseOrderStateMachine.canTransit(order.getStatus(), targetStatus)) {
            BusinessException.throwException(message);
        }
        boolean updated = update(new LambdaUpdateWrapper<PurchaseOrder>()
                .eq(PurchaseOrder::getId, id)
                .eq(PurchaseOrder::getStatus, order.getStatus())
                .set(PurchaseOrder::getStatus, targetStatus));
        if (!updated) {
            BusinessException.throwException("采购订单状态已变化，请刷新后重试");
        }
    }

    private void saveItems(Long poId, PurchaseOrderRequest request) {
        for (PurchaseItemRequest itemRequest : request.getItems()) {
            PurchaseOrderItem item = new PurchaseOrderItem();
            BeanUtils.copyProperties(itemRequest, item);
            item.setTenantId(TenantContext.getTenantId());
            item.setPoId(poId);
            item.setUnit(itemRequest.getUnit() == null ? "件" : itemRequest.getUnit());
            item.setReceivedQty(0);
            item.setAmount(defaultAmount(itemRequest.getUnitPrice()).multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
            itemMapper.insert(item);
        }
    }

    private PurchaseOrder mustGet(Long id) {
        PurchaseOrder order = getById(id);
        if (order == null) {
            BusinessException.throwException("采购订单不存在");
        }
        return order;
    }

    private BigDecimal calcTotalAmount(PurchaseOrderRequest request) {
        if (request.getTotalAmount() != null) {
            return request.getTotalAmount();
        }
        return request.getItems().stream()
                .map(item -> defaultAmount(item.getUnitPrice()).multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal defaultAmount(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }
}
