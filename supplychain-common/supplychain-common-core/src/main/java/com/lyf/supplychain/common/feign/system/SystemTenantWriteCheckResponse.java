package com.lyf.supplychain.common.feign.system;

import lombok.Data;

/**
 * 租户写操作检查结果。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Data
public class SystemTenantWriteCheckResponse {

    private Long tenantId;

    private Boolean canWrite;

    private String reason;
}
