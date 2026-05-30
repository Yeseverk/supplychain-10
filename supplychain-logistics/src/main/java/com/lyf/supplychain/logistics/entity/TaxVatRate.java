package com.lyf.supplychain.logistics.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * VAT 税率配置实体。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
@TableName("tax_vat_rate")
public class TaxVatRate {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String countryCode;
    private String countryName;
    private BigDecimal vatRate;
    private BigDecimal thresholdValue;
    private String currency;
    private LocalDate effectiveDate;
    private LocalDate expireDate;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
