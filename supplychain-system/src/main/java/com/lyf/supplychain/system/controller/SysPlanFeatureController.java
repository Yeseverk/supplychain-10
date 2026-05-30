package com.lyf.supplychain.system.controller;

import com.lyf.supplychain.common.security.annotation.RequiresPermission;
import com.lyf.supplychain.common.security.constant.PermissionCodes;
import com.lyf.supplychain.system.entity.SysPlanFeature;
import com.lyf.supplychain.system.service.SysPlanFeatureService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 套餐功能开关控制器。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@RestController
@RequestMapping("/api/saas/plan-features")
@RequiresPermission(PermissionCodes.SAAS_PLAN_MANAGE)
public class SysPlanFeatureController extends BaseCrudController<SysPlanFeature> {

    public SysPlanFeatureController(SysPlanFeatureService service) {
        super(service);
    }
}
