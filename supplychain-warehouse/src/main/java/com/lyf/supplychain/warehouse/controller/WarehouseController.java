package com.lyf.supplychain.warehouse.controller;

import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.common.security.annotation.TenantWriteGuard;
import com.lyf.supplychain.warehouse.entity.Warehouse;
import com.lyf.supplychain.warehouse.entity.WarehouseLocation;
import com.lyf.supplychain.warehouse.request.LocationBatchRequest;
import com.lyf.supplychain.warehouse.request.LocationRequest;
import com.lyf.supplychain.warehouse.request.WarehouseRequest;
import com.lyf.supplychain.warehouse.request.WmsPageQuery;
import com.lyf.supplychain.warehouse.service.WarehouseLocationService;
import com.lyf.supplychain.warehouse.service.WarehouseService;
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
 * 仓库和库位管理接口控制器。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@RestController
@RequestMapping({"/api/wms/warehouses", "/wms/warehouses"})
@com.lyf.supplychain.common.security.annotation.RequiresPermission(com.lyf.supplychain.common.security.constant.PermissionCodes.WMS_WAREHOUSE_MANAGE)
public class WarehouseController {

    private final WarehouseService warehouseService;
    private final WarehouseLocationService locationService;

    public WarehouseController(WarehouseService warehouseService, WarehouseLocationService locationService) {
        this.warehouseService = warehouseService;
        this.locationService = locationService;
    }

    /**
     * 查询仓库列表。
     *
     * @param query 分页参数
     * @return 仓库分页结果
     */
    @GetMapping
    public R<PageResult<Warehouse>> pageWarehouses(WmsPageQuery query) {
        return R.ok(warehouseService.pageWarehouses(query));
    }

    /**
     * 查询仓库详情。
     *
     * @param id 仓库ID
     * @return 仓库详情
     */
    @GetMapping("/{id}")
    public R<Warehouse> detail(@PathVariable("id") Long id) {
        return R.ok(warehouseService.getById(id));
    }

    /**
     * 新增仓库。
     *
     * @param request 仓库保存请求
     * @return 仓库ID
     */
    @PostMapping
    @TenantWriteGuard(scene = "新增仓库")
    public R<Long> create(@Valid @RequestBody WarehouseRequest request) {
        return R.ok(warehouseService.create(request));
    }

    /**
     * 编辑仓库。
     *
     * @param id      仓库ID
     * @param request 仓库保存请求
     * @return 无数据响应
     */
    @PutMapping("/{id}")
    @TenantWriteGuard(scene = "编辑仓库")
    public R<Void> update(@PathVariable("id") Long id, @Valid @RequestBody WarehouseRequest request) {
        warehouseService.update(id, request);
        return R.ok();
    }

    /**
     * 查询库位列表。
     *
     * @param wid 仓库ID
     * @return 库位列表
     */
    @GetMapping("/{wid}/locations")
    public R<List<WarehouseLocation>> locations(@PathVariable("wid") Long wid) {
        return R.ok(locationService.listByWarehouse(wid));
    }

    /**
     * 新增库位。
     *
     * @param wid     仓库ID
     * @param request 库位请求
     * @return 库位ID
     */
    @PostMapping("/{wid}/locations")
    @TenantWriteGuard(scene = "新增库位")
    public R<Long> createLocation(@PathVariable("wid") Long wid, @Valid @RequestBody LocationRequest request) {
        return R.ok(locationService.create(wid, request));
    }

    /**
     * 批量新增库位。
     *
     * @param wid     仓库ID
     * @param request 批量请求
     * @return 创建数量
     */
    @PostMapping("/{wid}/locations/batch")
    @TenantWriteGuard(scene = "批量新增库位")
    public R<Integer> batchLocations(@PathVariable("wid") Long wid, @Valid @RequestBody LocationBatchRequest request) {
        return R.ok(locationService.batchCreate(wid, request));
    }

    /**
     * 查询空闲库位。
     *
     * @param wid 仓库ID
     * @return 空闲库位列表
     */
    @GetMapping("/{wid}/locations/available")
    public R<List<WarehouseLocation>> availableLocations(@PathVariable("wid") Long wid) {
        return R.ok(locationService.available(wid));
    }
}
