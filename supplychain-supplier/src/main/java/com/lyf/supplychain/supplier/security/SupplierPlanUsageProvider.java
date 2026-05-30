package com.lyf.supplychain.supplier.security;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lyf.supplychain.common.security.plan.PlanUsageProvider;
import com.lyf.supplychain.supplier.entity.Supplier;
import com.lyf.supplychain.supplier.mapper.SupplierMapper;
import org.springframework.stereotype.Component;

/**
 * 供应商套餐用量提供者。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Component
public class SupplierPlanUsageProvider implements PlanUsageProvider {

    private static final String SUPPLIER_MAX_FEATURE = "supplier.max";

    private final SupplierMapper supplierMapper;

    public SupplierPlanUsageProvider(SupplierMapper supplierMapper) {
        this.supplierMapper = supplierMapper;
    }

    /**
     * 判断是否支持供应商数量限制。
     *
     * @param featureCode 功能编码
     * @return 是否支持
     */
    @Override
    public boolean supports(String featureCode) {
        return SUPPLIER_MAX_FEATURE.equals(featureCode);
    }

    /**
     * 查询当前租户已有供应商数量。
     *
     * @param tenantId    租户ID
     * @param featureCode 功能编码
     * @return 已使用数量
     */
    @Override
    public Integer currentUsage(Long tenantId, String featureCode) {
        Long count = supplierMapper.selectCount(new QueryWrapper<Supplier>()
                .eq("tenant_id", tenantId)
                .eq("is_deleted", 0));
        return count == null ? 0 : Math.toIntExact(count);
    }
}
