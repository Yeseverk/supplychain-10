package com.lyf.supplychain.warehouse.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lyf.supplychain.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库存主表实体。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("inventory")
public class Inventory extends BaseEntity {

    private Long warehouseId;
    private Long locationId;
    private Long skuId;
    private String skuCode;
    private String skuName;
    private Integer quantity;
    private Integer frozenQty;
    private Integer inTransitQty;
    private Integer defectiveQty;
    private Integer reservedQty;
    private Integer safetyStock;
    private Integer maxStock;
    private Integer reorderPoint;
    private BigDecimal avgCost;
    private BigDecimal totalCost;
    private LocalDateTime lastInboundTime;
    private LocalDateTime lastOutboundTime;

    @TableField(exist = false)
    private Integer availableQty;
}
