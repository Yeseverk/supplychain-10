package com.lyf.supplychain.purchase.controller;

import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.common.security.annotation.OperationLog;
import com.lyf.supplychain.common.security.annotation.TenantWriteGuard;
import com.lyf.supplychain.purchase.entity.PurchaseRequisition;
import com.lyf.supplychain.purchase.request.PurchaseAuditRequest;
import com.lyf.supplychain.purchase.request.PurchaseRequisitionPageQuery;
import com.lyf.supplychain.purchase.request.PurchaseRequisitionRequest;
import com.lyf.supplychain.purchase.service.PurchaseRequisitionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 采购申请接口控制器。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@RestController
@RequestMapping({"/api/pms/requisitions", "/pms/requisitions"})
@com.lyf.supplychain.common.security.annotation.RequiresPermission(com.lyf.supplychain.common.security.constant.PermissionCodes.PMS_ORDER_MANAGE)
public class PurchaseRequisitionController {

    private final PurchaseRequisitionService requisitionService;

    public PurchaseRequisitionController(PurchaseRequisitionService requisitionService) {
        this.requisitionService = requisitionService;
    }

    /**
     * 分页查询采购申请。
     *
     * @param query 查询条件
     * @return 采购申请分页结果
     */
    @GetMapping({"", "/page"})
    public R<PageResult<PurchaseRequisition>> pageRequisitions(PurchaseRequisitionPageQuery query) {
        return R.ok(requisitionService.pageRequisitions(query));
    }

    /**
     * 查询采购申请详情。
     *
     * @param id 采购申请ID
     * @return 采购申请详情
     */
    @GetMapping("/{id:\\d+}")
    public R<PurchaseRequisition> detail(@PathVariable("id") Long id) {
        return R.ok(requisitionService.getById(id));
    }

    /**
     * 创建采购申请。
     *
     * @param request 创建请求
     * @return 采购申请ID
     */
    @PostMapping
    @TenantWriteGuard(scene = "创建采购申请")
    @OperationLog(module = "采购申请", action = "创建采购申请", type = OperationLog.Type.INSERT)
    public R<Long> create(@Valid @RequestBody PurchaseRequisitionRequest request) {
        return R.ok(requisitionService.create(request));
    }

    /**
     * 修改采购申请。
     *
     * @param id      采购申请ID
     * @param request 修改请求
     * @return 无数据响应
     */
    @PutMapping("/{id:\\d+}")
    @TenantWriteGuard(scene = "修改采购申请")
    public R<Void> update(@PathVariable("id") Long id, @Valid @RequestBody PurchaseRequisitionRequest request) {
        requisitionService.updateDraft(id, request);
        return R.ok();
    }

    /**
     * 删除采购申请。
     *
     * @param id 采购申请ID
     * @return 无数据响应
     */
    @DeleteMapping("/{id:\\d+}")
    @TenantWriteGuard(scene = "删除采购申请")
    public R<Void> delete(@PathVariable("id") Long id) {
        requisitionService.deleteDraft(id);
        return R.ok();
    }

    /**
     * 提交采购申请。
     *
     * @param id 采购申请ID
     * @return 无数据响应
     */
    @PutMapping("/{id:\\d+}/submit")
    @TenantWriteGuard(scene = "提交采购申请")
    @OperationLog(module = "采购申请", action = "提交采购申请", type = OperationLog.Type.UPDATE)
    public R<Void> submit(@PathVariable("id") Long id) {
        requisitionService.submit(id);
        return R.ok();
    }

    /**
     * 审批通过采购申请。
     *
     * @param id      采购申请ID
     * @param request 审批请求
     * @return 无数据响应
     */
    @PutMapping("/{id:\\d+}/approve")
    @TenantWriteGuard(scene = "审批通过采购申请")
    @OperationLog(module = "采购申请", action = "审批通过采购申请", type = OperationLog.Type.UPDATE)
    public R<Void> approve(@PathVariable("id") Long id, @RequestBody PurchaseAuditRequest request) {
        requisitionService.approve(id, request);
        return R.ok();
    }

    /**
     * 审批拒绝采购申请。
     *
     * @param id      采购申请ID
     * @param request 审批请求
     * @return 无数据响应
     */
    @PutMapping("/{id:\\d+}/reject")
    @TenantWriteGuard(scene = "审批拒绝采购申请")
    @OperationLog(module = "采购申请", action = "审批拒绝采购申请", type = OperationLog.Type.UPDATE)
    public R<Void> reject(@PathVariable("id") Long id, @RequestBody PurchaseAuditRequest request) {
        requisitionService.reject(id, request);
        return R.ok();
    }
}
