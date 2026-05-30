package com.lyf.supplychain.supplier.constant;

/**
 * 供应商 Redis Key 常量。
 *
 * @author liyunfei
 * @date 2026-05-16
 */
public final class SupplierRedisKeys {

    private static final String SUPPLIER_CODE_PREFIX = "supplychain:supplier:code:";

    private SupplierRedisKeys() {
    }

    /**
     * 供应商编码每日自增序列。
     *
     * @param tenantId 租户ID
     * @param dateText 日期文本，格式 yyyyMMdd
     * @return Redis Key
     */
    public static String supplierCode(Long tenantId, String dateText) {
        return SUPPLIER_CODE_PREFIX + tenantId + ":" + dateText;
    }
}
