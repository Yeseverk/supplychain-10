package com.lyf.supplychain.order.controller;

import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.order.service.OrderMainService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 订单同步日志接口。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@RestController
@RequestMapping({"/api/oms/sync", "/oms/sync"})
@com.lyf.supplychain.common.security.annotation.RequiresPermission(com.lyf.supplychain.common.security.constant.PermissionCodes.OMS_ORDER_MANAGE)
public class OrderSyncController {

    private final OrderMainService orderService;

    public OrderSyncController(OrderMainService orderService) {
        this.orderService = orderService;
    }

    /**
     * 平台同步日志。
     *
     * @return 同步日志
     */
    @GetMapping("/logs")
    public R<List<Map<String, Object>>> logs() {
        return R.ok(orderService.syncLogs());
    }
}
