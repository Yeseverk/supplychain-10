package com.lyf.supplychain.common.security.aspect;

import com.lyf.supplychain.common.security.annotation.DataScope;
import com.lyf.supplychain.common.security.datascope.DataScopeContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 数据权限上下文切面。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Aspect
@Component
public class DataScopeAspect {

    /**
     * 设置当前业务方法的数据权限资源类型。
     *
     * @param joinPoint 切点
     * @param dataScope 数据权限注解
     * @return 业务方法执行结果
     * @throws Throwable 业务异常
     */
    @Around("@annotation(dataScope)")
    public Object applyDataScope(ProceedingJoinPoint joinPoint, DataScope dataScope) throws Throwable {
        DataScopeContext.set(dataScope.resource());
        try {
            return joinPoint.proceed();
        } finally {
            DataScopeContext.clear();
        }
    }
}
