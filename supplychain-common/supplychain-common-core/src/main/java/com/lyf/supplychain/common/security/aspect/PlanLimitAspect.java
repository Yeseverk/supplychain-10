package com.lyf.supplychain.common.security.aspect;

import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.common.constant.ResultCode;
import com.lyf.supplychain.common.context.TenantContext;
import com.lyf.supplychain.common.exception.BusinessException;
import com.lyf.supplychain.common.feign.system.SystemPlanLimitCheckRequest;
import com.lyf.supplychain.common.feign.system.SystemPlanLimitCheckResponse;
import com.lyf.supplychain.common.feign.system.SystemPlanLimitFeignClient;
import com.lyf.supplychain.common.security.annotation.PlanLimit;
import com.lyf.supplychain.common.security.context.SecurityContextHolder;
import com.lyf.supplychain.common.security.model.LoginUser;
import com.lyf.supplychain.common.security.plan.PlanUsageProvider;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 租户套餐限制切面。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Aspect
@Component
public class PlanLimitAspect {

    private final SystemPlanLimitFeignClient planLimitFeignClient;
    private final List<PlanUsageProvider> usageProviders;

    @Autowired
    public PlanLimitAspect(SystemPlanLimitFeignClient planLimitFeignClient,
                           List<PlanUsageProvider> usageProviders) {
        this.planLimitFeignClient = planLimitFeignClient;
        this.usageProviders = usageProviders;
    }

    /**
     * 校验当前租户套餐是否允许执行当前操作。
     *
     * @param joinPoint 切点
     * @param planLimit 套餐限制注解
     * @return 业务方法执行结果
     * @throws Throwable 业务异常
     */
    @Around("@annotation(planLimit)")
    public Object checkPlanLimit(ProceedingJoinPoint joinPoint, PlanLimit planLimit) throws Throwable {
        Long tenantId = resolveTenantId();
        if (tenantId == null) {
            BusinessException.throwException(ResultCode.UNAUTHORIZED);
        }
        R<SystemPlanLimitCheckResponse> response = planLimitFeignClient.check(buildRequest(tenantId, planLimit));
        if (response == null || !ResultCode.SUCCESS.getCode().equals(response.getCode())) {
            String message = response == null ? "套餐限制检查失败，请稍后重试" : response.getMsg();
            BusinessException.throwException(ResultCode.FORBIDDEN.getCode(), message);
        }
        return joinPoint.proceed();
    }

    private Long resolveTenantId() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId != null) {
            return tenantId;
        }
        LoginUser loginUser = SecurityContextHolder.getLoginUser();
        return loginUser == null ? null : loginUser.getTenantId();
    }

    private SystemPlanLimitCheckRequest buildRequest(Long tenantId, PlanLimit planLimit) {
        SystemPlanLimitCheckRequest request = new SystemPlanLimitCheckRequest();
        request.setTenantId(tenantId);
        request.setFeatureCode(planLimit.feature());
        request.setBizName(planLimit.bizName());
        request.setCurrentUsage(currentUsage(tenantId, planLimit.feature()));
        return request;
    }

    private Integer currentUsage(Long tenantId, String featureCode) {
        return usageProviders.stream()
                .filter(provider -> provider.supports(featureCode))
                .findFirst()
                .map(provider -> provider.currentUsage(tenantId, featureCode))
                .orElse(null);
    }
}
