package com.lyf.supplychain.warehouse.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 仓库调拨单明细实体。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Data
@TableName("transfer_order_item")
public class TransferOrderItem {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long tenantId;
    private Long transferId;
    private Long skuId;
    private String skuCode;
    private String skuName;
    private Integer transferQty;
    private Integer shippedQty;
    private Integer receivedQty;
    private Long fromLocationId;
    private Long toLocationId;
}
