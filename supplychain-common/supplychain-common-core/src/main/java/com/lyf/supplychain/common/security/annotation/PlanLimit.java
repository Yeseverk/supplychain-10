package com.lyf.supplychain.common.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 租户套餐限制校验注解。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PlanLimit {

    /**
     * 套餐功能编码。
     *
     * @return 功能编码
     */
    String feature();

    /**
     * 超限时展示的业务名称。
     *
     * @return 业务名称
     */
    String bizName() default "当前资源";
}
