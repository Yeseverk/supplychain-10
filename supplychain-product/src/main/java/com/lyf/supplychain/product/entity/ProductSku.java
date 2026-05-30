package com.lyf.supplychain.product.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lyf.supplychain.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * SKU 最小库存单元实体。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product_sku")
public class ProductSku extends BaseEntity {

    private Long spuId;

    private String skuCode;

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

    private Integer isBattery;

    private Integer isLiquid;

    private Integer isPowder;

    private BigDecimal costPrice;

    private String costCurrency;

    private String abcClass;

    private Integer status;

    private String remark;
}
