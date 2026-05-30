package com.lyf.supplychain.common;

import com.lyf.supplychain.common.api.PageQuery;
import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.common.constant.ResultCode;
import com.lyf.supplychain.common.context.TenantContext;
import com.lyf.supplychain.common.exception.BusinessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 公共基础能力测试。
 *
 * @author liyunfei
 * @date 2026-05-15
 */
class CommonInfrastructureTest {

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void successResponseShouldContainBusinessCodeMessageTimestampAndData() {
        R<String> response = R.ok("hello");

        assertThat(response.getCode()).isEqualTo(ResultCode.SUCCESS.getCode());
        assertThat(response.getMsg()).isEqualTo(ResultCode.SUCCESS.getMessage());
        assertThat(response.getData()).isEqualTo("hello");
        assertThat(response.getTimestamp()).isPositive();
    }

    @Test
    void businessExceptionShouldCarryCustomCodeAndMessage() {
        BusinessException exception = new BusinessException(ResultCode.PARAM_ERROR);

        assertThat(exception.getCode()).isEqualTo(ResultCode.PARAM_ERROR.getCode());
        assertThat(exception.getMessage()).isEqualTo(ResultCode.PARAM_ERROR.getMessage());
        assertThatThrownBy(() -> BusinessException.throwException("供应商不存在"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("供应商不存在");
    }

    @Test
    void pageQueryShouldNormalizeInvalidAndOversizedValues() {
        PageQuery query = new PageQuery();
        query.setPageNum(0L);
        query.setPageSize(1000L);

        query.normalize();

        assertThat(query.getPageNum()).isEqualTo(1L);
        assertThat(query.getPageSize()).isEqualTo(100L);
    }

    @Test
    void tenantContextShouldStoreAndClearCurrentThreadValues() {
        TenantContext.set(101L, 501L);

        assertThat(TenantContext.getTenantId()).isEqualTo(101L);
        assertThat(TenantContext.getUserId()).isEqualTo(501L);

        TenantContext.clear();

        assertThat(TenantContext.getTenantId()).isNull();
        assertThat(TenantContext.getUserId()).isNull();
    }
}
