package com.lyf.supplychain.common.security.datascope;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lyf.supplychain.common.security.constant.SecurityConstants;
import com.lyf.supplychain.common.security.context.SecurityContextHolder;
import com.lyf.supplychain.common.security.model.LoginUser;

/**
 * 数据权限查询条件辅助工具。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
public final class DataScopeQueryHelper {

    public static final int DATA_SCOPE_ALL = 1;
    public static final int DATA_SCOPE_DEPT = 2;
    public static final int DATA_SCOPE_SELF = 3;

    private DataScopeQueryHelper() {
    }

    /**
     * 根据当前登录用户和数据权限上下文追加查询条件。
     *
     * @param wrapper        查询条件
     * @param selfColumn     本人数据字段
     * @param storeColumn    店铺字段
     * @param warehouseColumn 仓库字段
     * @param supplierColumn 供应商字段
     * @param <T>            实体类型
     * @return 查询条件
     */
    public static <T> QueryWrapper<T> apply(QueryWrapper<T> wrapper,
                                            String selfColumn,
                                            String storeColumn,
                                            String warehouseColumn,
                                            String supplierColumn) {
        LoginUser loginUser = SecurityContextHolder.getLoginUser();
        if (loginUser == null || hasAllPermission(loginUser) || DATA_SCOPE_ALL == defaultScope(loginUser)) {
            return wrapper;
        }
        DataScopeResource resource = DataScopeContext.get();
        if (DataScopeResource.STORE == resource && storeColumn != null && CollUtil.isNotEmpty(loginUser.getStoreIds())) {
            return wrapper.in(storeColumn, loginUser.getStoreIds());
        }
        if (DataScopeResource.WAREHOUSE == resource && warehouseColumn != null && CollUtil.isNotEmpty(loginUser.getWarehouseIds())) {
            return wrapper.in(warehouseColumn, loginUser.getWarehouseIds());
        }
        if (DataScopeResource.SUPPLIER == resource && supplierColumn != null && loginUser.getSupplierId() != null) {
            return wrapper.eq(supplierColumn, loginUser.getSupplierId());
        }
        if (selfColumn != null) {
            return wrapper.eq(selfColumn, loginUser.getUserId());
        }
        return wrapper.apply("1 = 0");
    }

    private static boolean hasAllPermission(LoginUser loginUser) {
        return CollUtil.isNotEmpty(loginUser.getPermissions())
                && loginUser.getPermissions().contains(SecurityConstants.ALL_PERMISSION);
    }

    private static int defaultScope(LoginUser loginUser) {
        if (loginUser.getDataScope() == null) {
            return DATA_SCOPE_SELF;
        }
        if (DATA_SCOPE_DEPT == loginUser.getDataScope()) {
            return DATA_SCOPE_SELF;
        }
        return loginUser.getDataScope();
    }
}
