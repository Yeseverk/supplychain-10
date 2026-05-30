package com.lyf.supplychain.warehouse.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lyf.supplychain.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 入库单主表实体。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("inbound_order")
public class InboundOrder extends BaseEntity {

    private String inboundNo;
    private Integer inboundType;
    private Long warehouseId;
    private String warehouseName;
    private String refType;
    private Long refId;
    private String refNo;
    private LocalDate expectedDate;
    private LocalDate actualDate;
    private Integer status;
    private Integer totalSkuCount;
    private Integer totalQty;
    private Integer actualQty;
    private Long operatorId;
    private String remark;
}
