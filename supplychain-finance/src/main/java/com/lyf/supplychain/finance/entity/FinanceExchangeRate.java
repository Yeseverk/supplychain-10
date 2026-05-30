package com.lyf.supplychain.finance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 汇率实体。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
@TableName("finance_exchange_rate")
public class FinanceExchangeRate {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private LocalDate rateDate;
    private String currency;
    private BigDecimal rateToCny;
    private String rateSource;
    private Integer isOfficial;
    private LocalDateTime createTime;
}
