package com.lyf.supplychain.purchase.service;

import com.lyf.supplychain.common.context.TenantContext;
import com.lyf.supplychain.common.exception.BusinessException;
import com.lyf.supplychain.purchase.mapper.PurchaseOrderMapper;
import com.lyf.supplychain.purchase.mapper.PurchaseRequisitionMapper;
import com.lyf.supplychain.purchase.request.PurchaseItemRequest;
import com.lyf.supplychain.purchase.request.PurchaseRequisitionRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * 采购申请去重检查测试。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
class PurchaseRequisitionDuplicateCheckerTest {

    @AfterEach
    void clearTenant() {
        TenantContext.clear();
    }

    @Test
    void shouldBlockWhenOngoingRequisitionAndOrderCanCoverRequestedQuantity() {
        TenantContext.set(101L, 1L);
        PurchaseRequisitionRequest request = buildRequest(1001L, 1L, 50);
        PurchaseRequisitionDuplicateChecker checker = new PurchaseRequisitionDuplicateChecker(
                requisitionMapper(30),
                orderMapper(20)
        );

        assertThatCode(() -> checker.check(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("已存在进行中的采购申请或采购单");
    }

    @Test
    void shouldPassWhenOngoingQuantityCannotCoverRequestedQuantity() {
        TenantContext.set(101L, 1L);
        PurchaseRequisitionRequest request = buildRequest(1001L, 1L, 50);
        PurchaseRequisitionDuplicateChecker checker = new PurchaseRequisitionDuplicateChecker(
                requisitionMapper(10),
                orderMapper(5)
        );

        assertThatCode(() -> checker.check(request)).doesNotThrowAnyException();
    }

    private PurchaseRequisitionMapper requisitionMapper(Integer quantity) {
        return (PurchaseRequisitionMapper) Proxy.newProxyInstance(
                PurchaseRequisitionMapper.class.getClassLoader(),
                new Class[]{PurchaseRequisitionMapper.class},
                (proxy, method, args) -> "sumOngoingQty".equals(method.getName()) ? quantity : null
        );
    }

    private PurchaseOrderMapper orderMapper(Integer quantity) {
        return (PurchaseOrderMapper) Proxy.newProxyInstance(
                PurchaseOrderMapper.class.getClassLoader(),
                new Class[]{PurchaseOrderMapper.class},
                (proxy, method, args) -> "sumOngoingQty".equals(method.getName()) ? quantity : null
        );
    }

    private PurchaseRequisitionRequest buildRequest(Long skuId, Long warehouseId, Integer quantity) {
        PurchaseItemRequest item = new PurchaseItemRequest();
        item.setSkuId(skuId);
        item.setSkuCode("SKU-" + skuId);
        item.setSkuName("测试商品");
        item.setQuantity(quantity);
        PurchaseRequisitionRequest request = new PurchaseRequisitionRequest();
        request.setWarehouseId(warehouseId);
        request.setItems(List.of(item));
        return request;
    }
}
