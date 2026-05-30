package com.lyf.supplychain.purchase.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lyf.supplychain.purchase.entity.PurchaseOrder;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 采购订单 Mapper。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
public interface PurchaseOrderMapper extends BaseMapper<PurchaseOrder> {

    /**
     * 统计同仓库同 SKU 进行中的采购订单未收货数量。
     *
     * @param tenantId    租户ID
     * @param warehouseId 仓库ID
     * @param skuId       SKU ID
     * @return 进行中的采购订单未收货数量
     */
    @Select("""
            SELECT COALESCE(SUM(oi.quantity - oi.received_qty), 0)
            FROM purchase_order o
            INNER JOIN purchase_order_item oi ON o.id = oi.po_id
            WHERE o.tenant_id = #{tenantId}
              AND o.warehouse_id = #{warehouseId}
              AND oi.sku_id = #{skuId}
              AND o.status IN (1, 2, 3, 4)
              AND o.is_deleted = 0
            """)
    Integer sumOngoingQty(@Param("tenantId") Long tenantId,
                          @Param("warehouseId") Long warehouseId,
                          @Param("skuId") Long skuId);
}
