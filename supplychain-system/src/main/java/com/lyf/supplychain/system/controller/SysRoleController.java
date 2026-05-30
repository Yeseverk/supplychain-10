package com.lyf.supplychain.system.controller;

import com.lyf.supplychain.common.api.PageQuery;
import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.system.entity.SysRole;
import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.common.security.annotation.OperationLog;
import com.lyf.supplychain.system.request.RoleMenuAssignRequest;
import com.lyf.supplychain.system.request.SysRolePageQuery;
import com.lyf.supplychain.system.service.SysRoleService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

/**
 * 角色 CRUD 控制器。
 *
 * @author liyunfei
 * @date 2026-05-15
 */
@RestController
@RequestMapping({"/system/roles", "/api/system/roles"})
@com.lyf.supplychain.common.security.annotation.RequiresPermission(com.lyf.supplychain.common.security.constant.PermissionCodes.SYS_ROLE_MANAGE)
public class SysRoleController extends BaseCrudController<SysRole> {

    private final SysRoleService roleService;

    public SysRoleController(SysRoleService service) {
        super(service);
        this.roleService = service;
    }

    @Override
    @GetMapping("/page")
    public R<PageResult<SysRole>> page(PageQuery pageQuery) {
        SysRolePageQuery query = new SysRolePageQuery();
        query.setPageNum(pageQuery.getPageNum());
        query.setPageSize(pageQuery.getPageSize());
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            query.setKeyword(attributes.getRequest().getParameter("keyword"));
            query.setStatus(parseInteger(attributes.getRequest().getParameter("status")));
        }
        return R.ok(roleService.pageRoles(query));
    }

    /**
     * 查询角色已绑定菜单。
     *
     * @param id 角色ID
     * @return 菜单ID集合
     */
    @GetMapping("/{id}/menus")
    public R<List<Long>> listMenus(@PathVariable("id") Long id) {
        return R.ok(roleService.listMenuIds(id));
    }

    /**
     * 批量保存角色菜单授权。
     *
     * @param id      角色ID
     * @param request 授权菜单集合
     * @return 无数据响应
     */
    @PutMapping("/{id}/menus")
    @OperationLog(module = "系统角色", action = "分配角色菜单权限", type = OperationLog.Type.UPDATE)
    public R<Void> assignMenus(@PathVariable("id") Long id, @Valid @RequestBody RoleMenuAssignRequest request) {
        roleService.assignMenus(id, request.getMenuIds());
        return R.ok();
    }

    private Integer parseInteger(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
