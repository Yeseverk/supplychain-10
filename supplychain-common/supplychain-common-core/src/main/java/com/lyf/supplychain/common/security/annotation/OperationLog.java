package com.lyf.supplychain.common.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作审计日志注解。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperationLog {

    /**
     * 业务模块名称。
     *
     * @return 模块名称
     */
    String module();

    /**
     * 操作描述。
     *
     * @return 操作描述
     */
    String action();

    /**
     * 操作类型。
     *
     * @return 操作类型
     */
    Type type() default Type.UPDATE;

    /**
     * 是否保存请求参数。
     *
     * @return 是否保存参数
     */
    boolean saveParam() default true;

    /**
     * 需要脱敏的字段名。
     *
     * @return 字段名数组
     */
    String[] sensitiveFields() default {"password", "oldPassword", "newPassword", "bankAccount", "secret", "token"};

    enum Type {
        INSERT,
        UPDATE,
        DELETE,
        QUERY,
        EXPORT,
        IMPORT
    }
}
