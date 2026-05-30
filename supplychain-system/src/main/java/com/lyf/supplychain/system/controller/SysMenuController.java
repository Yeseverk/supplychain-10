package com.lyf.supplychain.system.controller;

import com.lyf.supplychain.system.entity.SysMenu;
import com.lyf.supplychain.system.service.SysMenuService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 菜单权限 CRUD 控制器。
 *
 * @author liyunfei
 * @date 2026-05-15
 */
@RestController
@RequestMapping({"/system/menus", "/api/system/menus"})
@com.lyf.supplychain.common.security.annotation.RequiresPermission(com.lyf.supplychain.common.security.constant.PermissionCodes.SYS_MENU_MANAGE)
public class SysMenuController extends BaseCrudController<SysMenu> {

    public SysMenuController(SysMenuService service) {
        super(service);
    }
}
