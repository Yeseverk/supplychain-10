package com.lyf.supplychain.purchase.mapper;

import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 采购报表 Mapper。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
public interface PurchaseReportMapper {

    /**
     * 查询采购总览指标。
     *
     * @return 总览指标
     */
    @Select("""
            SELECT
              COALESCE(COUNT(*), 0) AS orderCount,
              COALESCE(SUM(total_amount), 0) AS purchaseAmount,
              COALESCE(SUM(CASE WHEN status IN (1, 2, 3, 4) THEN 1 ELSE 0 END), 0) AS processingOrderCount,
              COALESCE(SUM(CASE WHEN status = 5 THEN 1 ELSE 0 END), 0) AS receivedOrderCount
            FROM purchase_order
            WHERE is_deleted = 0
            """)
    Map<String, Object> overview();

    /**
     * 查询供应商采购金额排行。
     *
     * @return 供应商采购金额排行
     */
    @Select("""
            SELECT
              supplier_id AS supplierId,
              supplier_name AS supplierName,
              COUNT(*) AS orderCount,
              COALESCE(SUM(total_amount), 0) AS purchaseAmount
            FROM purchase_order
            WHERE is_deleted = 0
            GROUP BY supplier_id, supplier_name
            ORDER BY purchaseAmount DESC
            LIMIT 10
            """)
    List<Map<String, Object>> supplierRank();

    /**
     * 查询采购月度趋势。
     *
     * @return 采购月度趋势
     */
    @Select("""
            SELECT
              DATE_FORMAT(order_date, '%Y-%m') AS month,
              COUNT(*) AS orderCount,
              COALESCE(SUM(total_amount), 0) AS purchaseAmount
            FROM purchase_order
            WHERE is_deleted = 0
              AND order_date >= DATE_SUB(CURDATE(), INTERVAL 12 MONTH)
            GROUP BY DATE_FORMAT(order_date, '%Y-%m')
            ORDER BY month ASC
            """)
    List<Map<String, Object>> monthlyTrend();
}
