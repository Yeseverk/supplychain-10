package com.lyf.supplychain.warehouse.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 库存预警事件实体。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Data
@TableName("inventory_warning_event")
public class InventoryWarningEvent {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long tenantId;
    private Long warehouseId;
    private Long skuId;
    private String skuCode;
    private Integer warningLevel;
    private Integer availableQty;
    private Integer safetyStock;
    private Integer status;
    private LocalDateTime firstDetectedTime;
    private LocalDateTime lastDetectedTime;
    private LocalDateTime resolvedTime;
}
