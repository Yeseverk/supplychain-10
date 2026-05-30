package com.lyf.supplychain.common.security.aspect;

import com.lyf.supplychain.common.constant.ResultCode;
import com.lyf.supplychain.common.exception.BusinessException;
import com.lyf.supplychain.common.security.annotation.RequiresPermission;
import com.lyf.supplychain.common.security.context.SecurityContextHolder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

import java.util.Arrays;

/**
 * 权限注解校验切面。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Aspect
@Component
public class RequiresPermissionAspect {

    /**
     * 校验方法或类上声明的权限标识。
     *
     * @param joinPoint          切点
     * @param requiresPermission 权限注解
     * @return 业务方法执行结果
     * @throws Throwable 业务异常
     */
    @Around("@annotation(com.lyf.supplychain.common.security.annotation.RequiresPermission) || @within(com.lyf.supplychain.common.security.annotation.RequiresPermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!matches(resolveAnnotation(joinPoint))) {
            BusinessException.throwException(ResultCode.FORBIDDEN);
        }
        return joinPoint.proceed();
    }

    private RequiresPermission resolveAnnotation(ProceedingJoinPoint joinPoint) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        RequiresPermission methodAnnotation = AnnotationUtils.findAnnotation(method, RequiresPermission.class);
        if (methodAnnotation != null) {
            return methodAnnotation;
        }
        return AnnotationUtils.findAnnotation(joinPoint.getTarget().getClass(), RequiresPermission.class);
    }

    private boolean matches(RequiresPermission requiresPermission) {
        if (requiresPermission == null || requiresPermission.value().length == 0) {
            return true;
        }
        if (RequiresPermission.Mode.OR == requiresPermission.mode()) {
            return Arrays.stream(requiresPermission.value()).anyMatch(SecurityContextHolder::hasPermission);
        }
        return Arrays.stream(requiresPermission.value()).allMatch(SecurityContextHolder::hasPermission);
    }
}
