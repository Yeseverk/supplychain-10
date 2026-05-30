package com.lyf.supplychain.warehouse.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 仓库库位实体。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Data
@TableName("warehouse_location")
public class WarehouseLocation {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long tenantId;
    private Long warehouseId;
    private String locationCode;
    private String zone;
    private Integer rowNo;
    private Integer columnNo;
    private Integer floorNo;
    private Integer locationType;
    private BigDecimal maxWeightKg;
    private BigDecimal maxVolumeL;
    private Integer isOccupied;
    private Integer status;
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;
    @TableLogic
    private Integer isDeleted;
}
