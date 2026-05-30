package com.lyf.supplychain.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyf.supplychain.common.exception.BusinessException;
import com.lyf.supplychain.common.feign.system.SystemPlanLimitCheckRequest;
import com.lyf.supplychain.common.feign.system.SystemPlanLimitCheckResponse;
import com.lyf.supplychain.common.feign.system.SystemTenantWriteCheckRequest;
import com.lyf.supplychain.common.feign.system.SystemTenantWriteCheckResponse;
import com.lyf.supplychain.system.constant.SystemConstants;
import com.lyf.supplychain.system.entity.SysPlanFeature;
import com.lyf.supplychain.system.entity.SysTenant;
import com.lyf.supplychain.system.mapper.SysPlanFeatureMapper;
import com.lyf.supplychain.system.mapper.SysTenantMapper;
import com.lyf.supplychain.system.service.SysPlanFeatureService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 套餐功能开关服务实现。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Service
public class SysPlanFeatureServiceImpl extends ServiceImpl<SysPlanFeatureMapper, SysPlanFeature> implements SysPlanFeatureService {

    private final SysTenantMapper tenantMapper;

    public SysPlanFeatureServiceImpl(SysTenantMapper tenantMapper) {
        this.tenantMapper = tenantMapper;
    }

    /**
     * 检查指定套餐是否开启功能。
     *
     * @param planType    套餐类型
     * @param featureCode 功能编码
     * @return 是否开启
     */
    @Override
    public boolean isFeatureEnabled(Integer planType, String featureCode) {
        Long count = count(new LambdaQueryWrapper<SysPlanFeature>()
                .eq(SysPlanFeature::getPlanType, planType)
                .eq(SysPlanFeature::getFeatureCode, featureCode)
                .eq(SysPlanFeature::getIsEnabled, 1));
        return count > 0;
    }

    /**
     * 检查租户套餐限制。
     *
     * @param request 套餐限制检查请求
     * @return 套餐限制检查结果
     */
    @Override
    public SystemPlanLimitCheckResponse checkLimit(SystemPlanLimitCheckRequest request) {
        SysTenant tenant = tenantMapper.selectById(request.getTenantId());
        if (tenant == null) {
            BusinessException.throwException("租户不存在");
        }
        if (Integer.valueOf(SystemConstants.TENANT_STATUS_DISABLED).equals(tenant.getStatus())) {
            BusinessException.throwException("租户已被禁用，无法继续操作");
        }
        if (tenant.getPlanEndTime() != null && tenant.getPlanEndTime().isBefore(LocalDateTime.now())) {
            BusinessException.throwException("租户套餐已到期，请续费后继续操作");
        }
        SysPlanFeature feature = getOne(new LambdaQueryWrapper<SysPlanFeature>()
                .eq(SysPlanFeature::getPlanType, tenant.getPlanType())
                .eq(SysPlanFeature::getFeatureCode, request.getFeatureCode())
                .last("limit 1"));
        if (feature == null || !Integer.valueOf(1).equals(feature.getIsEnabled())) {
            BusinessException.throwException("当前套餐不支持" + request.getBizName() + "，请升级套餐");
        }
        Integer currentUsage = request.getCurrentUsage();
        if (feature.getLimitValue() != null && currentUsage != null && currentUsage >= feature.getLimitValue()) {
            BusinessException.throwException("当前套餐最多支持" + feature.getLimitValue()
                    + feature.getLimitUnit() + request.getBizName() + "，请升级套餐");
        }
        return response(tenant, feature, currentUsage);
    }

    /**
     * 检查租户是否仍允许执行写操作。
     *
     * @param request 写操作检查请求
     * @return 写操作检查结果
     */
    @Override
    public SystemTenantWriteCheckResponse checkTenantWrite(SystemTenantWriteCheckRequest request) {
        SysTenant tenant = tenantMapper.selectById(request.getTenantId());
        if (tenant == null) {
            BusinessException.throwException("租户不存在");
        }
        if (Integer.valueOf(SystemConstants.TENANT_STATUS_DISABLED).equals(tenant.getStatus())) {
            BusinessException.throwException("租户已被禁用，当前不允许执行写操作");
        }
        if (Integer.valueOf(SystemConstants.TENANT_STATUS_CANCELLED).equals(tenant.getStatus())) {
            BusinessException.throwException("租户已注销，当前不允许执行写操作");
        }
        if (Integer.valueOf(SystemConstants.TENANT_STATUS_EXPIRED).equals(tenant.getStatus())
                || (tenant.getPlanEndTime() != null && tenant.getPlanEndTime().isBefore(LocalDateTime.now()))) {
            BusinessException.throwException("租户套餐已到期，当前仅允许查看历史数据");
        }
        SystemTenantWriteCheckResponse response = new SystemTenantWriteCheckResponse();
        response.setTenantId(tenant.getId());
        response.setCanWrite(true);
        response.setReason("允许执行写操作");
        return response;
    }

    private SystemPlanLimitCheckResponse response(SysTenant tenant, SysPlanFeature feature, Integer currentUsage) {
        SystemPlanLimitCheckResponse response = new SystemPlanLimitCheckResponse();
        response.setPlanType(tenant.getPlanType());
        response.setFeatureCode(feature.getFeatureCode());
        response.setFeatureName(feature.getFeatureName());
        response.setLimitValue(feature.getLimitValue());
        response.setLimitUnit(feature.getLimitUnit());
        response.setCurrentUsage(currentUsage);
        return response;
    }
}
