package com.lyf.supplychain.common.security.context;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.lyf.supplychain.common.context.TenantContext;
import com.lyf.supplychain.common.security.constant.SecurityConstants;
import com.lyf.supplychain.common.security.model.LoginUser;

/**
 * 当前线程安全上下文。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
public final class SecurityContextHolder {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    private SecurityContextHolder() {
    }

    /**
     * 写入当前登录用户，并同步写入租户上下文。
     *
     * @param loginUser 登录用户
     */
    public static void setLoginUser(LoginUser loginUser) {
        HOLDER.set(loginUser);
        if (ObjectUtil.isNotNull(loginUser)) {
            TenantContext.set(loginUser.getTenantId(), loginUser.getUserId());
        }
    }

    /**
     * 获取当前登录用户。
     *
     * @return 登录用户
     */
    public static LoginUser getLoginUser() {
        return HOLDER.get();
    }

    /**
     * 获取当前用户ID。
     *
     * @return 用户ID
     */
    public static Long getUserId() {
        LoginUser loginUser = getLoginUser();
        return ObjectUtil.isNull(loginUser) ? null : loginUser.getUserId();
    }

    /**
     * 获取当前租户ID。
     *
     * @return 租户ID
     */
    public static Long getTenantId() {
        LoginUser loginUser = getLoginUser();
        return ObjectUtil.isNull(loginUser) ? null : loginUser.getTenantId();
    }

    /**
     * 判断当前用户是否拥有指定权限。
     *
     * @param permission 权限标识
     * @return 是否拥有权限
     */
    public static boolean hasPermission(String permission) {
        LoginUser loginUser = getLoginUser();
        if (ObjectUtil.isNull(loginUser) || CollUtil.isEmpty(loginUser.getPermissions())) {
            return false;
        }
        return loginUser.getPermissions().contains(SecurityConstants.ALL_PERMISSION)
                || loginUser.getPermissions().contains(permission);
    }

    /**
     * 清理当前线程安全上下文和租户上下文。
     */
    public static void clear() {
        HOLDER.remove();
        TenantContext.clear();
    }
}
