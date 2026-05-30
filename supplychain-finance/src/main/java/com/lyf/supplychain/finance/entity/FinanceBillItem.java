package com.lyf.supplychain.finance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 平台账单明细实体。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
@TableName("finance_bill_item")
public class FinanceBillItem {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long tenantId;
    private Long billId;
    private String itemType;
    private String orderNo;
    private Long skuId;
    private String platformSku;
    private BigDecimal amount;
    private String currency;
    private String description;
    private LocalDate transactionDate;
    private Integer isMatched;
    private Long matchOrderId;
}
