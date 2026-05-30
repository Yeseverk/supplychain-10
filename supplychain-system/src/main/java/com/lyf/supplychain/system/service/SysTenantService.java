package com.lyf.supplychain.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lyf.supplychain.system.entity.SysTenant;
import com.lyf.supplychain.system.model.saas.TenantPlanChangeRequest;
import com.lyf.supplychain.system.model.saas.TenantRegisterRequest;

/**
 * 租户信息服务接口。
 *
 * @author liyunfei
 * @date 2026-05-15
 */
public interface SysTenantService extends IService<SysTenant> {

    /**
     * 注册租户并初始化管理员账号。
     *
     * @param request 注册请求
     * @return 租户ID
     */
    Long registerTenant(TenantRegisterRequest request);

    /**
     * 启用租户。
     *
     * @param id 租户ID
     */
    void enableTenant(Long id);

    /**
     * 禁用租户。
     *
     * @param id 租户ID
     */
    void disableTenant(Long id);

    /**
     * 变更租户套餐。
     *
     * @param id      租户ID
     * @param request 套餐变更请求
     */
    void changePlan(Long id, TenantPlanChangeRequest request);
}
