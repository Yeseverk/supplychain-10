package com.lyf.supplychain.product.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lyf.supplychain.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * SKU 多平台价格实体。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product_sku_price")
public class ProductSkuPrice extends BaseEntity {

    private Long skuId;

    private Integer priceType;

    private String platform;

    private String countryCode;

    private BigDecimal price;

    private String currency;

    private Integer minQty;

    private Integer maxQty;

    private LocalDateTime effectiveTime;

    private LocalDateTime expireTime;

    private Integer isActive;
}
