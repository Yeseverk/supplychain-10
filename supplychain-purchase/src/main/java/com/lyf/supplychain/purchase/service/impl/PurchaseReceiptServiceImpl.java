package com.lyf.supplychain.purchase.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.hutool.core.util.StrUtil;
import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.common.context.TenantContext;
import com.lyf.supplychain.common.exception.BusinessException;
import com.lyf.supplychain.common.feign.finance.FinanceFeignClient;
import com.lyf.supplychain.common.feign.finance.FinancePayableCreateRequest;
import com.lyf.supplychain.common.feign.warehouse.WarehouseFeignClient;
import com.lyf.supplychain.common.feign.warehouse.WarehouseInboundRequest;
import com.lyf.supplychain.common.feign.warehouse.WarehouseStockItem;
import com.lyf.supplychain.purchase.constant.PurchaseStatus;
import com.lyf.supplychain.purchase.entity.PurchaseOrder;
import com.lyf.supplychain.purchase.entity.PurchaseOrderItem;
import com.lyf.supplychain.purchase.entity.PurchaseReceipt;
import com.lyf.supplychain.purchase.entity.PurchaseReceiptItem;
import com.lyf.supplychain.purchase.mapper.PurchaseOrderItemMapper;
import com.lyf.supplychain.purchase.mapper.PurchaseReceiptItemMapper;
import com.lyf.supplychain.purchase.mapper.PurchaseReceiptMapper;
import com.lyf.supplychain.purchase.request.PurchaseReceiptItemRequest;
import com.lyf.supplychain.purchase.request.PurchaseReceiptPageQuery;
import com.lyf.supplychain.purchase.request.PurchaseReceiptRequest;
import com.lyf.supplychain.purchase.service.PurchaseNumberService;
import com.lyf.supplychain.purchase.service.PurchaseOrderService;
import com.lyf.supplychain.purchase.service.PurchaseReceiptService;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 采购收货服务实现。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Service
public class PurchaseReceiptServiceImpl extends ServiceImpl<PurchaseReceiptMapper, PurchaseReceipt>
        implements PurchaseReceiptService {

    private final PurchaseReceiptItemMapper receiptItemMapper;
    private final PurchaseOrderItemMapper orderItemMapper;
    private final PurchaseOrderService orderService;
    private final PurchaseNumberService numberService;
    private final WarehouseFeignClient warehouseFeignClient;
    private final FinanceFeignClient financeFeignClient;

    public PurchaseReceiptServiceImpl(PurchaseReceiptItemMapper receiptItemMapper,
                                      PurchaseOrderItemMapper orderItemMapper,
                                      PurchaseOrderService orderService,
                                      PurchaseNumberService numberService,
                                      WarehouseFeignClient warehouseFeignClient,
                                      FinanceFeignClient financeFeignClient) {
        this.receiptItemMapper = receiptItemMapper;
        this.orderItemMapper = orderItemMapper;
        this.orderService = orderService;
        this.numberService = numberService;
        this.warehouseFeignClient = warehouseFeignClient;
        this.financeFeignClient = financeFeignClient;
    }

    /**
     * 创建采购收货单和收货明细。
     *
     * @param request 收货请求
     * @return 收货单ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(PurchaseReceiptRequest request) {
        PurchaseOrder order = orderService.getById(request.getPoId());
        if (order == null) {
            BusinessException.throwException("采购订单不存在");
        }
        PurchaseReceipt receipt = new PurchaseReceipt();
        BeanUtils.copyProperties(request, receipt);
        receipt.setTenantId(TenantContext.getTenantId());
        receipt.setReceiptNo(numberService.nextNo("RCV"));
        receipt.setPoNo(order.getPoNo());
        receipt.setSupplierId(order.getSupplierId());
        receipt.setWarehouseId(order.getWarehouseId());
        receipt.setStatus(0);
        receipt.setTotalQty(request.getItems().stream().mapToInt(PurchaseReceiptItemRequest::getActualQty).sum());
        receipt.setPassQty(request.getItems().stream().mapToInt(item -> item.getPassQty() == null ? 0 : item.getPassQty()).sum());
        receipt.setRejectQty(request.getItems().stream().mapToInt(item -> item.getRejectQty() == null ? 0 : item.getRejectQty()).sum());
        receipt.setIsOnTime(calcOnTime(order, request.getReceiveDate()));
        save(receipt);
        for (PurchaseReceiptItemRequest itemRequest : request.getItems()) {
            PurchaseOrderItem orderItem = orderItemMapper.selectById(itemRequest.getPoItemId());
            if (orderItem == null) {
                BusinessException.throwException("采购订单明细不存在");
            }
            PurchaseReceiptItem item = new PurchaseReceiptItem();
            BeanUtils.copyProperties(itemRequest, item);
            item.setTenantId(TenantContext.getTenantId());
            item.setReceiptId(receipt.getId());
            item.setSkuId(orderItem.getSkuId());
            item.setSkuCode(orderItem.getSkuCode());
            item.setSkuName(orderItem.getSkuName());
            item.setPassQty(itemRequest.getPassQty() == null ? itemRequest.getActualQty() : itemRequest.getPassQty());
            item.setRejectQty(itemRequest.getRejectQty() == null ? 0 : itemRequest.getRejectQty());
            item.setStatus(0);
            receiptItemMapper.insert(item);
        }
        return receipt.getId();
    }

    /**
     * 分页查询采购收货单。
     *
     * @param query 分页参数
     * @return 收货单分页结果
     */
    @Override
    public PageResult<PurchaseReceipt> pageReceipts(PurchaseReceiptPageQuery query) {
        query.normalize();
        LambdaQueryWrapper<PurchaseReceipt> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(query.getKeyword())) {
            String keyword = query.getKeyword().trim();
            wrapper.and(item -> item.like(PurchaseReceipt::getReceiptNo, keyword)
                    .or().like(PurchaseReceipt::getPoNo, keyword)
                    .or().like(PurchaseReceipt::getReceiverName, keyword)
                    .or().like(PurchaseReceipt::getRemark, keyword));
        }
        if (query.getStatus() != null) {
            wrapper.eq(PurchaseReceipt::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(PurchaseReceipt::getCreateTime);
        Page<PurchaseReceipt> page = page(Page.of(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResult.from(page);
    }

    /**
     * 确认入库，并通过 Seata 联动仓储库存和财务应付账款。
     *
     * @param receiptId 收货单ID
     */
    @Override
    @GlobalTransactional(name = "purchase-confirm-inbound", rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public void confirmInbound(Long receiptId) {
        PurchaseReceipt receipt = getById(receiptId);
        if (receipt == null) {
            BusinessException.throwException("收货单不存在");
        }
        if (receipt.getStatus() >= 3) {
            return;
        }
        PurchaseOrder order = orderService.getById(receipt.getPoId());
        if (order == null) {
            BusinessException.throwException("采购订单不存在");
        }
        List<PurchaseReceiptItem> receiptItems = receiptItemMapper.selectList(
                new LambdaQueryWrapper<PurchaseReceiptItem>().eq(PurchaseReceiptItem::getReceiptId, receiptId));
        List<WarehouseStockItem> stockItems = new ArrayList<>();
        for (PurchaseReceiptItem receiptItem : receiptItems) {
            PurchaseOrderItem orderItem = orderItemMapper.selectById(receiptItem.getPoItemId());
            int newReceivedQty = orderItem.getReceivedQty() + receiptItem.getPassQty();
            if (newReceivedQty > orderItem.getQuantity()) {
                BusinessException.throwException("累计收货数量不能大于采购数量");
            }
            orderItem.setReceivedQty(newReceivedQty);
            orderItemMapper.updateById(orderItem);
            receiptItem.setStatus(1);
            receiptItemMapper.updateById(receiptItem);
            stockItems.add(toStockItem(receiptItem));
        }
        receipt.setStatus(3);
        updateById(receipt);
        updateOrderReceivedStatus(order.getId());
        WarehouseInboundRequest inboundRequest = new WarehouseInboundRequest();
        inboundRequest.setTenantId(receipt.getTenantId());
        inboundRequest.setWarehouseId(receipt.getWarehouseId());
        inboundRequest.setBizNo(receipt.getReceiptNo());
        inboundRequest.setBizType("PURCHASE_IN");
        inboundRequest.setItems(stockItems);
        assertSuccess(warehouseFeignClient.inbound(inboundRequest), "WMS入库失败");
        if (isAllReceived(order.getId())) {
            assertSuccess(financeFeignClient.createPayable(toPayableRequest(order)), "FMS应付生成失败");
        }
    }

    private void assertSuccess(R<?> response, String message) {
        if (response == null || response.getCode() == null || response.getCode() != 200) {
            BusinessException.throwException(message + "：" + (response == null ? "无响应" : response.getMsg()));
        }
    }

    private WarehouseStockItem toStockItem(PurchaseReceiptItem receiptItem) {
        WarehouseStockItem item = new WarehouseStockItem();
        item.setSkuId(receiptItem.getSkuId());
        item.setSkuCode(receiptItem.getSkuCode());
        item.setSkuName(receiptItem.getSkuName());
        item.setLocationId(receiptItem.getLocationId());
        item.setQuantity(receiptItem.getPassQty());
        return item;
    }

    private FinancePayableCreateRequest toPayableRequest(PurchaseOrder order) {
        FinancePayableCreateRequest request = new FinancePayableCreateRequest();
        request.setTenantId(order.getTenantId());
        request.setPoId(order.getId());
        request.setPoNo(order.getPoNo());
        request.setSupplierId(order.getSupplierId());
        request.setSupplierName(order.getSupplierName());
        request.setInvoiceNo(order.getInvoiceNo());
        request.setInvoiceDate(LocalDate.now());
        request.setPayableAmount(order.getTotalAmount());
        request.setCurrency(order.getCurrency());
        request.setPaymentDays(order.getPaymentDays());
        return request;
    }

    private void updateOrderReceivedStatus(Long poId) {
        int status = isAllReceived(poId) ? PurchaseStatus.PO_ALL_RECEIVED : PurchaseStatus.PO_PART_RECEIVED;
        orderService.update(new LambdaUpdateWrapper<PurchaseOrder>()
                .eq(PurchaseOrder::getId, poId)
                .set(PurchaseOrder::getStatus, status));
    }

    private boolean isAllReceived(Long poId) {
        return orderItemMapper.selectList(new LambdaQueryWrapper<PurchaseOrderItem>()
                        .eq(PurchaseOrderItem::getPoId, poId))
                .stream()
                .allMatch(item -> item.getReceivedQty() >= item.getQuantity());
    }

    private Integer calcOnTime(PurchaseOrder order, LocalDate receiveDate) {
        if (order.getConfirmedDate() == null || receiveDate == null) {
            return null;
        }
        return receiveDate.isAfter(order.getConfirmedDate()) ? 0 : 1;
    }
}
