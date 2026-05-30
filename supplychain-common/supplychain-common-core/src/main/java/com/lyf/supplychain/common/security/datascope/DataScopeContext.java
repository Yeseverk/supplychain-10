package com.lyf.supplychain.common.security.datascope;

/**
 * 数据权限上下文。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
public final class DataScopeContext {

    private static final ThreadLocal<DataScopeResource> HOLDER = new ThreadLocal<>();

    private DataScopeContext() {
    }

    /**
     * 设置当前数据权限资源。
     *
     * @param resource 数据资源类型
     */
    public static void set(DataScopeResource resource) {
        HOLDER.set(resource);
    }

    /**
     * 获取当前数据权限资源。
     *
     * @return 数据资源类型
     */
    public static DataScopeResource get() {
        return HOLDER.get();
    }

    /**
     * 清理当前数据权限资源。
     */
    public static void clear() {
        HOLDER.remove();
    }
}
