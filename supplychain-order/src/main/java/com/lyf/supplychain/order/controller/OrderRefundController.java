package com.lyf.supplychain.order.controller;

import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.common.security.annotation.TenantWriteGuard;
import com.lyf.supplychain.order.entity.OrderRefund;
import com.lyf.supplychain.order.request.RefundAuditRequest;
import com.lyf.supplychain.order.request.RefundCreateRequest;
import com.lyf.supplychain.order.request.RefundPageQuery;
import com.lyf.supplychain.order.service.OrderRefundService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 退款单接口。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@RestController
@RequestMapping({"/api/oms/refunds", "/oms/refunds"})
@com.lyf.supplychain.common.security.annotation.RequiresPermission(com.lyf.supplychain.common.security.constant.PermissionCodes.OMS_ORDER_MANAGE)
public class OrderRefundController {

    private final OrderRefundService refundService;

    public OrderRefundController(OrderRefundService refundService) {
        this.refundService = refundService;
    }

    /**
     * 退款单列表。
     *
     * @param query 分页参数
     * @return 退款分页结果
     */
    @GetMapping
    public R<PageResult<OrderRefund>> page(RefundPageQuery query) {
        return R.ok(refundService.page(query));
    }

    /**
     * 创建退款单。
     *
     * @param request 退款请求
     * @return 退款单ID
     */
    @PostMapping
    @TenantWriteGuard(scene = "创建退款单")
    public R<Long> create(@Valid @RequestBody RefundCreateRequest request) {
        return R.ok(refundService.create(request));
    }

    /**
     * 审核退款。
     *
     * @param id      退款单ID
     * @param request 审核请求
     * @return 无数据响应
     */
    @PutMapping("/{id}/audit")
    @TenantWriteGuard(scene = "审核退款单")
    public R<Void> audit(@PathVariable("id") Long id, @Valid @RequestBody RefundAuditRequest request) {
        refundService.audit(id, request);
        return R.ok();
    }

    /**
     * 确认收到退货。
     *
     * @param id 退款单ID
     * @return 无数据响应
     */
    @PutMapping("/{id}/received")
    @TenantWriteGuard(scene = "确认收到退货")
    public R<Void> received(@PathVariable("id") Long id) {
        refundService.received(id);
        return R.ok();
    }

    /**
     * 完成退款。
     *
     * @param id 退款单ID
     * @return 无数据响应
     */
    @PutMapping("/{id}/complete")
    @TenantWriteGuard(scene = "完成退款")
    public R<Void> complete(@PathVariable("id") Long id) {
        refundService.complete(id);
        return R.ok();
    }
}
