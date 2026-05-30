package com.lyf.supplychain.logistics.controller;

import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.logistics.service.LogisticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 物流报表控制器。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@RestController
@com.lyf.supplychain.common.security.annotation.RequiresPermission(com.lyf.supplychain.common.security.constant.PermissionCodes.TMS_LOGISTICS_MANAGE)
public class LogisticsReportController {

    private final LogisticsService logisticsService;

    public LogisticsReportController(LogisticsService logisticsService) {
        this.logisticsService = logisticsService;
    }

    /**
     * 查询渠道效率报表。
     *
     * @return 渠道效率数据
     */
    @GetMapping({"/api/tms/report/channel-efficiency", "/tms/report/channel-efficiency"})
    public R<Map<String, Object>> channelEfficiency() {
        return R.ok(logisticsService.channelEfficiency());
    }

    /**
     * 查询异常报表。
     *
     * @return 异常统计数据
     */
    @GetMapping({"/api/tms/report/exceptions", "/tms/report/exceptions"})
    public R<Map<String, Object>> exceptions() {
        return R.ok(logisticsService.exceptionReport());
    }

    /**
     * 查询费用汇总报表。
     *
     * @return 费用汇总数据
     */
    @GetMapping({"/api/tms/report/fee-summary", "/tms/report/fee-summary"})
    public R<Map<String, Object>> feeSummary() {
        return R.ok(logisticsService.feeSummary());
    }
}
