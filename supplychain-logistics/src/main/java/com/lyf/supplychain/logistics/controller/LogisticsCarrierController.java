package com.lyf.supplychain.logistics.controller;

import com.lyf.supplychain.common.api.PageQuery;
import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.common.security.annotation.TenantWriteGuard;
import com.lyf.supplychain.logistics.entity.LogisticsCarrier;
import com.lyf.supplychain.logistics.request.CarrierRequest;
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
 * 物流商管理控制器。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@RestController
@RequestMapping({"/api/tms/carriers", "/tms/carriers"})
@com.lyf.supplychain.common.security.annotation.RequiresPermission(com.lyf.supplychain.common.security.constant.PermissionCodes.TMS_LOGISTICS_MANAGE)
public class LogisticsCarrierController {

    private final LogisticsService logisticsService;

    public LogisticsCarrierController(LogisticsService logisticsService) {
        this.logisticsService = logisticsService;
    }

    /**
     * 分页查询物流商。
     *
     * @param query 分页参数
     * @return 物流商分页结果
     */
    @GetMapping
    public R<PageResult<LogisticsCarrier>> page(PageQuery query) {
        return R.ok(logisticsService.pageCarriers(query));
    }

    /**
     * 创建物流商。
     *
     * @param request 物流商请求
     * @return 物流商ID
     */
    @PostMapping
    @TenantWriteGuard(scene = "创建物流商")
    public R<Long> create(@Valid @RequestBody CarrierRequest request) {
        return R.ok(logisticsService.createCarrier(request));
    }

    /**
     * 修改物流商。
     *
     * @param id      物流商ID
     * @param request 物流商请求
     * @return 无数据响应
     */
    @PutMapping("/{id}")
    @TenantWriteGuard(scene = "修改物流商")
    public R<Void> update(@PathVariable("id") Long id, @Valid @RequestBody CarrierRequest request) {
        logisticsService.updateCarrier(id, request);
        return R.ok();
    }
}
