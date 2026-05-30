package com.lyf.supplychain.common.redis;

import cn.hutool.core.util.StrUtil;

/**
 * 全项目通用 Redis Key。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
public final class CommonRedisKeys {

    public static final String PREFIX = "supplychain:";

    private CommonRedisKeys() {
    }

    /**
     * 用户权限缓存 Key。
     *
     * @param tenantId 租户ID
     * @param userId 用户ID
     * @return Redis Key
     */
    public static String permission(Long tenantId, Long userId) {
        return PREFIX + "permission:" + normalize(String.valueOf(tenantId)) + ":" + normalize(String.valueOf(userId));
    }

    /**
     * 用户角色缓存 Key。
     *
     * @param tenantId 租户ID
     * @param userId   用户ID
     * @return Redis Key
     */
    public static String roles(Long tenantId, Long userId) {
        return PREFIX + "roles:" + normalize(String.valueOf(tenantId)) + ":" + normalize(String.valueOf(userId));
    }

    /**
     * 用户数据权限缓存 Key。
     *
     * @param tenantId 租户ID
     * @param userId   用户ID
     * @return Redis Key
     */
    public static String dataScope(Long tenantId, Long userId) {
        return PREFIX + "data-scope:" + normalize(String.valueOf(tenantId)) + ":" + normalize(String.valueOf(userId));
    }

    /**
     * 登录刷新令牌 Key。
     *
     * @param refreshToken 刷新令牌
     * @return Redis Key
     */
    public static String refreshToken(String refreshToken) {
        return PREFIX + "auth:refresh-token:" + normalize(refreshToken);
    }

    /**
     * 用户权限缓存 Key。
     *
     * @param userId 用户ID
     * @return Redis Key
     */
    public static String permission(Long userId) {
        return PREFIX + "permission:" + userId;
    }

    /**
     * 分布式锁 Key。
     *
     * @param scene 业务场景
     * @param bizId 业务ID
     * @return Redis Key
     */
    public static String lock(String scene, String bizId) {
        return PREFIX + "lock:" + normalize(scene) + ":" + normalize(bizId);
    }

    /**
     * Tenant-aware distributed lock key.
     *
     * @param tenantId tenant ID
     * @param scene    business scene
     * @param bizId    business ID
     * @return Redis Key
     */
    public static String lock(Long tenantId, String scene, String bizId) {
        return PREFIX + "lock:" + normalize(String.valueOf(tenantId)) + ":" + normalize(scene) + ":" + normalize(bizId);
    }

    /**
     * 幂等标记 Key。
     *
     * @param scene 业务场景
     * @param token 幂等令牌
     * @return Redis Key
     */
    public static String idempotent(String scene, String token) {
        return PREFIX + "idempotent:" + normalize(scene) + ":" + normalize(token);
    }

    /**
     * Tenant-aware idempotent marker key.
     *
     * @param tenantId tenant ID
     * @param scene    business scene
     * @param token    idempotent token
     * @return Redis Key
     */
    public static String idempotent(Long tenantId, String scene, String token) {
        return PREFIX + "idempotent:" + normalize(String.valueOf(tenantId)) + ":" + normalize(scene) + ":" + normalize(token);
    }

    /**
     * 领域事件去重 Key。
     *
     * @param eventId 事件ID
     * @return Redis Key
     */
    public static String event(String eventId) {
        return PREFIX + "event:" + normalize(eventId);
    }

    /**
     * WMS SKU 多仓库存缓存 Key。
     *
     * @param tenantId 租户ID
     * @param skuId    SKU ID
     * @return Redis Key
     */
    public static String wmsInventorySku(Long tenantId, Long skuId) {
        return PREFIX + "wms:inventory:sku:" + normalize(String.valueOf(tenantId)) + ":" + normalize(String.valueOf(skuId));
    }

    private static String normalize(String value) {
        return StrUtil.blankToDefault(value, "unknown").trim();
    }
}
