package com.lyf.supplychain.warehouse.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 库存流水实体，只允许新增和查询。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Data
@TableName("inventory_log")
public class InventoryLog {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long tenantId;
    private Integer logType;
    private Long warehouseId;
    private Long locationId;
    private Long skuId;
    private String skuCode;
    private String skuName;
    private Integer changeQty;
    private Integer beforeQty;
    private Integer afterQty;
    private String refType;
    private String refNo;
    private Long refId;
    private String batchNo;
    private Long operatorId;
    private String operatorName;
    private LocalDateTime operateTime;
    private String remark;
}
