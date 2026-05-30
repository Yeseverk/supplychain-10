package com.lyf.supplychain.logistics.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lyf.supplychain.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 退货运单实体。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("logistics_return")
public class LogisticsReturn extends BaseEntity {

    private String returnNo;
    private Long originalWaybillId;
    private Long orderId;
    private Long refundId;
    private Integer returnType;
    private Long carrierId;
    private String returnTrackingNo;
    private String fromCountry;
    private Long toWarehouseId;
    private Integer status;
    private LocalDate expectedArriveDate;
    private LocalDate actualArriveDate;
    private String labelUrl;
    private String remark;
}
