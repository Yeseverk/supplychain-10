package com.lyf.supplychain.finance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 资金流水实体。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
@TableName("finance_cash_flow")
public class FinanceCashFlow {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long tenantId;
    private LocalDate flowDate;
    private Integer flowType;
    private String sourceType;
    private Long sourceId;
    private String sourceNo;
    private BigDecimal amountCny;
    private BigDecimal amountOrigin;
    private String currency;
    private BigDecimal exchangeRate;
    private String remark;
    private LocalDateTime createTime;
    private Long createBy;
}
