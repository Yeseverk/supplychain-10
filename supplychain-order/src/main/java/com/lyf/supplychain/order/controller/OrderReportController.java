package com.lyf.supplychain.order.controller;

import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.order.service.OrderMainService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 订单报表接口。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@RestController
@RequestMapping({"/api/oms/report", "/oms/report"})
@com.lyf.supplychain.common.security.annotation.RequiresPermission(com.lyf.supplychain.common.security.constant.PermissionCodes.OMS_ORDER_LIST)
public class OrderReportController {

    private final OrderMainService orderService;

    public OrderReportController(OrderMainService orderService) {
        this.orderService = orderService;
    }

    /**
     * 订单统计看板。
     *
     * @return 概览数据
     */
    @GetMapping("/overview")
    public R<Map<String, Object>> overview() {
        return R.ok(orderService.overview());
    }

    /**
     * 今日订单实时。
     *
     * @return 今日数据
     */
    @GetMapping("/today")
    public R<Map<String, Object>> today() {
        return R.ok(orderService.today());
    }
}
