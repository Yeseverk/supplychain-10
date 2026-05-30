package com.lyf.supplychain.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lyf.supplychain.common.context.TenantContext;
import com.lyf.supplychain.common.exception.BusinessException;
import com.lyf.supplychain.common.security.model.LoginUser;
import com.lyf.supplychain.system.constant.SystemConstants;
import com.lyf.supplychain.system.entity.SysTenant;
import com.lyf.supplychain.system.entity.SysUser;
import com.lyf.supplychain.system.mapper.SysTenantMapper;
import com.lyf.supplychain.system.mapper.SysUserMapper;
import com.lyf.supplychain.system.model.auth.AuthLoginRequest;
import com.lyf.supplychain.system.model.auth.AuthLoginVO;
import com.lyf.supplychain.system.model.auth.AuthUserProfileVO;
import com.lyf.supplychain.system.service.AuthService;
import com.lyf.supplychain.system.service.AuthTokenService;
import com.lyf.supplychain.system.service.RbacPermissionService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 登录认证应用服务实现。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper userMapper;

    private final SysTenantMapper tenantMapper;

    private final RbacPermissionService rbacPermissionService;

    private final AuthTokenService authTokenService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthServiceImpl(SysUserMapper userMapper,
                           SysTenantMapper tenantMapper,
                           RbacPermissionService rbacPermissionService,
                           AuthTokenService authTokenService) {
        this.userMapper = userMapper;
        this.tenantMapper = tenantMapper;
        this.rbacPermissionService = rbacPermissionService;
        this.authTokenService = authTokenService;
    }

    /**
     * 校验租户、账号、密码并签发访问令牌。
     *
     * @param request 登录请求
     * @param clientIp 客户端IP
     * @return 登录响应
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuthLoginVO login(AuthLoginRequest request, String clientIp) {
        SysTenant tenant = findTenant(request.getTenantCode());
        validateTenant(tenant);
        TenantContext.set(tenant.getId(), null);
        try {
            SysUser user = findUser(tenant.getId(), request.getUsername());
            validateUser(user);
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                handlePasswordFailure(user);
            }
            List<String> permissions = rbacPermissionService.listPermissions(tenant.getId(), user.getId());
            List<String> roles = rbacPermissionService.listRoles(tenant.getId(), user.getId());
            Integer dataScope = rbacPermissionService.dataScope(tenant.getId(), user.getId());
            LoginUser loginUser = buildLoginUser(tenant, user, roles, permissions, dataScope);
            String tokenValue = authTokenService.login(loginUser);
            String refreshToken = authTokenService.issueRefreshToken(loginUser);
            markLoginSuccess(user, clientIp);
            return buildLoginVO(tenant, user, roles, permissions, tokenValue, refreshToken);
        } finally {
            TenantContext.clear();
        }
    }

    /**
     * 使用刷新令牌重新签发访问令牌，并轮换刷新令牌。
     *
     * @param refreshToken 刷新令牌
     * @return 登录响应
     */
    @Override
    public AuthLoginVO refresh(String refreshToken) {
        if (StrUtil.isBlank(refreshToken)) {
            BusinessException.throwException("刷新令牌不能为空");
        }
        Long userId = authTokenService.resolveRefreshUserId(refreshToken);
        if (userId == null) {
            BusinessException.throwException("刷新令牌无效或已过期");
        }
        SysUser user = findUserById(userId);
        validateUser(user);
        SysTenant tenant = findTenantById(user.getTenantId());
        validateTenant(tenant);
        List<String> permissions = rbacPermissionService.listPermissions(tenant.getId(), user.getId());
        List<String> roles = rbacPermissionService.listRoles(tenant.getId(), user.getId());
        Integer dataScope = rbacPermissionService.dataScope(tenant.getId(), user.getId());
        LoginUser loginUser = buildLoginUser(tenant, user, roles, permissions, dataScope);
        String tokenValue = authTokenService.login(loginUser);
        authTokenService.revokeRefreshToken(refreshToken);
        String newRefreshToken = authTokenService.issueRefreshToken(loginUser);
        return buildLoginVO(tenant, user, roles, permissions, tokenValue, newRefreshToken);
    }

    /**
     * 退出当前登录会话。
     */
    @Override
    public void logout(String refreshToken) {
        authTokenService.logout();
        authTokenService.revokeRefreshToken(refreshToken);
    }

    /**
     * 查询当前登录用户资料。
     *
     * @return 当前用户资料
     */
    @Override
    public AuthUserProfileVO profile() {
        LoginUser loginUser = requireLoginUser();
        return AuthUserProfileVO.builder()
                .userId(loginUser.getUserId())
                .tenantId(loginUser.getTenantId())
                .username(loginUser.getUsername())
                .realName(loginUser.getRealName())
                .planType(loginUser.getPlanType())
                .roles(loginUser.getRoles())
                .permissions(loginUser.getPermissions())
                .build();
    }

    /**
     * 查询当前登录用户权限。
     *
     * @return 权限标识集合
     */
    @Override
    public List<String> permissions() {
        return requireLoginUser().getPermissions();
    }

    private SysTenant findTenant(String tenantCode) {
        SysTenant tenant = tenantMapper.selectOne(new LambdaQueryWrapper<SysTenant>()
                .eq(SysTenant::getTenantCode, tenantCode)
                .eq(SysTenant::getIsDeleted, 0)
                .last("LIMIT 1"));
        if (ObjectUtil.isNull(tenant)) {
            BusinessException.throwException("租户不存在");
        }
        return tenant;
    }

    private SysTenant findTenantById(Long tenantId) {
        SysTenant tenant = tenantMapper.selectById(tenantId);
        if (ObjectUtil.isNull(tenant) || Integer.valueOf(1).equals(tenant.getIsDeleted())) {
            BusinessException.throwException("租户不存在");
        }
        return tenant;
    }

    private void validateTenant(SysTenant tenant) {
        if (SystemConstants.TENANT_STATUS_DISABLED == tenant.getStatus()
                || SystemConstants.TENANT_STATUS_CANCELLED == tenant.getStatus()) {
            BusinessException.throwException("租户已被禁用或注销");
        }
    }

    private SysUser findUser(Long tenantId, String username) {
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getTenantId, tenantId)
                .eq(SysUser::getUsername, username)
                .eq(SysUser::getIsDeleted, 0)
                .last("LIMIT 1"));
        if (ObjectUtil.isNull(user)) {
            BusinessException.throwException("用户名或密码错误");
        }
        return user;
    }

    private SysUser findUserById(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (ObjectUtil.isNull(user) || Integer.valueOf(1).equals(user.getIsDeleted())) {
            BusinessException.throwException("用户不存在");
        }
        return user;
    }

    private void validateUser(SysUser user) {
        if (SystemConstants.STATUS_DISABLED == user.getStatus()) {
            BusinessException.throwException("账号已禁用");
        }
        if (SystemConstants.STATUS_LOCKED == user.getStatus()) {
            if (user.getLockUntilTime() != null && user.getLockUntilTime().isAfter(LocalDateTime.now())) {
                BusinessException.throwException("账号已锁定，请在" + SystemConstants.LOGIN_LOCK_MINUTES + "分钟后重试");
            }
            unlockExpiredUser(user);
        }
    }

    private void handlePasswordFailure(SysUser user) {
        int failCount = ObjectUtil.defaultIfNull(user.getLoginFailCount(), 0) + 1;
        SysUser update = new SysUser();
        update.setId(user.getId());
        update.setLoginFailCount(failCount);
        if (failCount >= SystemConstants.MAX_LOGIN_FAIL_COUNT) {
            LocalDateTime now = LocalDateTime.now();
            update.setStatus(SystemConstants.STATUS_LOCKED);
            update.setLockTime(now);
            update.setLockUntilTime(now.plusMinutes(SystemConstants.LOGIN_LOCK_MINUTES));
            userMapper.updateById(update);
            BusinessException.throwException("密码错误次数过多，账号已锁定" + SystemConstants.LOGIN_LOCK_MINUTES + "分钟");
        }
        userMapper.updateById(update);
        int leftCount = Math.max(SystemConstants.MAX_LOGIN_FAIL_COUNT - failCount, 0);
        BusinessException.throwException("密码错误，剩余" + leftCount + "次机会");
    }

    private void unlockExpiredUser(SysUser user) {
        SysUser update = new SysUser();
        update.setId(user.getId());
        update.setStatus(SystemConstants.STATUS_ENABLED);
        update.setLoginFailCount(0);
        update.setLockTime(null);
        update.setLockUntilTime(null);
        userMapper.updateById(update);
        user.setStatus(SystemConstants.STATUS_ENABLED);
        user.setLoginFailCount(0);
        user.setLockTime(null);
        user.setLockUntilTime(null);
    }

    private LoginUser buildLoginUser(SysTenant tenant, SysUser user, List<String> roles, List<String> permissions, Integer dataScope) {
        return LoginUser.builder()
                .userId(user.getId())
                .tenantId(tenant.getId())
                .tenantCode(tenant.getTenantCode())
                .username(user.getUsername())
                .realName(user.getRealName())
                .userType(user.getUserType())
                .planType(tenant.getPlanType())
                .dataScope(dataScope)
                .roles(CollUtil.emptyIfNull(roles))
                .permissions(CollUtil.emptyIfNull(permissions))
                .build();
    }

    private AuthLoginVO buildLoginVO(SysTenant tenant,
                                     SysUser user,
                                     List<String> roles,
                                     List<String> permissions,
                                     String tokenValue,
                                     String refreshToken) {
        return AuthLoginVO.builder()
                .tokenName(authTokenService.getTokenName())
                .tokenValue(tokenValue)
                .refreshToken(refreshToken)
                .refreshTokenExpireSeconds(604800L)
                .userId(user.getId())
                .tenantId(tenant.getId())
                .tenantCode(tenant.getTenantCode())
                .username(user.getUsername())
                .realName(user.getRealName())
                .planType(tenant.getPlanType())
                .roles(roles)
                .permissions(permissions)
                .build();
    }

    private void markLoginSuccess(SysUser user, String clientIp) {
        SysUser update = new SysUser();
        update.setId(user.getId());
        update.setLoginFailCount(0);
        update.setLockTime(null);
        update.setLockUntilTime(null);
        update.setLastLoginIp(clientIp);
        update.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(update);
    }

    private LoginUser requireLoginUser() {
        LoginUser loginUser = authTokenService.getLoginUser();
        if (ObjectUtil.isNull(loginUser)) {
            BusinessException.throwException("请先登录");
        }
        return loginUser;
    }
}
