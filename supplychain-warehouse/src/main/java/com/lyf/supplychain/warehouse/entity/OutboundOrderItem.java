package com.lyf.supplychain.warehouse.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 出库单明细实体。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Data
@TableName("outbound_order_item")
public class OutboundOrderItem {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long tenantId;
    private Long outboundId;
    private Long skuId;
    private String skuCode;
    private String skuName;
    private Integer quantity;
    private Integer pickedQty;
    private Long locationId;
    private String locationCode;
    private Integer pickStatus;
    private String remark;
}
