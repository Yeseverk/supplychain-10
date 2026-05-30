package com.lyf.supplychain.common.security.aspect;

import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.common.constant.ResultCode;
import com.lyf.supplychain.common.context.TenantContext;
import com.lyf.supplychain.common.exception.BusinessException;
import com.lyf.supplychain.common.feign.system.SystemTenantWriteCheckRequest;
import com.lyf.supplychain.common.feign.system.SystemTenantWriteCheckResponse;
import com.lyf.supplychain.common.feign.system.SystemTenantWriteFeignClient;
import com.lyf.supplychain.common.security.annotation.TenantWriteGuard;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 租户写操作保护切面。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Aspect
@Component
public class TenantWriteGuardAspect {

    private final SystemTenantWriteFeignClient tenantWriteFeignClient;

    @Autowired
    public TenantWriteGuardAspect(SystemTenantWriteFeignClient tenantWriteFeignClient) {
        this.tenantWriteFeignClient = tenantWriteFeignClient;
    }

    /**
     * 在写操作执行前检查租户状态和套餐到期状态。
     *
     * @param joinPoint        切点
     * @param tenantWriteGuard 写操作保护注解
     * @return 业务方法执行结果
     * @throws Throwable 业务异常
     */
    @Around("@annotation(tenantWriteGuard)")
    public Object checkTenantCanWrite(ProceedingJoinPoint joinPoint, TenantWriteGuard tenantWriteGuard) throws Throwable {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            BusinessException.throwException(ResultCode.UNAUTHORIZED);
        }
        R<SystemTenantWriteCheckResponse> response = tenantWriteFeignClient.check(buildRequest(tenantId, tenantWriteGuard));
        if (response == null || !ResultCode.SUCCESS.getCode().equals(response.getCode())) {
            String message = response == null ? "租户写操作检查失败，请稍后重试" : response.getMsg();
            BusinessException.throwException(ResultCode.FORBIDDEN.getCode(), message);
        }
        return joinPoint.proceed();
    }

    private SystemTenantWriteCheckRequest buildRequest(Long tenantId, TenantWriteGuard tenantWriteGuard) {
        SystemTenantWriteCheckRequest request = new SystemTenantWriteCheckRequest();
        request.setTenantId(tenantId);
        request.setScene(tenantWriteGuard.scene());
        return request;
    }
}
