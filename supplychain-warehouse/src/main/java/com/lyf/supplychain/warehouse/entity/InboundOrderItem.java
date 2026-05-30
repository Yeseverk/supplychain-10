package com.lyf.supplychain.warehouse.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 入库单明细实体。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Data
@TableName("inbound_order_item")
public class InboundOrderItem {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long tenantId;
    private Long inboundId;
    private Long skuId;
    private String skuCode;
    private String skuName;
    private Integer expectedQty;
    private Integer actualQty;
    private Integer defectiveQty;
    private Long locationId;
    private String locationCode;
    private BigDecimal unitCost;
    private Integer status;
    private String remark;
}
