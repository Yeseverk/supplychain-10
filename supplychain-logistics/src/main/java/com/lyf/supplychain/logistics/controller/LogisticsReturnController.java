package com.lyf.supplychain.logistics.controller;

import com.lyf.supplychain.common.api.PageQuery;
import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.common.security.annotation.TenantWriteGuard;
import com.lyf.supplychain.logistics.entity.LogisticsReturn;
import com.lyf.supplychain.logistics.request.ReturnRequest;
import com.lyf.supplychain.logistics.service.LogisticsService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 物流退货控制器。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@RestController
@RequestMapping({"/api/tms/returns", "/tms/returns"})
@com.lyf.supplychain.common.security.annotation.RequiresPermission(com.lyf.supplychain.common.security.constant.PermissionCodes.TMS_LOGISTICS_MANAGE)
public class LogisticsReturnController {

    private final LogisticsService logisticsService;

    public LogisticsReturnController(LogisticsService logisticsService) {
        this.logisticsService = logisticsService;
    }

    /**
     * 分页查询退货运单。
     *
     * @param query 分页参数
     * @return 退货分页结果
     */
    @GetMapping
    public R<PageResult<LogisticsReturn>> page(PageQuery query) {
        return R.ok(logisticsService.pageReturns(query));
    }

    /**
     * 创建退货运单。
     *
     * @param request 退货请求
     * @return 退货单ID
     */
    @PostMapping
    @TenantWriteGuard(scene = "创建退货运单")
    public R<Long> create(@Valid @RequestBody ReturnRequest request) {
        return R.ok(logisticsService.createReturn(request));
    }

    /**
     * 确认退货到仓。
     *
     * @param id 退货单ID
     * @return 无数据响应
     */
    @PutMapping("/{id}/arrive")
    @TenantWriteGuard(scene = "确认退货到仓")
    public R<Void> arrive(@PathVariable("id") Long id) {
        logisticsService.arriveReturn(id);
        return R.ok();
    }
}
