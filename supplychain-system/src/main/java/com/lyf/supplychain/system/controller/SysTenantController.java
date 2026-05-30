package com.lyf.supplychain.system.controller;

import com.lyf.supplychain.system.entity.SysTenant;
import com.lyf.supplychain.system.service.SysTenantService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 租户信息 CRUD 控制器。
 *
 * @author liyunfei
 * @date 2026-05-15
 */
@RestController
@RequestMapping("/system/tenants")
@com.lyf.supplychain.common.security.annotation.RequiresPermission(com.lyf.supplychain.common.security.constant.PermissionCodes.SYS_TENANT_MANAGE)
public class SysTenantController extends BaseCrudController<SysTenant> {

    public SysTenantController(SysTenantService service) {
        super(service);
    }
}
