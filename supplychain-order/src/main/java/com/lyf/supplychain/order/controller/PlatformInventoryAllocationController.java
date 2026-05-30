package com.lyf.supplychain.order.controller;

import com.lyf.supplychain.common.api.PageQuery;
import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.common.security.annotation.RequiresPermission;
import com.lyf.supplychain.common.security.annotation.TenantWriteGuard;
import com.lyf.supplychain.common.security.constant.PermissionCodes;
import com.lyf.supplychain.order.entity.PlatformInventoryAllocation;
import com.lyf.supplychain.order.request.PlatformInventoryAllocationAdjustRequest;
import com.lyf.supplychain.order.request.PlatformInventoryAllocationRequest;
import com.lyf.supplychain.order.service.PlatformInventoryAllocationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 多平台库存分配接口。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@RestController
@RequestMapping({"/api/oms/platform-allocations", "/oms/platform-allocations"})
@RequiresPermission(PermissionCodes.OMS_ORDER_MANAGE)
public class PlatformInventoryAllocationController {

    private final PlatformInventoryAllocationService allocationService;

    public PlatformInventoryAllocationController(PlatformInventoryAllocationService allocationService) {
        this.allocationService = allocationService;
    }

    /**
     * 查询平台库存分配列表。
     *
     * @param query 分页参数
     * @return 平台库存分配分页数据
     */
    @GetMapping
    public R<PageResult<PlatformInventoryAllocation>> page(PageQuery query) {
        return R.ok(allocationService.page(query));
    }

    /**
     * 创建平台库存分配。
     *
     * @param request 保存请求
     * @return 分配记录ID
     */
    @PostMapping
    @TenantWriteGuard(scene = "创建平台库存分配")
    public R<Long> create(@Valid @RequestBody PlatformInventoryAllocationRequest request) {
        return R.ok(allocationService.create(request));
    }

    /**
     * 调整平台库存配额。
     *
     * @param id      分配记录ID
     * @param request 调整请求
     * @return 无数据响应
     */
    @PutMapping("/{id}/adjust")
    @TenantWriteGuard(scene = "调整平台库存配额")
    public R<Void> adjust(@PathVariable("id") Long id,
                          @Valid @RequestBody PlatformInventoryAllocationAdjustRequest request) {
        allocationService.adjust(id, request);
        return R.ok();
    }

    /**
     * 同步平台库存。
     *
     * @param id 分配记录ID
     * @return 无数据响应
     */
    @PostMapping("/{id}/sync")
    @TenantWriteGuard(scene = "同步平台库存")
    public R<Void> sync(@PathVariable("id") Long id) {
        allocationService.sync(id);
        return R.ok();
    }
}
