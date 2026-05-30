package com.lyf.supplychain.warehouse.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库存盘点明细实体。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Data
@TableName("stocktake_item")
public class StocktakeItem {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long tenantId;
    private Long taskId;
    private Long warehouseId;
    private Long locationId;
    private String locationCode;
    private Long skuId;
    private String skuCode;
    private String skuName;
    private Integer bookQty;
    private Integer actualQty;
    private Integer diffQty;
    private BigDecimal diffAmount;
    private String diffReason;
    private Integer isAdjusted;
    private LocalDateTime adjustTime;
    private Long pickerId;
    private LocalDateTime pickTime;
}
