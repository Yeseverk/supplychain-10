package com.lyf.supplychain.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 平台订单原始报文实体。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
@TableName("order_platform_raw")
public class OrderPlatformRaw {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long tenantId;

    private Long orderId;

    private String platform;

    private String platformOrderNo;

    private String rawData;

    private LocalDateTime syncTime;

    private Integer syncType;
}
