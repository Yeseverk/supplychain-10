package com.lyf.supplychain.common.security.constant;

/**
 * 安全体系通用常量。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
public final class SecurityConstants {

    public static final String AUTHORIZATION_HEADER = "Authorization";

    public static final String TENANT_ID_HEADER = "X-Tenant-Id";

    public static final String USER_ID_HEADER = "X-User-Id";

    public static final String USERNAME_HEADER = "X-Username";

    public static final String INTERNAL_REQUEST_HEADER = "X-Internal-Request";

    public static final String INTERNAL_REQUEST_VALUE = "supplychain";

    public static final String LOGIN_USER_SESSION_KEY = "loginUser";

    public static final String SESSION_TENANT_ID = "tenantId";

    public static final String SESSION_PLAN_TYPE = "planType";

    public static final String SESSION_USERNAME = "username";

    public static final String SESSION_REAL_NAME = "realName";

    public static final String ALL_PERMISSION = "*";

    public static final String RATE_LIMIT_KEY_PREFIX = "security:rate-limit:";

    public static final String PERMISSION_CACHE_PREFIX = "security:permission:";

    private SecurityConstants() {
    }
}
