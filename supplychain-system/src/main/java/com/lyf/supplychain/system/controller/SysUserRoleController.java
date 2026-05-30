package com.lyf.supplychain.system.controller;

import com.lyf.supplychain.system.entity.SysUserRole;
import com.lyf.supplychain.system.service.SysUserRoleService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户角色关联 CRUD 控制器。
 *
 * @author liyunfei
 * @date 2026-05-15
 */
@RestController
@RequestMapping("/system/user-roles")
@com.lyf.supplychain.common.security.annotation.RequiresPermission(com.lyf.supplychain.common.security.constant.PermissionCodes.SYS_USER_EDIT)
public class SysUserRoleController extends BaseCrudController<SysUserRole> {

    public SysUserRoleController(SysUserRoleService service) {
        super(service);
    }
}
