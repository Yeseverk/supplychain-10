package com.lyf.supplychain.finance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * VAT 申报记录实体。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
@TableName("finance_vat_record")
public class FinanceVatRecord {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long tenantId;
    private String countryCode;
    private String vatNo;
    private String period;
    private BigDecimal taxableAmount;
    private BigDecimal vatRate;
    private BigDecimal vatAmount;
    private String localCurrency;
    private BigDecimal cnyAmount;
    private Integer status;
    private String fileUrl;
    private LocalDateTime declareTime;
    private LocalDateTime payTime;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long createBy;
}
