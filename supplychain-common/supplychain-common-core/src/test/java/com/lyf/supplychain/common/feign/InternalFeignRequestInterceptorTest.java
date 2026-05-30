package com.lyf.supplychain.common.feign;

import com.lyf.supplychain.common.context.TenantContext;
import com.lyf.supplychain.common.feign.config.InternalFeignRequestInterceptor;
import com.lyf.supplychain.common.security.constant.SecurityConstants;
import feign.RequestTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Feign 内部调用请求头单元测试。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
class InternalFeignRequestInterceptorTest {

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void applyShouldAttachInternalHeaderAndTenantContext() {
        TenantContext.set(101L, 501L);
        RequestTemplate template = new RequestTemplate();
        InternalFeignRequestInterceptor interceptor = new InternalFeignRequestInterceptor();

        interceptor.apply(template);

        assertThat(template.headers().get(SecurityConstants.INTERNAL_REQUEST_HEADER))
                .containsExactly(SecurityConstants.INTERNAL_REQUEST_VALUE);
        assertThat(template.headers().get(SecurityConstants.TENANT_ID_HEADER)).containsExactly("101");
        assertThat(template.headers().get(SecurityConstants.USER_ID_HEADER)).containsExactly("501");
    }
}
