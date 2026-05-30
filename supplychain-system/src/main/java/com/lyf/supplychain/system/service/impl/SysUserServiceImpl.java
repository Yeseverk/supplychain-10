package com.lyf.supplychain.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.common.exception.BusinessException;
import com.lyf.supplychain.common.security.context.SecurityContextHolder;
import com.lyf.supplychain.common.security.model.LoginUser;
import com.lyf.supplychain.system.constant.SystemConstants;
import com.lyf.supplychain.system.entity.SysRole;
import com.lyf.supplychain.system.entity.SysUser;
import com.lyf.supplychain.system.entity.SysUserRole;
import com.lyf.supplychain.system.mapper.SysRoleMapper;
import com.lyf.supplychain.system.mapper.SysUserMapper;
import com.lyf.supplychain.system.request.SysUserPageQuery;
import com.lyf.supplychain.system.service.RbacPermissionService;
import com.lyf.supplychain.system.service.SysUserRoleService;
import com.lyf.supplychain.system.service.SysUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 系统用户服务实现。
 *
 * @author liyunfei
 * @date 2026-05-15
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private static final List<String> PLATFORM_ROLE_CODES = List.of(
            "ROLE_SUPER_ADMIN",
            "ROLE_PLATFORM_ADMIN",
            "ROLE_PLATFORM_OPS");

    private final SysUserRoleService userRoleService;
    private final SysRoleMapper roleMapper;
    private final RbacPermissionService rbacPermissionService;

    public SysUserServiceImpl(SysUserRoleService userRoleService,
                              SysRoleMapper roleMapper,
                              RbacPermissionService rbacPermissionService) {
        this.userRoleService = userRoleService;
        this.roleMapper = roleMapper;
        this.rbacPermissionService = rbacPermissionService;
    }

    @Override
    public PageResult<SysUser> pageUsers(SysUserPageQuery query) {
        query.normalize();
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        Long tenantId = SecurityContextHolder.getTenantId();
        if (tenantId != null) {
            wrapper.eq(SysUser::getTenantId, tenantId);
        }
        if (StrUtil.isNotBlank(query.getKeyword())) {
            wrapper.and(w -> w
                    .like(SysUser::getUsername, query.getKeyword())
                    .or().like(SysUser::getRealName, query.getKeyword())
                    .or().like(SysUser::getEmail, query.getKeyword())
                    .or().like(SysUser::getPhone, query.getKeyword()));
        }
        if (query.getStatus() != null) {
            wrapper.eq(SysUser::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(SysUser::getCreateTime);
        return PageResult.from(page(Page.of(query.getPageNum(), query.getPageSize()), wrapper));
    }

    /**
     * 手动解锁用户账号并清空登录失败次数。
     *
     * @param id 用户ID
     */
    @Override
    public void unlock(Long id) {
        SysUser user = getById(id);
        if (user == null) {
            BusinessException.throwException("用户不存在");
        }
        SysUser update = new SysUser();
        update.setId(id);
        update.setStatus(SystemConstants.STATUS_ENABLED);
        update.setLoginFailCount(0);
        update.setLockTime(null);
        update.setLockUntilTime(null);
        if (!updateById(update)) {
            BusinessException.throwException("用户解锁失败");
        }
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        requireUser(id);
        if (!Objects.equals(status, SystemConstants.STATUS_ENABLED) && !Objects.equals(status, 0)) {
            BusinessException.throwException("用户状态只能为启用或停用");
        }
        SysUser update = new SysUser();
        update.setId(id);
        update.setStatus(status);
        if (Objects.equals(status, SystemConstants.STATUS_ENABLED)) {
            update.setLoginFailCount(0);
            update.setLockTime(null);
            update.setLockUntilTime(null);
        }
        if (!updateById(update)) {
            BusinessException.throwException("用户状态更新失败");
        }
        SysUser user = getById(id);
        if (user != null) {
            rbacPermissionService.evictUserPermissionCache(effectiveTenantId(user), user.getId());
        }
    }

    @Override
    public List<Long> listRoleIds(Long userId) {
        SysUser user = requireUser(userId);
        Long tenantId = effectiveTenantId(user);
        List<Long> roleIds = userRoleService.list(new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getTenantId, tenantId)
                        .eq(SysUserRole::getUserId, user.getId()))
                .stream()
                .map(SysUserRole::getRoleId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (roleIds.isEmpty() || isPlatformOperator()) {
            return roleIds;
        }
        return roleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                        .in(SysRole::getId, roleIds)
                        .in(SysRole::getTenantId, List.of(0L, tenantId))
                        .notIn(SysRole::getRoleCode, PLATFORM_ROLE_CODES))
                .stream()
                .map(SysRole::getId)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(Long userId, List<Long> roleIds) {
        SysUser user = requireUser(userId);
        Long tenantId = effectiveTenantId(user);
        List<Long> distinctRoleIds = roleIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (!distinctRoleIds.isEmpty()) {
            long roleCount = roleMapper.selectCount(new LambdaQueryWrapper<SysRole>()
                    .in(SysRole::getId, distinctRoleIds)
                    .in(SysRole::getTenantId, List.of(0L, tenantId))
                    .notIn(!isPlatformOperator(), SysRole::getRoleCode, PLATFORM_ROLE_CODES)
                    .eq(SysRole::getStatus, SystemConstants.STATUS_ENABLED));
            if (roleCount != distinctRoleIds.size()) {
                BusinessException.throwException("角色不存在或已停用");
            }
        }
        userRoleService.remove(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getTenantId, tenantId)
                .eq(SysUserRole::getUserId, user.getId()));
        List<SysUserRole> relations = distinctRoleIds.stream()
                .map(roleId -> {
                    SysUserRole relation = new SysUserRole();
                    relation.setTenantId(tenantId);
                    relation.setUserId(user.getId());
                    relation.setRoleId(roleId);
                    return relation;
                })
                .toList();
        if (!relations.isEmpty()) {
            userRoleService.saveBatch(relations);
        }
        rbacPermissionService.evictUserPermissionCache(tenantId, user.getId());
    }

    private SysUser requireUser(Long userId) {
        if (userId == null || userId <= 0) {
            BusinessException.throwException("用户ID必须大于0");
        }
        SysUser user = getById(userId);
        if (user == null) {
            BusinessException.throwException("用户不存在");
        }
        return user;
    }

    private Long effectiveTenantId(SysUser user) {
        if (user.getTenantId() != null) {
            return user.getTenantId();
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
