package com.lyf.supplychain.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyf.supplychain.common.exception.BusinessException;
import com.lyf.supplychain.system.constant.SystemConstants;
import com.lyf.supplychain.system.entity.SysTenant;
import com.lyf.supplychain.system.entity.SysUser;
import com.lyf.supplychain.system.entity.SysUserRole;
import com.lyf.supplychain.system.mapper.SysTenantMapper;
import com.lyf.supplychain.system.mapper.SysUserMapper;
import com.lyf.supplychain.system.mapper.SysUserRoleMapper;
import com.lyf.supplychain.system.model.saas.TenantPlanChangeRequest;
import com.lyf.supplychain.system.model.saas.TenantRegisterRequest;
import com.lyf.supplychain.system.service.SysTenantService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 租户信息服务实现。
 *
 * @author liyunfei
 * @date 2026-05-15
 */
@Service
public class SysTenantServiceImpl extends ServiceImpl<SysTenantMapper, SysTenant> implements SysTenantService {

    private static final long ROLE_TENANT_ADMIN_ID = 2L;

    private final SysUserMapper userMapper;

    private final SysUserRoleMapper userRoleMapper;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public SysTenantServiceImpl(SysUserMapper userMapper, SysUserRoleMapper userRoleMapper) {
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
    }

    /**
     * 注册租户并初始化租户管理员。
     *
     * @param request 注册请求
     * @return 租户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long registerTenant(TenantRegisterRequest request) {
        SysTenant tenant = new SysTenant();
        tenant.setTenantCode(generateTenantCode());
        tenant.setCompanyName(request.getCompanyName());
        tenant.setContactName(request.getContactName());
        tenant.setContactPhone(request.getContactPhone());
        tenant.setContactEmail(request.getContactEmail());
        tenant.setPlanType(request.getPlanType());
        tenant.setPlanStartTime(LocalDateTime.now());
        tenant.setPlanEndTime(LocalDateTime.now().plusDays(14));
        tenant.setTrialEndTime(LocalDateTime.now().plusDays(14));
        tenant.setStatus(SystemConstants.TENANT_STATUS_TRIAL);
        applyPlanLimit(tenant, request.getPlanType());
        save(tenant);

        SysUser admin = new SysUser();
        admin.setTenantId(tenant.getId());
        admin.setUsername(request.getContactEmail());
        admin.setPassword(passwordEncoder.encode(request.getPassword()));
        admin.setRealName(request.getContactName());
        admin.setEmail(request.getContactEmail());
        admin.setPhone(request.getContactPhone());
        admin.setUserType(2);
        admin.setStatus(SystemConstants.STATUS_ENABLED);
        admin.setLoginFailCount(0);
        userMapper.insert(admin);
        assignTenantAdminRole(tenant.getId(), admin.getId());

        SysTenant update = new SysTenant();
        update.setId(tenant.getId());
        update.setAdminUserId(admin.getId());
        updateById(update);
        return tenant.getId();
    }

    private void assignTenantAdminRole(Long tenantId, Long userId) {
        SysUserRole userRole = new SysUserRole();
        userRole.setTenantId(tenantId);
        userRole.setUserId(userId);
        userRole.setRoleId(ROLE_TENANT_ADMIN_ID);
        userRole.setCreateBy(userId);
        userRoleMapper.insert(userRole);
    }

    /**
     * 启用租户。
     *
     * @param id 租户ID
     */
    @Override
    public void enableTenant(Long id) {
        updateTenantStatus(id, SystemConstants.TENANT_STATUS_ENABLED);
    }

    /**
     * 禁用租户。
     *
     * @param id 租户ID
     */
    @Override
    public void disableTenant(Long id) {
        updateTenantStatus(id, SystemConstants.TENANT_STATUS_DISABLED);
    }

    /**
     * 变更租户套餐并同步套餐限制字段。
     *
     * @param id      租户ID
     * @param request 套餐变更请求
     */
    @Override
    public void changePlan(Long id, TenantPlanChangeRequest request) {
        SysTenant tenant = getById(id);
        if (tenant == null) {
            BusinessException.throwException("租户不存在");
        }
        tenant.setPlanType(request.getPlanType());
        tenant.setPlanEndTime(request.getPlanEndTime());
        tenant.setStatus(SystemConstants.TENANT_STATUS_ENABLED);
        applyPlanLimit(tenant, request.getPlanType());
        updateById(tenant);
    }

    private void updateTenantStatus(Long id, Integer status) {
        SysTenant tenant = new SysTenant();
        tenant.setId(id);
        tenant.setStatus(status);
        if (!updateById(tenant)) {
            BusinessException.throwException("租户不存在");
        }
    }

    private void applyPlanLimit(SysTenant tenant, Integer planType) {
        if (Integer.valueOf(2).equals(planType)) {
            tenant.setMaxSupplier(100);
            tenant.setMaxWarehouse(5);
            tenant.setMaxMonthlyOrder(5000);
            tenant.setMaxPlatform(3);
        } else if (Integer.valueOf(3).equals(planType)) {
            tenant.setMaxSupplier(999999);
            tenant.setMaxWarehouse(999999);
            tenant.setMaxMonthlyOrder(999999);
            tenant.setMaxPlatform(999999);
        } else {
            tenant.setMaxSupplier(20);
            tenant.setMaxWarehouse(1);
            tenant.setMaxMonthlyOrder(500);
            tenant.setMaxPlatform(1);
        }
    }

    private String generateTenantCode() {
        return "TC-" + LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                + "-" + ThreadLocalRandom.current().nextInt(1000, 10000);
    }
}
