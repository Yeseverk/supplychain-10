package com.lyf.supplychain.warehouse.controller;

import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.common.security.annotation.TenantWriteGuard;
import com.lyf.supplychain.warehouse.entity.Inventory;
import com.lyf.supplychain.warehouse.entity.InventoryLog;
import com.lyf.supplychain.warehouse.request.InventoryAdjustRequest;
import com.lyf.supplychain.warehouse.request.WmsPageQuery;
import com.lyf.supplychain.warehouse.service.WmsInventoryService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 库存查询和调整接口控制器。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@RestController
@RequestMapping({"/api/wms/inventory", "/wms/inventory"})
@com.lyf.supplychain.common.security.annotation.RequiresPermission(com.lyf.supplychain.common.security.constant.PermissionCodes.WMS_INVENTORY_LIST)
public class WmsInventoryController {

    private final WmsInventoryService inventoryService;

    public WmsInventoryController(WmsInventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    /**
     * 查询 SKU 库存列表。
     *
     * @param query 分页参数
     * @return 库存分页结果
     */
    @GetMapping
    public R<PageResult<Inventory>> pageInventory(WmsPageQuery query) {
        return R.ok(inventoryService.pageInventory(query));
    }

    /**
     * 查询 SKU 多仓库存详情。
     *
     * @param skuId SKU ID
     * @return 库存列表
     */
    @GetMapping("/sku/{skuId}")
    public R<List<Inventory>> skuDetail(@PathVariable("skuId") Long skuId) {
        return R.ok(inventoryService.skuDetail(skuId));
    }

    /**
     * 查询库存预警列表。
     *
     * @return 预警库存列表
     */
    @GetMapping("/warnings")
    public R<List<Inventory>> warnings() {
        return R.ok(inventoryService.warnings());
    }

    /**
     * 查询库存流水。
     *
     * @param query 分页参数
     * @return 流水分页结果
     */
    @GetMapping("/logs")
    public R<PageResult<InventoryLog>> logs(WmsPageQuery query) {
        return R.ok(inventoryService.pageLogs(query));
    }

    /**
     * 查询历史库存快照。
     *
     * @param warehouseId 仓库ID
     * @param skuId       SKU ID
     * @param targetTime  目标时间
     * @return 快照信息
     */
    @GetMapping("/snapshot")
    public R<Map<String, Object>> snapshot(@RequestParam("warehouseId") Long warehouseId,
                                           @RequestParam("skuId") Long skuId,
                                           @RequestParam(value = "targetTime", required = false)
                                           @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime targetTime) {
        return R.ok(inventoryService.snapshot(warehouseId, skuId, targetTime));
    }

    /**
     * 人工调整库存。
     *
     * @param request 调整请求
     * @return 无数据响应
     */
    @PostMapping("/adjust")
    @TenantWriteGuard(scene = "人工调整库存")
    public R<Void> adjust(@Valid @RequestBody InventoryAdjustRequest request) {
        inventoryService.adjust(request);
        return R.ok();
    }
}
