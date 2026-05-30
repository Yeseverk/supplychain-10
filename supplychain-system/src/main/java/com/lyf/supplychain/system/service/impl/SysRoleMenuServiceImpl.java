package com.lyf.supplychain.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyf.supplychain.system.entity.SysRoleMenu;
import com.lyf.supplychain.system.mapper.SysRoleMenuMapper;
import com.lyf.supplychain.system.service.RbacPermissionService;
import com.lyf.supplychain.system.service.SysRoleMenuService;
import org.springframework.stereotype.Service;

import java.io.Serializable;

/**
 * 角色菜单关联服务实现。
 *
 * @author liyunfei
 * @date 2026-05-15
 */
@Service
public class SysRoleMenuServiceImpl extends ServiceImpl<SysRoleMenuMapper, SysRoleMenu> implements SysRoleMenuService {

    private final RbacPermissionService rbacPermissionService;

    public SysRoleMenuServiceImpl(RbacPermissionService rbacPermissionService) {
        this.rbacPermissionService = rbacPermissionService;
    }

    /**
     * 保存角色菜单关系后清理该角色下用户权限缓存。
     *
     * @param entity 角色菜单关系
     * @return 是否保存成功
     */
    @Override
    public boolean save(SysRoleMenu entity) {
        boolean success = super.save(entity);
        if (success) {
            evict(entity);
        }
        return success;
    }

    /**
     * 更新角色菜单关系后清理新旧角色下用户权限缓存。
     *
     * @param entity 角色菜单关系
     * @return 是否更新成功
     */
    @Override
    public boolean updateById(SysRoleMenu entity) {
        SysRoleMenu old = entity.getId() == null ? null : getById(entity.getId());
        boolean success = super.updateById(entity);
        if (success) {
            evict(old);
            evict(entity);
        }
        return success;
    }

    /**
     * 删除角色菜单关系后清理该角色下用户权限缓存。
     *
     * @param id 主键ID
     * @return 是否删除成功
     */
    @Override
    public boolean removeById(Serializable id) {
        SysRoleMenu old = getById(id);
        boolean success = super.removeById(id);
        if (success) {
            evict(old);
        }
        return success;
    }

    private void evict(SysRoleMenu roleMenu) {
        if (roleMenu == null) {
            return;
        }
        rbacPermissionService.evictRolePermissionCache(roleMenu.getTenantId(), roleMenu.getRoleId());
    }
}
