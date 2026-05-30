package com.lyf.supplychain.warehouse.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 仓库库存流水实体。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Data
@TableName("warehouse_inventory_log")
public class WarehouseInventoryLog {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long tenantId;
    private Long warehouseId;
    private Long locationId;
    private Long skuId;
    private String skuCode;
    private String skuName;
    private String bizType;
    private String bizNo;
    private Integer changeQty;
    private Integer beforeQty;
    private Integer afterQty;
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
