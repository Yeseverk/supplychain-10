package com.lyf.supplychain.system.controller;

import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.common.feign.system.SystemPlanLimitCheckRequest;
import com.lyf.supplychain.common.feign.system.SystemPlanLimitCheckResponse;
import com.lyf.supplychain.common.feign.system.SystemTenantWriteCheckRequest;
import com.lyf.supplychain.common.feign.system.SystemTenantWriteCheckResponse;
import com.lyf.supplychain.system.service.SysPlanFeatureService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统套餐限制内部接口控制器。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@RestController
@RequestMapping("/internal/system")
public class SystemInternalPlanLimitController {

    private final SysPlanFeatureService planFeatureService;

    public SystemInternalPlanLimitController(SysPlanFeatureService planFeatureService) {
        this.planFeatureService = planFeatureService;
    }

    /**
     * 检查租户套餐限制。
     *
     * @param request 套餐限制检查请求
     * @return 套餐限制检查结果
     */
    @PostMapping("/plan-limits/check")
    public R<SystemPlanLimitCheckResponse> check(@RequestBody SystemPlanLimitCheckRequest request) {
        return R.ok(planFeatureService.checkLimit(request));
    }

    /**
     * 检查租户当前是否允许写操作。
     *
     * @param request 写操作检查请求
     * @return 写操作检查结果
     */
    @PostMapping("/tenants/write-check")
    public R<SystemTenantWriteCheckResponse> checkTenantWrite(@RequestBody SystemTenantWriteCheckRequest request) {
        return R.ok(planFeatureService.checkTenantWrite(request));
    }
}
