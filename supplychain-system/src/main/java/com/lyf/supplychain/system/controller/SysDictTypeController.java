package com.lyf.supplychain.system.controller;

import com.lyf.supplychain.system.entity.SysDictType;
import com.lyf.supplychain.system.service.SysDictTypeService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据字典类型 CRUD 控制器。
 *
 * @author liyunfei
 * @date 2026-05-15
 */
@RestController
@RequestMapping("/system/dict-types")
@com.lyf.supplychain.common.security.annotation.RequiresPermission(com.lyf.supplychain.common.security.constant.PermissionCodes.SYS_MENU_MANAGE)
public class SysDictTypeController extends BaseCrudController<SysDictType> {

    public SysDictTypeController(SysDictTypeService service) {
        super(service);
    }
}
