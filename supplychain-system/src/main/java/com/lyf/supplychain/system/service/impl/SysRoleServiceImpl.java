package com.lyf.supplychain.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.common.constant.ResultCode;
import com.lyf.supplychain.common.exception.BusinessException;
import com.lyf.supplychain.common.security.context.SecurityContextHolder;
import com.lyf.supplychain.common.security.model.LoginUser;
import com.lyf.supplychain.system.entity.SysRole;
import com.lyf.supplychain.system.entity.SysRoleMenu;
import com.lyf.supplychain.system.mapper.SysRoleMapper;
import com.lyf.supplychain.system.request.SysRolePageQuery;
import com.lyf.supplychain.system.service.RbacPermissionService;
import com.lyf.supplychain.system.service.SysRoleMenuService;
import com.lyf.supplychain.system.service.SysRoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * 角色服务实现。
 *
 * @author liyunfei
 * @date 2026-05-15
 */
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    private static final List<String> PLATFORM_ROLE_CODES = List.of(
            "ROLE_SUPER_ADMIN",
            "ROLE_PLATFORM_ADMIN",
            "ROLE_PLATFORM_OPS");

    private final RbacPermissionService rbacPermissionService;
    private final SysRoleMenuService roleMenuService;

    public SysRoleServiceImpl(RbacPermissionService rbacPermissionService, SysRoleMenuService roleMenuService) {
        this.rbacPermissionService = rbacPermissionService;
        this.roleMenuService = roleMenuService;
    }

    @Override
    public PageResult<SysRole> pageRoles(SysRolePageQuery query) {
        query.normalize();
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        Long tenantId = SecurityContextHolder.getTenantId();
        if (tenantId != null) {
            wrapper.in(SysRole::getTenantId, List.of(0L, tenantId));
        }
        if (!isPlatformOperator()) {
            wrapper.notIn(SysRole::getRoleCode, PLATFORM_ROLE_CODES);
        }
        if (StrUtil.isNotBlank(query.getKeyword())) {
            wrapper.and(w -> w
                    .like(SysRole::getRoleName, query.getKeyword())
                    .or().like(SysRole::getRoleCode, query.getKeyword())
                    .or().like(SysRole::getRemark, query.getKeyword()));
        }
        if (query.getStatus() != null) {
            wrapper.eq(SysRole::getStatus, query.getStatus());
        }
        wrapper.orderByAsc(SysRole::getSort).orderByDesc(SysRole::getCreateTime);
        return PageResult.from(page(Page.of(query.getPageNum(), query.getPageSize()), wrapper));
    }

    @Override
    public List<Long> listMenuIds(Long roleId) {
        SysRole role = requireRole(roleId);
        return roleMenuService.list(new LambdaQueryWrapper<SysRoleMenu>()
                        .eq(SysRoleMenu::getRoleId, role.getId())
                        .eq(SysRoleMenu::getTenantId, effectiveTenantId(role)))
                .stream()
                .map(SysRoleMenu::getMenuId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignMenus(Long roleId, List<Long> menuIds) {
        SysRole role = requireRole(roleId);
        Long tenantId = effectiveTenantId(role);
        roleMenuService.remove(new LambdaQueryWrapper<SysRoleMenu>()
                .eq(SysRoleMenu::getRoleId, role.getId())
                .eq(SysRoleMenu::getTenantId, tenantId));
        List<SysRoleMenu> relations = menuIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .map(menuId -> {
                    SysRoleMenu relation = new SysRoleMenu();
                    relation.setTenantId(tenantId);
                    relation.setRoleId(role.getId());
                    relation.setMenuId(menuId);
                    return relation;
                })
                .toList();
        if (!relations.isEmpty()) {
            roleMenuService.saveBatch(relations);
        }
        rbacPermissionService.evictRolePermissionCache(tenantId, role.getId());
    }

    /**
     * 更新角色基础信息后清理该角色下用户权限缓存。
     *
     * @param entity 角色实体
     * @return 是否更新成功
     */
    @Override
    public boolean updateById(SysRole entity) {
        boolean success = super.updateById(entity);
        if (success) {
            rbacPermissionService.evictRolePermissionCache(entity.getTenantId(), entity.getId());
        }
        return success;
    }

    /**
     * 删除角色后清理该角色下用户权限缓存。
     *
     * @param id 主键ID
     * @return 是否删除成功
     */
    @Override
    public boolean removeById(Serializable id) {
        SysRole old = getById(id);
        boolean success = super.removeById(id);
        if (success && old != null) {
            rbacPermissionService.evictRolePermissionCache(old.getTenantId(), old.getId());
        }
        return success;
    }

    private SysRole requireRole(Long roleId) {
        if (roleId == null || roleId <= 0) {
            BusinessException.throwException(ResultCode.PARAM_ERROR.getCode(), "角色ID必须大于0");
        }
        SysRole role = getById(roleId);
        if (role == null) {
            BusinessException.throwException(ResultCode.DATA_NOT_FOUND);
        }
        return role;
    }

    private Long effectiveTenantId(SysRole role) {
        if (role.getTenantId() != null) {
            return role.getTenantId();
        }
        Long tenantId = SecurityContextHolder.getTenantId();
        return tenantId == null ? 0L : tenantId;
    }

    private boolean isPlatformOperator() {
        LoginUser loginUser = SecurityContextHolder.getLoginUser();
        if (loginUser == null || loginUser.getRoles() == null) {
            return false;
        }
        return loginUser.getRoles().stream().anyMatch(PLATFORM_ROLE_CODES::contains);
    }
}
