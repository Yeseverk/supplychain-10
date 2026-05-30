package com.lyf.supplychain.common.security.annotation;

import com.lyf.supplychain.common.security.datascope.DataScopeResource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据权限过滤注解。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataScope {

    /**
     * 数据资源类型。
     *
     * @return 数据资源类型
     */
    DataScopeResource resource();
}
