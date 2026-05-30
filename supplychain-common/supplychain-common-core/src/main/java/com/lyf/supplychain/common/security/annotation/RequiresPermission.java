package com.lyf.supplychain.common.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 业务接口权限校验注解。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresPermission {

    /**
     * 接口需要具备的权限标识。
     *
     * @return 权限标识数组
     */
    String[] value();

    /**
     * 多权限匹配模式，默认全部满足。
     *
     * @return 匹配模式
     */
    Mode mode() default Mode.AND;

    enum Mode {
        AND,
        OR
    }
}
