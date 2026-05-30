package com.lyf.supplychain.common.security.datascope;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lyf.supplychain.common.security.constant.SecurityConstants;
import com.lyf.supplychain.common.security.context.SecurityContextHolder;
import com.lyf.supplychain.common.security.model.LoginUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 数据权限查询条件辅助工具测试。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
class DataScopeQueryHelperTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clear();
        DataScopeContext.clear();
    }

    @Test
    void shouldNotAppendConditionWhenUserHasAllPermission() {
        LoginUser loginUser = LoginUser.builder()
                .userId(1001L)
                .permissions(List.of(SecurityConstants.ALL_PERMISSION))
                .dataScope(DataScopeQueryHelper.DATA_SCOPE_ALL)
                .build();
        SecurityContextHolder.setLoginUser(loginUser);

        QueryWrapper<Object> wrapper = DataScopeQueryHelper.apply(new QueryWrapper<>(),
                "create_by", "store_id", "warehouse_id", "supplier_id");

        assertThat(wrapper.getSqlSegment()).isBlank();
    }

    @Test
    void shouldAppendStoreScopeCondition() {
        LoginUser loginUser = LoginUser.builder()
                .userId(1001L)
                .dataScope(DataScopeQueryHelper.DATA_SCOPE_SELF)
                .storeIds(List.of(10L, 11L))
                .build();
        SecurityContextHolder.setLoginUser(loginUser);
        DataScopeContext.set(DataScopeResource.STORE);

        QueryWrapper<Object> wrapper = DataScopeQueryHelper.apply(new QueryWrapper<>(),
                "create_by", "store_id", "warehouse_id", "supplier_id");

        assertThat(wrapper.getSqlSegment()).contains("store_id IN");
    }

    @Test
    void shouldFallbackToSelfConditionWhenResourceScopeIsEmpty() {
        LoginUser loginUser = LoginUser.builder()
                .userId(1001L)
                .dataScope(DataScopeQueryHelper.DATA_SCOPE_SELF)
                .build();
        SecurityContextHolder.setLoginUser(loginUser);
        DataScopeContext.set(DataScopeResource.WAREHOUSE);

        QueryWrapper<Object> wrapper = DataScopeQueryHelper.apply(new QueryWrapper<>(),
                "create_by", "store_id", "warehouse_id", "supplier_id");

        assertThat(wrapper.getSqlSegment()).contains("create_by =");
    }
}
