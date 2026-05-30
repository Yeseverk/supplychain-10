package com.lyf.supplychain.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyf.supplychain.system.entity.SysUserRole;
import com.lyf.supplychain.system.mapper.SysUserRoleMapper;
import com.lyf.supplychain.system.service.RbacPermissionService;
import com.lyf.supplychain.system.service.SysUserRoleService;
import org.springframework.stereotype.Service;

import java.io.Serializable;

/**
 * 用户角色关联服务实现。
 *
 * @author liyunfei
 * @date 2026-05-15
 */
@Service
public class SysUserRoleServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole> implements SysUserRoleService {

    private final RbacPermissionService rbacPermissionService;

    public SysUserRoleServiceImpl(RbacPermissionService rbacPermissionService) {
        this.rbacPermissionService = rbacPermissionService;
    }

    /**
     * 保存用户角色关系后清理权限缓存。
     *
     * @param entity 用户角色关系
     * @return 是否保存成功
     */
    @Override
    public boolean save(SysUserRole entity) {
        boolean success = super.save(entity);
        if (success) {
            evict(entity);
        }
        return success;
    }

    /**
     * 更新用户角色关系后清理新旧用户权限缓存。
     *
     * @param entity 用户角色关系
     * @return 是否更新成功
     */
    @Override
    public boolean updateById(SysUserRole entity) {
        SysUserRole old = entity.getId() == null ? null : getById(entity.getId());
        boolean success = super.updateById(entity);
        if (success) {
            evict(old);
            evict(entity);
        }
        return success;
    }

    /**
     * 删除用户角色关系后清理权限缓存。
     *
     * @param id 主键ID
     * @return 是否删除成功
     */
    @Override
    public boolean removeById(Serializable id) {
        SysUserRole old = getById(id);
        boolean success = super.removeById(id);
        if (success) {
            evict(old);
        }
        return success;
    }

    private void evict(SysUserRole userRole) {
        if (userRole == null) {
            return;
        }
        rbacPermissionService.evictUserPermissionCache(userRole.getTenantId(), userRole.getUserId());
    }
}
