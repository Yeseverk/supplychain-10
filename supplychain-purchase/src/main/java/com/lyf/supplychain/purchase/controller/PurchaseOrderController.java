package com.lyf.supplychain.purchase.controller;

import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.common.security.annotation.TenantWriteGuard;
import com.lyf.supplychain.purchase.entity.PurchaseOrder;
import com.lyf.supplychain.purchase.request.PurchaseOrderPageQuery;
import com.lyf.supplychain.purchase.request.PurchaseOrderRequest;
import com.lyf.supplychain.purchase.response.PurchaseOrderDetailResponse;
import com.lyf.supplychain.purchase.service.PurchaseOrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 采购订单接口控制器。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@RestController
@RequestMapping({"/api/pms/orders", "/pms/orders"})
@com.lyf.supplychain.common.security.annotation.RequiresPermission(com.lyf.supplychain.common.security.constant.PermissionCodes.PMS_ORDER_LIST)
public class PurchaseOrderController {

    private final PurchaseOrderService orderService;

    public PurchaseOrderController(PurchaseOrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 分页查询采购订单。
     *
     * @param query 查询条件
     * @return 采购订单分页结果
     */
    @GetMapping({"", "/page"})
    public R<PageResult<PurchaseOrder>> pageOrders(PurchaseOrderPageQuery query) {
        return R.ok(orderService.pageOrders(query));
    }

    /**
     * 查询采购订单详情。
     *
     * @param id 采购订单ID
     * @return 采购订单详情
     */
    @GetMapping("/{id:\\d+}")
    public R<PurchaseOrder> detail(@PathVariable("id") Long id) {
        return R.ok(orderService.getById(id));
    }

    @GetMapping("/{id:\\d+}/detail")
    public R<PurchaseOrderDetailResponse> detailView(@PathVariable("id") Long id) {
        return R.ok(orderService.detail(id));
    }

    /**
     * 创建采购订单。
     *
     * @param request 创建请求
     * @return 采购订单ID
     */
    @PostMapping
    @TenantWriteGuard(scene = "创建采购订单")
    public R<Long> create(@Valid @RequestBody PurchaseOrderRequest request) {
        return R.ok(orderService.create(request));
    }

    /**
     * 修改采购订单。
     *
     * @param id      采购订单ID
     * @param request 修改请求
     * @return 无数据响应
     */
    @PutMapping("/{id:\\d+}")
    @TenantWriteGuard(scene = "修改采购订单")
    public R<Void> update(@PathVariable("id") Long id, @Valid @RequestBody PurchaseOrderRequest request) {
        orderService.updateDraft(id, request);
        return R.ok();
    }

    /**
     * 发送采购订单给供应商。
     *
     * @param id 采购订单ID
     * @return 无数据响应
     */
    @PutMapping("/{id:\\d+}/send")
    @TenantWriteGuard(scene = "发送采购订单")
    public R<Void> send(@PathVariable("id") Long id) {
        orderService.send(id);
        return R.ok();
    }

    /**
     * 确认采购订单。
     *
     * @param id 采购订单ID
     * @return 无数据响应
     */
    @PutMapping("/{id:\\d+}/confirm")
    @TenantWriteGuard(scene = "确认采购订单")
    public R<Void> confirm(@PathVariable("id") Long id) {
        orderService.confirm(id);
        return R.ok();
    }

    /**
     * 标记采购订单发货中。
     *
     * @param id 采购订单ID
     * @return 无数据响应
     */
    @PutMapping("/{id:\\d+}/shipping")
    @TenantWriteGuard(scene = "标记采购订单发货中")
    public R<Void> markShipping(@PathVariable("id") Long id) {
        orderService.markShipping(id);
        return R.ok();
    }

    /**
     * 标记采购订单已对账。
     *
     * @param id 采购订单ID
     * @return 无数据响应
     */
    @PutMapping("/{id:\\d+}/reconcile")
    @TenantWriteGuard(scene = "采购订单对账")
    public R<Void> reconcile(@PathVariable("id") Long id) {
        orderService.reconcile(id);
        return R.ok();
    }

    /**
     * 取消采购订单。
     *
     * @param id 采购订单ID
     * @return 无数据响应
     */
    @PutMapping("/{id:\\d+}/cancel")
    @TenantWriteGuard(scene = "取消采购订单")
    public R<Void> cancel(@PathVariable("id") Long id) {
        orderService.cancel(id);
        return R.ok();
    }

    /**
     * 查询历史采购价格。
     *
     * @param supplierId 供应商ID
     * @param skuId      SKU ID
     * @return 采购订单列表
     */
    @GetMapping("/price-history")
    public R<List<PurchaseOrder>> priceHistory(@RequestParam("supplierId") Long supplierId,
                                               @RequestParam("skuId") Long skuId) {
        return R.ok(orderService.priceHistory(supplierId, skuId));
    }
}
