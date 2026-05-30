package com.lyf.supplychain.warehouse.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lyf.supplychain.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 库存盘点任务实体。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("stocktake_task")
public class StocktakeTask extends BaseEntity {

    private String taskNo;
    private Integer taskType;
    private Long warehouseId;
    private String taskName;
    private LocalDate planDate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
    private Integer totalSkuCount;
    private Integer diffSkuCount;
    private Integer profitQty;
    private Integer lossQty;
    private BigDecimal profitAmount;
    private BigDecimal lossAmount;
    private Long auditorId;
    private LocalDateTime auditTime;
    private String remark;
}
