package com.lyf.supplychain.system.controller;

import com.lyf.supplychain.system.entity.SysRoleMenu;
import com.lyf.supplychain.system.service.SysRoleMenuService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 角色菜单关联 CRUD 控制器。
 *
 * @author liyunfei
 * @date 2026-05-15
 */
@RestController
@RequestMapping({"/system/role-menus", "/api/system/role-menus"})
@com.lyf.supplychain.common.security.annotation.RequiresPermission(com.lyf.supplychain.common.security.constant.PermissionCodes.SYS_ROLE_MANAGE)
public class SysRoleMenuController extends BaseCrudController<SysRoleMenu> {

    public SysRoleMenuController(SysRoleMenuService service) {
        super(service);
    }
}
