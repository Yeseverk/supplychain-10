package com.lyf.supplychain.system.controller;

import com.lyf.supplychain.common.api.PageQuery;
import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.common.security.annotation.OperationLog;
import com.lyf.supplychain.common.security.annotation.RequiresPermission;
import com.lyf.supplychain.common.security.constant.PermissionCodes;
import com.lyf.supplychain.system.entity.SysUser;
import com.lyf.supplychain.system.request.SysUserPageQuery;
import com.lyf.supplychain.system.request.UserRoleAssignRequest;
import com.lyf.supplychain.system.request.UserStatusUpdateRequest;
import com.lyf.supplychain.system.service.SysUserService;
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
 * 系统用户 CRUD 控制器。
 *
 * @author liyunfei
 * @date 2026-05-15
 */
@RestController
@RequestMapping({"/system/users", "/api/system/users"})
@com.lyf.supplychain.common.security.annotation.RequiresPermission(com.lyf.supplychain.common.security.constant.PermissionCodes.SYS_USER_LIST)
public class SysUserController extends BaseCrudController<SysUser> {

    private final SysUserService userService;

    public SysUserController(SysUserService service) {
        super(service);
        this.userService = service;
    }

    @Override
    @GetMapping("/page")
    public R<PageResult<SysUser>> page(PageQuery pageQuery) {
        SysUserPageQuery query = new SysUserPageQuery();
        query.setPageNum(pageQuery.getPageNum());
        query.setPageSize(pageQuery.getPageSize());
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            query.setKeyword(attributes.getRequest().getParameter("keyword"));
            query.setStatus(parseInteger(attributes.getRequest().getParameter("status")));
        }
        return R.ok(userService.pageUsers(query));
    }

    /**
     * 手动解锁用户账号。
     *
     * @param id 用户ID
     * @return 无数据响应
     */
    @PutMapping("/{id}/unlock")
    @RequiresPermission(PermissionCodes.SYS_USER_EDIT)
    @OperationLog(module = "系统用户", action = "手动解锁用户账号", type = OperationLog.Type.UPDATE)
    public R<Void> unlock(@PathVariable("id") Long id) {
        userService.unlock(id);
        return R.ok();
    }

    @PutMapping("/{id}/status")
    @RequiresPermission(PermissionCodes.SYS_USER_EDIT)
    @OperationLog(module = "系统用户", action = "更新用户状态", type = OperationLog.Type.UPDATE)
    public R<Void> updateStatus(@PathVariable("id") Long id, @Valid @RequestBody UserStatusUpdateRequest request) {
        userService.updateStatus(id, request.getStatus());
        return R.ok();
    }

    @GetMapping("/{id}/roles")
    @RequiresPermission(PermissionCodes.SYS_USER_LIST)
    public R<List<Long>> listRoles(@PathVariable("id") Long id) {
        return R.ok(userService.listRoleIds(id));
    }

    @PutMapping("/{id}/roles")
    @RequiresPermission(PermissionCodes.SYS_USER_EDIT)
    @OperationLog(module = "系统用户", action = "分配用户角色", type = OperationLog.Type.UPDATE)
    public R<Void> assignRoles(@PathVariable("id") Long id, @Valid @RequestBody UserRoleAssignRequest request) {
        userService.assignRoles(id, request.getRoleIds());
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
