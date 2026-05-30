package com.lyf.supplychain.common.feign.system;

import lombok.Data;

/**
 * 租户写操作检查请求。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Data
public class SystemTenantWriteCheckRequest {

    private Long tenantId;

    private String scene;
}
