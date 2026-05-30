package com.lyf.supplychain.supplier.mapper;

import com.lyf.supplychain.supplier.model.SupplierPerformanceMetrics;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

/**
 * 供应商绩效评分原始数据 Mapper。
 *
 * @author liyunfei
 * @date 2026-05-18
 */
@Mapper
public interface SupplierPerformanceDataMapper {

    /**
     * 查询供应商月度采购到货指标。
     *
     * @param tenantId   租户ID
     * @param supplierId 供应商ID
     * @param scoreMonth 评分月份
     * @return 采购到货指标
     */
    @Select("""
            SELECT COUNT(1) AS total_orders,
                   COALESCE(SUM(CASE WHEN actual_arrival_date <= promised_arrival_date THEN 1 ELSE 0 END), 0) AS delivered_on_time
            FROM supplier_purchase_arrival
            WHERE tenant_id = #{tenantId}
              AND supplier_id = #{supplierId}
              AND score_month = #{scoreMonth}
              AND is_deleted = 0
            """)
    SupplierPerformanceMetrics selectDeliveryMetrics(@Param("tenantId") Long tenantId,
                                                     @Param("supplierId") Long supplierId,
                                                     @Param("scoreMonth") String scoreMonth);

    /**
     * 查询供应商月度质检指标。
     *
     * @param tenantId   租户ID
     * @param supplierId 供应商ID
     * @param scoreMonth 评分月份
     * @return 质检指标
     */
    @Select("""
            SELECT COALESCE(SUM(CASE WHEN inspection_result = 1 THEN 1 ELSE 0 END), 0) AS quality_passed,
                   COUNT(1) AS quality_total
            FROM supplier_quality_inspection
            WHERE tenant_id = #{tenantId}
              AND supplier_id = #{supplierId}
              AND score_month = #{scoreMonth}
              AND is_deleted = 0
            """)
    SupplierPerformanceMetrics selectQualityMetrics(@Param("tenantId") Long tenantId,
                                                    @Param("supplierId") Long supplierId,
                                                    @Param("scoreMonth") String scoreMonth);

    /**
     * 查询供应商月度平均询价响应时长。
     *
     * @param tenantId   租户ID
     * @param supplierId 供应商ID
     * @param scoreMonth 评分月份
     * @return 平均响应小时数
     */
    @Select("""
            SELECT AVG(TIMESTAMPDIFF(MINUTE, inquiry_time, quote_time)) / 60
            FROM supplier_quote_response
            WHERE tenant_id = #{tenantId}
              AND supplier_id = #{supplierId}
              AND score_month = #{scoreMonth}
              AND quote_time IS NOT NULL
              AND is_deleted = 0
            """)
    BigDecimal selectResponseHoursAvg(@Param("tenantId") Long tenantId,
                                      @Param("supplierId") Long supplierId,
                                      @Param("scoreMonth") String scoreMonth);

    /**
     * 查询供应商月度价格竞争力系数。
     *
     * @param tenantId   租户ID
     * @param supplierId 供应商ID
     * @param scoreMonth 评分月份
     * @return 价格竞争力系数
     */
    @Select("""
            SELECT AVG(supplier_category.avg_price / NULLIF(market_category.avg_price, 0))
            FROM (
                SELECT category_id, AVG(unit_price) AS avg_price
                FROM supplier_purchase_price
                WHERE tenant_id = #{tenantId}
                  AND supplier_id = #{supplierId}
                  AND score_month = #{scoreMonth}
                  AND is_deleted = 0
                GROUP BY category_id
            ) supplier_category
            INNER JOIN (
                SELECT category_id, AVG(unit_price) AS avg_price
                FROM supplier_purchase_price
                WHERE tenant_id = #{tenantId}
                  AND score_month = #{scoreMonth}
                  AND is_deleted = 0
                GROUP BY category_id
            ) market_category ON market_category.category_id = supplier_category.category_id
            """)
    BigDecimal selectPriceComparison(@Param("tenantId") Long tenantId,
                                     @Param("supplierId") Long supplierId,
                                     @Param("scoreMonth") String scoreMonth);
}
