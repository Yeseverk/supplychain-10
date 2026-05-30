package com.lyf.supplychain.common.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 租户写操作保护注解。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TenantWriteGuard {

    /**
     * 写操作业务场景。
     *
     * @return 业务场景
     */
    String scene();
}
