package com.lyf.supplychain.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单操作日志实体。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
@TableName("order_log")
public class OrderLog {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long tenantId;

    private Long orderId;

    private String orderNo;

    private Integer fromStatus;

    private Integer toStatus;

    private String action;

    private Integer operatorType;

    private Long operatorId;

    private String operatorName;

    private String remark;

    private LocalDateTime operateTime;
}
