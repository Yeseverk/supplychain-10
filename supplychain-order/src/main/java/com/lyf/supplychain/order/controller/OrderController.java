package com.lyf.supplychain.order.controller;

import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.common.security.annotation.TenantWriteGuard;
import com.lyf.supplychain.order.entity.OrderLog;
import com.lyf.supplychain.order.entity.OrderMain;
import com.lyf.supplychain.order.request.OrderCancelRequest;
import com.lyf.supplychain.order.request.OrderCreateRequest;
import com.lyf.supplychain.order.request.OrderFlagRequest;
import com.lyf.supplychain.order.request.OrderMergeRequest;
import com.lyf.supplychain.order.request.OrderPageQuery;
import com.lyf.supplychain.order.request.OrderSplitRequest;
import com.lyf.supplychain.order.service.OrderMainService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 订单管理接口。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@RestController
@RequestMapping({"/api/oms/orders", "/oms/orders"})
@com.lyf.supplychain.common.security.annotation.RequiresPermission(com.lyf.supplychain.common.security.constant.PermissionCodes.OMS_ORDER_LIST)
public class OrderController {

    private final OrderMainService orderService;

    public OrderController(OrderMainService orderService) {
        this.orderService = orderService;
    }

    /**
     * 订单列表。
     *
     * @param query 分页参数
     * @return 订单分页结果
     */
    @GetMapping
    public R<PageResult<OrderMain>> page(OrderPageQuery query) {
        return R.ok(orderService.page(query));
    }

    /**
     * 订单详情。
     *
     * @param id 订单ID
     * @return 订单
     */
    @GetMapping("/{id}")
    public R<OrderMain> detail(@PathVariable("id") Long id) {
        return R.ok(orderService.detail(id));
    }

    /**
     * 手动创建订单。
     *
     * @param request 创建请求
     * @return 订单ID
     */
    @PostMapping
    @TenantWriteGuard(scene = "手动创建订单")
    public R<Long> create(@Valid @RequestBody OrderCreateRequest request) {
        return R.ok(orderService.create(request));
    }

    /**
     * 手动取消订单。
     *
     * @param id      订单ID
     * @param request 取消请求
     * @return 无数据响应
     */
    @PutMapping("/{id}/cancel")
    @TenantWriteGuard(scene = "取消订单")
    public R<Void> cancel(@PathVariable("id") Long id, @Valid @RequestBody OrderCancelRequest request) {
        orderService.cancel(id, request);
        return R.ok();
    }

    /**
     * 风控审核通过。
     *
     * @param id 订单ID
     * @return 无数据响应
     */
    @PutMapping("/{id}/approve")
    @TenantWriteGuard(scene = "订单风控审核通过")
    public R<Void> approve(@PathVariable("id") Long id) {
        orderService.approve(id);
        return R.ok();
    }

    /**
     * 风控审核拒绝。
     *
     * @param id 订单ID
     * @return 无数据响应
     */
    @PutMapping("/{id}/reject")
    @TenantWriteGuard(scene = "订单风控审核拒绝")
    public R<Void> reject(@PathVariable("id") Long id) {
        orderService.reject(id);
        return R.ok();
    }

    /**
     * 标记异常。
     *
     * @param id      订单ID
     * @param request 异常请求
     * @return 无数据响应
     */
    @PutMapping("/{id}/flag")
    @TenantWriteGuard(scene = "标记订单异常")
    public R<Void> flag(@PathVariable("id") Long id, @Valid @RequestBody OrderFlagRequest request) {
        orderService.flag(id, request);
        return R.ok();
    }

    /**
     * 同步平台状态。
     *
     * @param id 订单ID
     * @return 无数据响应
     */
    @PostMapping("/{id}/sync")
    @TenantWriteGuard(scene = "同步平台订单状态")
    public R<Void> sync(@PathVariable("id") Long id) {
        orderService.sync(id);
        return R.ok();
    }

    /**
     * 查询订单日志。
     *
     * @param id 订单ID
     * @return 日志列表
     */
    @GetMapping("/{id}/logs")
    public R<List<OrderLog>> logs(@PathVariable("id") Long id) {
        return R.ok(orderService.logs(id));
    }

    /**
     * 拆单。
     *
     * @param id      订单ID
     * @param request 拆单请求
     * @return 新订单ID
     */
    @PostMapping("/{id}/split")
    @TenantWriteGuard(scene = "订单拆单")
    public R<Long> split(@PathVariable("id") Long id, @Valid @RequestBody OrderSplitRequest request) {
        return R.ok(orderService.split(id, request));
    }
}
