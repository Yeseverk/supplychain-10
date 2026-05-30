package com.lyf.supplychain.warehouse.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 仓库库存实体。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Data
@TableName("warehouse_inventory")
public class WarehouseInventory {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long tenantId;
    private Long warehouseId;
    private Long locationId;
    private Long skuId;
    private String skuCode;
    private String skuName;
    private Integer quantity;
    private Integer lockedQuantity;
    @TableField(exist = false)
    private Integer availableQuantity;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer isDeleted;
    @Version
    private Integer version;
}
