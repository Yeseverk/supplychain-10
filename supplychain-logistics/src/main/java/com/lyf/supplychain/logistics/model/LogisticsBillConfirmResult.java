package com.lyf.supplychain.logistics.model;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 物流账单确认结果。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Data
public class LogisticsBillConfirmResult {

    private String billBatchNo;
    private String carrierCode;
    private Long payableId;
    private BigDecimal payableAmount;
    private String currency;
    private Integer confirmedCount;
}
