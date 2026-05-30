package com.lyf.supplychain.system.controller;

import com.lyf.supplychain.system.entity.SysDictItem;
import com.lyf.supplychain.system.service.SysDictItemService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据字典明细 CRUD 控制器。
 *
 * @author liyunfei
 * @date 2026-05-15
 */
@RestController
@RequestMapping("/system/dict-items")
@com.lyf.supplychain.common.security.annotation.RequiresPermission(com.lyf.supplychain.common.security.constant.PermissionCodes.SYS_MENU_MANAGE)
public class SysDictItemController extends BaseCrudController<SysDictItem> {

    public SysDictItemController(SysDictItemService service) {
        super(service);
    }
}
