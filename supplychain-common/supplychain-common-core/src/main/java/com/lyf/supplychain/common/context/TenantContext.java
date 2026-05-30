package com.lyf.supplychain.common.context;

import cn.hutool.core.util.ObjectUtil;

/**
 * 当前请求的租户与用户上下文。
 *
 * @author liyunfei
 * @date 2026-05-15
 */
public final class TenantContext {

    private static final ThreadLocal<TenantInfo> HOLDER = new ThreadLocal<>();

    private TenantContext() {
    }

    /**
     * 写入当前线程的租户与用户信息。
     *
     * @param tenantId 租户ID
     * @param userId   用户ID
     */
    public static void set(Long tenantId, Long userId) {
        HOLDER.set(new TenantInfo(tenantId, userId));
    }

    /**
     * 获取当前租户ID。
     *
     * @return 租户ID
     */
    public static Long getTenantId() {
        TenantInfo tenantInfo = HOLDER.get();
        return ObjectUtil.isNull(tenantInfo) ? null : tenantInfo.tenantId();
    }

    /**
     * 获取当前用户ID。
     *
     * @return 用户ID
     */
    public static Long getUserId() {
        TenantInfo tenantInfo = HOLDER.get();
        return ObjectUtil.isNull(tenantInfo) ? null : tenantInfo.userId();
    }

    /**
     * 清理当前线程上下文，避免线程复用导致租户串用。
     */
    public static void clear() {
        HOLDER.remove();
    }

    private record TenantInfo(Long tenantId, Long userId) {
    }
}
