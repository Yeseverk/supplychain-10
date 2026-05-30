package com.lyf.supplychain.supplier.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lyf.supplychain.supplier.entity.Supplier;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 供应商主表 Mapper。
 *
 * @author liyunfei
 * @date 2026-05-16
 */
public interface SupplierMapper extends BaseMapper<Supplier> {

    /**
     * 查询指定日期前缀下最大的供应商编码，用于 Redis 不可用时兜底生成。
     *
     * @param tenantId   租户ID
     * @param codePrefix 编码前缀
     * @return 最大供应商编码
     */
    @Select("""
            SELECT supplier_code
            FROM supplier
            WHERE tenant_id = #{tenantId}
              AND supplier_code LIKE CONCAT(#{codePrefix}, '%')
              AND is_deleted = 0
            ORDER BY supplier_code DESC
            LIMIT 1
            """)
    String selectMaxSupplierCode(@Param("tenantId") Long tenantId, @Param("codePrefix") String codePrefix);
}
