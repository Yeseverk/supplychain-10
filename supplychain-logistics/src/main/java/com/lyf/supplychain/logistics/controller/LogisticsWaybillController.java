package com.lyf.supplychain.logistics.controller;

import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.common.security.annotation.TenantWriteGuard;
import com.lyf.supplychain.logistics.entity.LogisticsTrack;
import com.lyf.supplychain.logistics.entity.LogisticsWaybill;
import com.lyf.supplychain.logistics.request.LogisticsPageQuery;
import com.lyf.supplychain.logistics.request.WaybillBatchRequest;
import com.lyf.supplychain.logistics.request.WaybillRequest;
import com.lyf.supplychain.logistics.request.WaybillStatusRequest;
import com.lyf.supplychain.logistics.service.LogisticsService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 运单管理控制器。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@RestController
@RequestMapping({"/api/tms/waybills", "/tms/waybills"})
@com.lyf.supplychain.common.security.annotation.RequiresPermission(com.lyf.supplychain.common.security.constant.PermissionCodes.TMS_WAYBILL_ADD)
public class LogisticsWaybillController {

    private final LogisticsService logisticsService;

    public LogisticsWaybillController(LogisticsService logisticsService) {
        this.logisticsService = logisticsService;
    }

    /**
     * 分页查询运单。
     *
     * @param query 分页参数
     * @return 运单分页结果
     */
    @GetMapping
    public R<PageResult<LogisticsWaybill>> page(LogisticsPageQuery query) {
        return R.ok(logisticsService.pageWaybills(query));
    }

    /**
     * 查询运单详情。
     *
     * @param id 运单ID
     * @return 运单详情
     */
    @GetMapping("/{id}")
    public R<LogisticsWaybill> detail(@PathVariable("id") Long id) {
        return R.ok(logisticsService.detailWaybill(id));
    }

    /**
     * 创建运单。
     *
     * @param request 运单请求
     * @return 运单ID
     */
    @PostMapping
    @TenantWriteGuard(scene = "创建运单")
    public R<Long> create(@Valid @RequestBody WaybillRequest request) {
        return R.ok(logisticsService.createWaybill(request));
    }

    /**
     * 批量创建运单。
     *
     * @param request 批量运单请求
     * @return 运单ID列表
     */
    @PostMapping("/batch")
    @TenantWriteGuard(scene = "批量创建运单")
    public R<List<Long>> batch(@Valid @RequestBody WaybillBatchRequest request) {
        return R.ok(logisticsService.batchCreateWaybills(request));
    }

    /**
     * 获取运单面单。
     *
     * @param id 运单ID
     * @return 面单信息
     */
    @GetMapping("/{id}/label")
    public R<Map<String, Object>> label(@PathVariable("id") Long id) {
        return R.ok(logisticsService.label(id));
    }

    /**
     * 批量获取运单面单。
     *
     * @param ids 运单ID列表
     * @return 面单列表
     */
    @PostMapping("/labels/batch")
    public R<List<Map<String, Object>>> batchLabels(@RequestBody List<Long> ids) {
        return R.ok(logisticsService.batchLabels(ids));
    }

    /**
     * 更新运单状态。
     *
     * @param id      运单ID
     * @param request 状态请求
     * @return 无数据响应
     */
    @PutMapping("/{id}/status")
    @TenantWriteGuard(scene = "更新运单状态")
    public R<Void> updateStatus(@PathVariable("id") Long id, @Valid @RequestBody WaybillStatusRequest request) {
        logisticsService.updateWaybillStatus(id, request);
        return R.ok();
    }

    /**
     * 取消运单。
     *
     * @param id 运单ID
     * @return 无数据响应
     */
    @PutMapping("/{id}/cancel")
    @TenantWriteGuard(scene = "取消运单")
    public R<Void> cancel(@PathVariable("id") Long id) {
        logisticsService.cancelWaybill(id);
        return R.ok();
    }

    /**
     * 查询运单轨迹。
     *
     * @param id 运单ID
     * @return 轨迹列表
     */
    @GetMapping("/{id}/tracks")
    public R<List<LogisticsTrack>> tracks(@PathVariable("id") Long id) {
        return R.ok(logisticsService.tracks(id));
    }

    /**
     * 查询异常运单。
     *
     * @return 异常运单列表
     */
    @GetMapping("/exceptions")
    public R<List<LogisticsWaybill>> exceptions() {
        return R.ok(logisticsService.exceptions());
    }

    /**
     * 手动刷新运单轨迹。
     *
     * @param id 运单ID
     * @return 无数据响应
     */
    @PostMapping("/{id}/tracks/refresh")
    @TenantWriteGuard(scene = "手动刷新运单轨迹")
    public R<Void> refreshTracks(@PathVariable("id") Long id) {
        logisticsService.refreshTracks(id);
        return R.ok();
    }
}
