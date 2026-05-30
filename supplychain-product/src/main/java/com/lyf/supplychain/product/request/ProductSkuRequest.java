package com.lyf.supplychain.product.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

/**
 * SKU 保存请求。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
public class ProductSkuRequest {

    private String skuCode;

    @NotBlank(message = "SKU名称不能为空")
    private String skuName;

    private String barcode;

    private String fnsku;

    private String specValues;

    private String specValuesEn;

    private BigDecimal netWeightG;

    private BigDecimal grossWeightG;

    private BigDecimal lengthMm;

    private BigDecimal widthMm;

    private BigDecimal heightMm;

    private Integer isBattery = 0;

    private Integer isLiquid = 0;

    private Integer isPowder = 0;

    private BigDecimal costPrice;

    private String costCurrency = "CNY";

    private String abcClass = "C";

    private Integer status;

    private String remark;
}
