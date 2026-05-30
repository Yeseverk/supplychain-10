package com.lyf.supplychain.order.controller;

import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.common.security.annotation.TenantWriteGuard;
import com.lyf.supplychain.order.request.OrderMergeRequest;
import com.lyf.supplychain.order.service.OrderMainService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单合单接口。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@RestController
@RequestMapping({"/api/oms/orders", "/oms/orders"})
@com.lyf.supplychain.common.security.annotation.RequiresPermission(com.lyf.supplychain.common.security.constant.PermissionCodes.OMS_ORDER_MANAGE)
public class OrderMergeController {

    private final OrderMainService orderService;

    public OrderMergeController(OrderMainService orderService) {
        this.orderService = orderService;
    }

    /**
     * 合并订单。
     *
     * @param request 合单请求
     * @return 主订单ID
     */
    @PostMapping("/merge")
    @TenantWriteGuard(scene = "订单合单")
    public R<Long> merge(@Valid @RequestBody OrderMergeRequest request) {
        return R.ok(orderService.merge(request));
    }
}
