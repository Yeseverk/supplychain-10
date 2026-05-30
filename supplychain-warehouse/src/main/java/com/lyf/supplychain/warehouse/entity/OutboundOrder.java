package com.lyf.supplychain.warehouse.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lyf.supplychain.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 出库单主表实体。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("outbound_order")
public class OutboundOrder extends BaseEntity {

    private String outboundNo;
    private Integer outboundType;
    private Long warehouseId;
    private String warehouseName;
    private String refType;
    private Long refId;
    private String refNo;
    private LocalDate planDate;
    private LocalDate actualDate;
    private Integer status;
    private Long pickUserId;
    private LocalDateTime pickStartTime;
    private LocalDateTime pickEndTime;
    private Integer totalQty;
    private String remark;
}
