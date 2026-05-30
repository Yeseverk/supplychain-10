package com.lyf.supplychain.system.constant;

/**
 * 系统管理业务常量。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
public final class SystemConstants {

    public static final int STATUS_DISABLED = 0;

    public static final int STATUS_ENABLED = 1;

    public static final int STATUS_LOCKED = 2;

    public static final int TENANT_STATUS_DISABLED = 0;

    public static final int TENANT_STATUS_ENABLED = 1;

    public static final int TENANT_STATUS_TRIAL = 2;

    public static final int TENANT_STATUS_EXPIRED = 3;

    public static final int TENANT_STATUS_CANCELLED = 4;

    public static final int USER_TYPE_SUPER_ADMIN = 9;

    public static final int MAX_LOGIN_FAIL_COUNT = 5;

    public static final int LOGIN_LOCK_MINUTES = 30;

    private SystemConstants() {
    }
}
