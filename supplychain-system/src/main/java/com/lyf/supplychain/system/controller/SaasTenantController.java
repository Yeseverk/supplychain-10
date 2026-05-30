package com.lyf.supplychain.system.controller;

import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.common.security.annotation.OperationLog;
import com.lyf.supplychain.common.security.annotation.RequiresPermission;
import com.lyf.supplychain.common.security.constant.PermissionCodes;
import com.lyf.supplychain.system.model.saas.TenantPlanChangeRequest;
import com.lyf.supplychain.system.model.saas.TenantRegisterRequest;
import com.lyf.supplychain.system.service.SysTenantService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * SaaS 租户运营控制器。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@RestController
@RequestMapping("/api/saas/tenants")
public class SaasTenantController {

    private final SysTenantService tenantService;

    public SaasTenantController(SysTenantService tenantService) {
        this.tenantService = tenantService;
    }

    /**
     * 自助注册租户并初始化管理员。
     *
     * @param request 注册请求
     * @return 租户ID
     */
    @PostMapping("/register")
    @OperationLog(module = "SaaS租户", action = "注册租户", type = OperationLog.Type.INSERT, sensitiveFields = {"password", "adminPassword", "token", "secret"})
    public R<Long> register(@Valid @RequestBody TenantRegisterRequest request) {
        return R.ok(tenantService.registerTenant(request));
    }

    /**
     * 启用租户。
     *
     * @param id 租户ID
     * @return 无数据响应
     */
    @PutMapping("/{id}/enable")
    @RequiresPermission(PermissionCodes.SAAS_TENANT_MANAGE)
    @OperationLog(module = "SaaS租户", action = "启用租户", type = OperationLog.Type.UPDATE)
    public R<Void> enable(@PathVariable("id") Long id) {
        tenantService.enableTenant(id);
        return R.ok();
    }

    /**
     * 禁用租户。
     *
     * @param id 租户ID
     * @return 无数据响应
     */
    @PutMapping("/{id}/disable")
    @RequiresPermission(PermissionCodes.SAAS_TENANT_MANAGE)
    @OperationLog(module = "SaaS租户", action = "禁用租户", type = OperationLog.Type.UPDATE)
    public R<Void> disable(@PathVariable("id") Long id) {
        tenantService.disableTenant(id);
        return R.ok();
    }

    /**
     * 变更租户套餐。
     *
     * @param id      租户ID
     * @param request 套餐变更请求
     * @return 无数据响应
     */
    @PutMapping("/{id}/plan")
    @RequiresPermission(PermissionCodes.SAAS_PLAN_MANAGE)
    @OperationLog(module = "SaaS套餐", action = "变更租户套餐", type = OperationLog.Type.UPDATE)
    public R<Void> changePlan(@PathVariable("id") Long id, @Valid @RequestBody TenantPlanChangeRequest request) {
        tenantService.changePlan(id, request);
        return R.ok();
    }
}
