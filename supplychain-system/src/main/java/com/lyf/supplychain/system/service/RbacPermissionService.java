package com.lyf.supplychain.system.service;

import java.util.List;

/**
 * RBAC 权限查询服务。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
public interface RbacPermissionService {

    /**
     * 查询用户拥有的权限标识。
     *
     * @param tenantId 租户ID
     * @param userId   用户ID
     * @return 权限标识集合
     */
    List<String> listPermissions(Long tenantId, Long userId);

    /**
     * 查询用户拥有的角色编码。
     *
     * @param tenantId 租户ID
     * @param userId   用户ID
     * @return 角色编码集合
     */
    List<String> listRoles(Long tenantId, Long userId);

    /**
     * 查询用户数据权限范围。
     *
     * @param tenantId 租户ID
     * @param userId   用户ID
     * @return 数据权限范围
     */
    Integer dataScope(Long tenantId, Long userId);

    /**
     * 清理指定用户的权限缓存。
     *
     * @param tenantId 租户ID
     * @param userId   用户ID
     */
    void evictUserPermissionCache(Long tenantId, Long userId);

    /**
     * 清理指定角色下所有用户的权限缓存。
     *
     * @param tenantId 租户ID
     * @param roleId   角色ID
     */
    void evictRolePermissionCache(Long tenantId, Long roleId);
}
