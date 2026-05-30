package com.lyf.supplychain.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lyf.supplychain.common.feign.system.SystemPlanLimitCheckRequest;
import com.lyf.supplychain.common.feign.system.SystemPlanLimitCheckResponse;
import com.lyf.supplychain.common.feign.system.SystemTenantWriteCheckRequest;
import com.lyf.supplychain.common.feign.system.SystemTenantWriteCheckResponse;
import com.lyf.supplychain.system.entity.SysPlanFeature;

/**
 * 套餐功能开关服务接口。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
public interface SysPlanFeatureService extends IService<SysPlanFeature> {

    /**
     * 检查指定套餐是否开启功能。
     *
     * @param planType    套餐类型
     * @param featureCode 功能编码
     * @return 是否开启
     */
    boolean isFeatureEnabled(Integer planType, String featureCode);

    /**
     * 检查租户套餐限制。
     *
     * @param request 套餐限制检查请求
     * @return 套餐限制检查结果
     */
    SystemPlanLimitCheckResponse checkLimit(SystemPlanLimitCheckRequest request);

    /**
     * 检查租户当前是否允许写操作。
     *
     * @param request 写操作检查请求
     * @return 写操作检查结果
     */
    SystemTenantWriteCheckResponse checkTenantWrite(SystemTenantWriteCheckRequest request);
}
