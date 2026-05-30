package com.lyf.supplychain.finance.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 付款记录实体。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Data
@TableName("finance_payment_record")
public class FinancePaymentRecord {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long tenantId;
    private Long payableId;
    private BigDecimal paymentAmount;
    private LocalDate paymentDate;
    private Integer paymentMethod;
    private String voucherNo;
    private Long operatorId;
    private String operatorName;
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
