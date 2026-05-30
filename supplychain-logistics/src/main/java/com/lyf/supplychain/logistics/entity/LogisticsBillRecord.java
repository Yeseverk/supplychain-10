package com.lyf.supplychain.logistics.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 物流商账单导入记录实体。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Data
@TableName("logistics_bill_record")
public class LogisticsBillRecord {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long tenantId;
    private String billBatchNo;
    private String carrierCode;
    private Long waybillId;
    private Long payableId;
    private String waybillNo;
    private String trackingNo;
    private BigDecimal billingWeightG;
    private BigDecimal baseFee;
    private BigDecimal fuelSurcharge;
    private BigDecimal peakSurcharge;
    private BigDecimal remoteFee;
    private BigDecimal otherFee;
    private BigDecimal actualTotal;
    private String currency;
    private BigDecimal diffAmount;
    private BigDecimal diffRate;
    private Integer reconcileStatus;
    private LocalDateTime confirmTime;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
