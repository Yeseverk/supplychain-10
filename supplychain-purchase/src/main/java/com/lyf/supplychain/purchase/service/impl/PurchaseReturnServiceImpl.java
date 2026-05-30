package com.lyf.supplychain.purchase.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyf.supplychain.common.api.PageQuery;
import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.common.context.TenantContext;
import com.lyf.supplychain.common.exception.BusinessException;
import com.lyf.supplychain.common.feign.finance.FinanceFeignClient;
import com.lyf.supplychain.common.feign.finance.FinancePayableOffsetRequest;
import com.lyf.supplychain.common.feign.warehouse.WarehouseFeignClient;
import com.lyf.supplychain.common.feign.warehouse.WarehouseOutboundRequest;
import com.lyf.supplychain.common.feign.warehouse.WarehouseStockItem;
import com.lyf.supplychain.purchase.entity.PurchaseOrder;
import com.lyf.supplychain.purchase.entity.PurchaseOrderItem;
import com.lyf.supplychain.purchase.entity.PurchaseReturn;
import com.lyf.supplychain.purchase.mapper.PurchaseOrderItemMapper;
import com.lyf.supplychain.purchase.mapper.PurchaseReturnMapper;
import com.lyf.supplychain.purchase.request.PurchaseReturnRequest;
import com.lyf.supplychain.purchase.service.PurchaseNumberService;
import com.lyf.supplychain.purchase.service.PurchaseOrderService;
import com.lyf.supplychain.purchase.service.PurchaseReturnService;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 采购退货服务实现。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Service
public class PurchaseReturnServiceImpl extends ServiceImpl<PurchaseReturnMapper, PurchaseReturn>
        implements PurchaseReturnService {

    private final PurchaseOrderService orderService;
    private final PurchaseOrderItemMapper orderItemMapper;
    private final PurchaseNumberService numberService;
    private final WarehouseFeignClient warehouseFeignClient;
    private final FinanceFeignClient financeFeignClient;

    public PurchaseReturnServiceImpl(PurchaseOrderService orderService,
                                     PurchaseOrderItemMapper orderItemMapper,
                                     PurchaseNumberService numberService,
                                     WarehouseFeignClient warehouseFeignClient,
                                     FinanceFeignClient financeFeignClient) {
        this.orderService = orderService;
        this.orderItemMapper = orderItemMapper;
        this.numberService = numberService;
        this.warehouseFeignClient = warehouseFeignClient;
        this.financeFeignClient = financeFeignClient;
    }

    /**
     * 创建采购退货单。
     *
     * @param request 退货请求
     * @return 退货单ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(PurchaseReturnRequest request) {
        PurchaseOrder order = orderService.getById(request.getPoId());
        if (order == null) {
            BusinessException.throwException("采购订单不存在");
        }
        PurchaseReturn purchaseReturn = new PurchaseReturn();
        BeanUtils.copyProperties(request, purchaseReturn);
        purchaseReturn.setTenantId(TenantContext.getTenantId());
        purchaseReturn.setReturnNo(numberService.nextNo("RTN"));
        purchaseReturn.setPoNo(order.getPoNo());
        purchaseReturn.setSupplierId(order.getSupplierId());
        purchaseReturn.setWarehouseId(order.getWarehouseId());
        purchaseReturn.setStatus(0);
        save(purchaseReturn);
        return purchaseReturn.getId();
    }

    /**
     * 分页查询采购退货单。
     *
     * @param query 分页参数
     * @return 退货单分页结果
     */
    @Override
    public PageResult<PurchaseReturn> pageReturns(PageQuery query) {
        query.normalize();
        Page<PurchaseReturn> page = page(Page.of(query.getPageNum(), query.getPageSize()));
        return PageResult.from(page);
    }

    /**
     * 确认退货出库，并通过 Feign 联动仓储扣减库存。
     *
     * @param returnId 退货单ID
     */
    @Override
    @GlobalTransactional(name = "purchase-return-ship", rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public void ship(Long returnId) {
        PurchaseReturn purchaseReturn = getById(returnId);
        if (purchaseReturn == null) {
            BusinessException.throwException("退货单不存在");
        }
        if (purchaseReturn.getStatus() >= 3) {
            return;
        }
        PurchaseOrderItem firstItem = orderItemMapper.selectList(
                        com.baomidou.mybatisplus.core.toolkit.Wrappers.<PurchaseOrderItem>lambdaQuery()
                                .eq(PurchaseOrderItem::getPoId, purchaseReturn.getPoId())
                                .last("limit 1"))
                .stream()
                .findFirst()
                .orElseThrow(() -> new BusinessException("采购订单明细不存在"));
        WarehouseStockItem stockItem = new WarehouseStockItem();
        stockItem.setSkuId(firstItem.getSkuId());
        stockItem.setSkuCode(firstItem.getSkuCode());
        stockItem.setSkuName(firstItem.getSkuName());
        stockItem.setQuantity(purchaseReturn.getReturnQty());
        WarehouseOutboundRequest request = new WarehouseOutboundRequest();
        request.setTenantId(purchaseReturn.getTenantId());
        request.setWarehouseId(purchaseReturn.getWarehouseId());
        request.setBizNo(purchaseReturn.getReturnNo());
        request.setBizType("RETURN_OUT");
        request.setItems(List.of(stockItem));
        assertSuccess(warehouseFeignClient.outbound(request), "WMS退货出库失败");
        assertSuccess(financeFeignClient.offsetPayable(toOffsetRequest(purchaseReturn)), "FMS应付冲减失败");
        purchaseReturn.setStatus(3);
        updateById(purchaseReturn);
    }

    private void assertSuccess(R<?> response, String message) {
        if (response == null || response.getCode() == null || response.getCode() != 200) {
            BusinessException.throwException(message + "：" + (response == null ? "无响应" : response.getMsg()));
        }
    }

    private FinancePayableOffsetRequest toOffsetRequest(PurchaseReturn purchaseReturn) {
        FinancePayableOffsetRequest request = new FinancePayableOffsetRequest();
        request.setTenantId(purchaseReturn.getTenantId());
        request.setPoId(purchaseReturn.getPoId());
        request.setReturnNo(purchaseReturn.getReturnNo());
        request.setOffsetAmount(purchaseReturn.getReturnAmount());
        request.setReason("采购退货出库自动冲减应付");
        return request;
    }
}
