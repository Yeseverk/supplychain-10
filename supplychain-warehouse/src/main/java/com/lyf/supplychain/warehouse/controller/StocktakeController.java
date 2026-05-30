package com.lyf.supplychain.warehouse.controller;

import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.common.security.annotation.TenantWriteGuard;
import com.lyf.supplychain.warehouse.entity.StocktakeItem;
import com.lyf.supplychain.warehouse.entity.StocktakeTask;
import com.lyf.supplychain.warehouse.request.StocktakeAuditRequest;
import com.lyf.supplychain.warehouse.request.StocktakeCountRequest;
import com.lyf.supplychain.warehouse.request.StocktakeTaskRequest;
import com.lyf.supplychain.warehouse.request.WmsPageQuery;
import com.lyf.supplychain.warehouse.service.StocktakeService;
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
 * 盘点管理接口控制器。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@RestController
@RequestMapping({"/api/wms/stocktake", "/wms/stocktake"})
@com.lyf.supplychain.common.security.annotation.RequiresPermission(com.lyf.supplychain.common.security.constant.PermissionCodes.WMS_INVENTORY_ADJUST)
public class StocktakeController {

    private final StocktakeService stocktakeService;

    public StocktakeController(StocktakeService stocktakeService) {
        this.stocktakeService = stocktakeService;
    }

    /**
     * 盘点任务列表。
     *
     * @param query 分页参数
     * @return 盘点任务分页结果
     */
    @GetMapping
    public R<PageResult<StocktakeTask>> pageStocktake(WmsPageQuery query) {
        return R.ok(stocktakeService.pageStocktake(query));
    }

    /**
     * 创建盘点任务。
     *
     * @param request 创建请求
     * @return 盘点任务ID
     */
    @PostMapping
    @TenantWriteGuard(scene = "创建盘点任务")
    public R<Long> create(@Valid @RequestBody StocktakeTaskRequest request) {
        return R.ok(stocktakeService.create(request));
    }

    /**
     * 查询盘点任务明细。
     *
     * @param id 盘点任务ID
     * @return 盘点明细列表
     */
    @GetMapping("/{id}/items")
    public R<List<StocktakeItem>> items(@PathVariable("id") Long id) {
        return R.ok(stocktakeService.items(id));
    }

    /**
     * 提交实盘数据。
     *
     * @param id      盘点任务ID
     * @param request 实盘请求
     * @return 无数据响应
     */
    @PutMapping("/{id}/count")
    @TenantWriteGuard(scene = "提交实盘数据")
    public R<Void> count(@PathVariable("id") Long id, @Valid @RequestBody StocktakeCountRequest request) {
        stocktakeService.count(id, request);
        return R.ok();
    }

    /**
     * 审核调整差异。
     *
     * @param id      盘点任务ID
     * @param request 审核请求
     * @return 无数据响应
     */
    @PutMapping("/{id}/audit")
    @TenantWriteGuard(scene = "审核盘点差异")
    public R<Void> audit(@PathVariable("id") Long id, @Valid @RequestBody StocktakeAuditRequest request) {
        stocktakeService.audit(id, request);
        return R.ok();
    }
}
