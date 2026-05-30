package com.lyf.supplychain.purchase.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lyf.supplychain.purchase.entity.PurchaseRequisition;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 采购申请单 Mapper。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
public interface PurchaseRequisitionMapper extends BaseMapper<PurchaseRequisition> {

    /**
     * 统计同仓库同 SKU 进行中的采购申请数量。
     *
     * @param tenantId    租户ID
     * @param warehouseId 仓库ID
     * @param skuId       SKU ID
     * @return 进行中的采购申请数量
     */
    @Select("""
            SELECT COALESCE(SUM(ri.quantity), 0)
            FROM purchase_requisition r
            INNER JOIN purchase_requisition_item ri ON r.id = ri.req_id
            WHERE r.tenant_id = #{tenantId}
              AND r.warehouse_id = #{warehouseId}
              AND ri.sku_id = #{skuId}
              AND r.status IN (1, 2, 4)
              AND r.is_deleted = 0
            """)
    Integer sumOngoingQty(@Param("tenantId") Long tenantId,
                          @Param("warehouseId") Long warehouseId,
                          @Param("skuId") Long skuId);
}
