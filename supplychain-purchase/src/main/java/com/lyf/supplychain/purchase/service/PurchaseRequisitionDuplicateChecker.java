package com.lyf.supplychain.purchase.service;

import com.lyf.supplychain.common.context.TenantContext;
import com.lyf.supplychain.common.exception.BusinessException;
import com.lyf.supplychain.purchase.mapper.PurchaseOrderMapper;
import com.lyf.supplychain.purchase.mapper.PurchaseRequisitionMapper;
import com.lyf.supplychain.purchase.request.PurchaseItemRequest;
import com.lyf.supplychain.purchase.request.PurchaseRequisitionRequest;
import org.springframework.stereotype.Component;

/**
 * 采购申请去重检查器。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Component
public class PurchaseRequisitionDuplicateChecker {

    private final PurchaseRequisitionMapper requisitionMapper;

    private final PurchaseOrderMapper orderMapper;

    public PurchaseRequisitionDuplicateChecker(PurchaseRequisitionMapper requisitionMapper,
                                               PurchaseOrderMapper orderMapper) {
        this.requisitionMapper = requisitionMapper;
        this.orderMapper = orderMapper;
    }

    /**
     * 校验同租户、同仓库、同 SKU 是否已有足够进行中的采购数量。
     *
     * @param request 采购申请请求
     */
    public void check(PurchaseRequisitionRequest request) {
        Long tenantId = TenantContext.getTenantId();
        for (PurchaseItemRequest item : request.getItems()) {
            int ongoingQty = safeQty(requisitionMapper.sumOngoingQty(tenantId, request.getWarehouseId(), item.getSkuId()))
                    + safeQty(orderMapper.sumOngoingQty(tenantId, request.getWarehouseId(), item.getSkuId()));
            if (ongoingQty >= safeQty(item.getQuantity())) {
                BusinessException.throwException("SKU【" + item.getSkuCode() + "】已存在进行中的采购申请或采购单，请避免重复采购");
            }
        }
    }

    private int safeQty(Integer quantity) {
        return quantity == null ? 0 : quantity;
    }
}
