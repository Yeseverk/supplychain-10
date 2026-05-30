package com.lyf.supplychain.common.security.plan;

/**
 * 套餐用量提供者。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
public interface PlanUsageProvider {

    /**
     * 判断当前提供者是否支持该功能编码。
     *
     * @param featureCode 功能编码
     * @return 是否支持
     */
    boolean supports(String featureCode);

    /**
     * 查询当前租户在该功能上的已使用数量。
     *
     * @param tenantId    租户ID
     * @param featureCode 功能编码
     * @return 已使用数量
     */
    Integer currentUsage(Long tenantId, String featureCode);
}
