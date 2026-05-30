package com.lyf.supplychain.logistics.model;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 物流商账单导入行。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Data
public class LogisticsBillImportRow {

    @ExcelProperty("trackingNo")
    private String trackingNo;

    @ExcelProperty("waybillNo")
    private String waybillNo;

    @ExcelProperty("billingWeightG")
    private BigDecimal billingWeightG;

    @ExcelProperty("baseFee")
    private BigDecimal baseFee;

    @ExcelProperty("fuelSurcharge")
    private BigDecimal fuelSurcharge;

    @ExcelProperty("peakSurcharge")
    private BigDecimal peakSurcharge;

    @ExcelProperty("remoteFee")
    private BigDecimal remoteFee;

    @ExcelProperty("otherFee")
    private BigDecimal otherFee;

    @ExcelProperty("actualTotal")
    private BigDecimal actualTotal;

    @ExcelProperty("currency")
    private String currency;
}
