package com.lyf.supplychain.common.feign.config;

import com.lyf.supplychain.common.context.TenantContext;
import com.lyf.supplychain.common.security.constant.SecurityConstants;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;

/**
 * Feign 内部调用请求头拦截器。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Component
public class InternalFeignRequestInterceptor implements RequestInterceptor {

    /**
     * 为所有 Feign 请求追加内部调用标识和当前上下文。
     *
     * @param template Feign 请求模板
     */
    @Override
    public void apply(RequestTemplate template) {
        template.header(SecurityConstants.INTERNAL_REQUEST_HEADER, SecurityConstants.INTERNAL_REQUEST_VALUE);
        Long tenantId = TenantContext.getTenantId();
        if (tenantId != null) {
            template.header(SecurityConstants.TENANT_ID_HEADER, String.valueOf(tenantId));
        }
        Long userId = TenantContext.getUserId();
        if (userId != null) {
            template.header(SecurityConstants.USER_ID_HEADER, String.valueOf(userId));
        }
    }
}
