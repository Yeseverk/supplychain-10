package com.lyf.supplychain.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lyf.supplychain.common.redis.CommonRedisKeys;
import com.lyf.supplychain.common.security.constant.SecurityConstants;
import com.lyf.supplychain.system.entity.SysUserRole;
import com.lyf.supplychain.system.mapper.SysMenuMapper;
import com.lyf.supplychain.system.mapper.SysRoleMapper;
import com.lyf.supplychain.system.mapper.SysUserRoleMapper;
import com.lyf.supplychain.system.service.RbacPermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

/**
 * RBAC 权限查询服务实现。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Slf4j
@Service
public class RbacPermissionServiceImpl implements RbacPermissionService {

    private static final Duration PERMISSION_CACHE_TTL = Duration.ofHours(2);

    private final SysMenuMapper menuMapper;

    private final SysRoleMapper roleMapper;

    private final SysUserRoleMapper userRoleMapper;

    private final StringRedisTemplate redisTemplate;

    public RbacPermissionServiceImpl(SysMenuMapper menuMapper,
                                     SysRoleMapper roleMapper,
                                     SysUserRoleMapper userRoleMapper,
                                     ObjectProvider<StringRedisTemplate> redisTemplateProvider) {
        this.menuMapper = menuMapper;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.redisTemplate = redisTemplateProvider.getIfAvailable();
    }

    /**
     * 查询用户拥有的权限标识，超级管理员角色拥有通配权限。
     *
     * @param tenantId 租户ID
     * @param userId   用户ID
     * @return 权限标识集合
     */
    @Override
    public List<String> listPermissions(Long tenantId, Long userId) {
        String cacheKey = CommonRedisKeys.permission(tenantId, userId);
        List<String> cachedPermissions = readList(cacheKey);
        if (cachedPermissions != null) {
            return cachedPermissions;
        }
        List<String> roles = listRoles(tenantId, userId);
        List<String> permissions = roles.contains("ROLE_SUPER_ADMIN")
                ? List.of(SecurityConstants.ALL_PERMISSION)
                : menuMapper.selectPermissionsByUserId(tenantId, userId);
        writeList(cacheKey, permissions);
        return permissions;
    }

    /**
     * 查询用户拥有的角色编码。
     *
     * @param tenantId 租户ID
     * @param userId   用户ID
     * @return 角色编码集合
     */
    @Override
    public List<String> listRoles(Long tenantId, Long userId) {
        String cacheKey = CommonRedisKeys.roles(tenantId, userId);
        List<String> cachedRoles = readList(cacheKey);
        if (cachedRoles != null) {
            return cachedRoles;
        }
        List<String> roles = roleMapper.selectRoleCodesByUserId(tenantId, userId);
        writeList(cacheKey, roles);
        return roles;
    }

    /**
     * 查询用户数据权限范围。
     *
     * @param tenantId 租户ID
     * @param userId   用户ID
     * @return 数据权限范围
     */
    @Override
    public Integer dataScope(Long tenantId, Long userId) {
        String cacheKey = CommonRedisKeys.dataScope(tenantId, userId);
        String cachedDataScope = read(cacheKey);
        if (StrUtil.isNotBlank(cachedDataScope)) {
            return Integer.valueOf(cachedDataScope);
        }
        Integer dataScope = roleMapper.selectDataScopeByUserId(tenantId, userId);
        Integer result = dataScope == null ? 3 : dataScope;
        write(cacheKey, String.valueOf(result));
        return result;
    }

    /**
     * 清理指定用户的权限、角色和数据权限缓存。
     *
     * @param tenantId 租户ID
     * @param userId   用户ID
     */
    @Override
    public void evictUserPermissionCache(Long tenantId, Long userId) {
        if (redisTemplate == null || tenantId == null || userId == null) {
            return;
        }
        try {
            redisTemplate.delete(List.of(
                    CommonRedisKeys.permission(tenantId, userId),
                    CommonRedisKeys.roles(tenantId, userId),
                    CommonRedisKeys.dataScope(tenantId, userId)));
        } catch (Exception exception) {
            log.warn("清理用户权限缓存失败，tenantId={}，userId={}", tenantId, userId, exception);
        }
    }

    /**
     * 根据角色清理受影响用户的权限缓存。
     *
     * @param tenantId 租户ID
     * @param roleId   角色ID
     */
    @Override
    public void evictRolePermissionCache(Long tenantId, Long roleId) {
        if (tenantId == null || roleId == null) {
            return;
        }
        List<SysUserRole> userRoles = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getTenantId, tenantId)
                .eq(SysUserRole::getRoleId, roleId));
        if (CollUtil.isEmpty(userRoles)) {
            return;
        }
        userRoles.stream()
                .map(SysUserRole::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .forEach(userId -> evictUserPermissionCache(tenantId, userId));
    }

    private List<String> readList(String cacheKey) {
        String cachedValue = read(cacheKey);
        if (StrUtil.isBlank(cachedValue)) {
            return null;
        }
        try {
            return JSONUtil.toList(JSONUtil.parseArray(cachedValue), String.class);
        } catch (Exception exception) {
            log.warn("读取权限列表缓存失败，cacheKey={}", cacheKey, exception);
            return null;
        }
    }

    private String read(String cacheKey) {
        if (redisTemplate == null) {
            return null;
        }
        try {
            return redisTemplate.opsForValue().get(cacheKey);
        } catch (Exception exception) {
            log.warn("读取权限缓存失败，cacheKey={}", cacheKey, exception);
            return null;
        }
    }

    private void writeList(String cacheKey, List<String> values) {
        write(cacheKey, JSONUtil.toJsonStr(CollUtil.emptyIfNull(values)));
    }

    private void write(String cacheKey, String value) {
        if (redisTemplate == null) {
            return;
        }
        try {
            redisTemplate.opsForValue().set(cacheKey, value, PERMISSION_CACHE_TTL);
        } catch (Exception exception) {
            log.warn("写入权限缓存失败，cacheKey={}", cacheKey, exception);
        }
    }
}
