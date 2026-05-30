package com.lyf.supplychain.supplier.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lyf.supplychain.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 供应商租户级策略配置实体。
 *
 * @author liyunfei
 * @date 2026-05-18
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("supplier_tenant_config")
public class SupplierTenantConfig extends BaseEntity {

    private String configKey;

    private String configValue;

    private String configDesc;

    private Integer enabled;
}
