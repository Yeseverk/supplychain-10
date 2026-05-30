package com.lyf.supplychain.product.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * SKU 价格明细请求。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
public class ProductSkuPriceItemRequest {

    @NotNull(message = "价格类型不能为空")
    private Integer priceType;

    private String platform;

    private String countryCode;

    @NotNull(message = "价格不能为空")
    private BigDecimal price;

    private String currency = "USD";

    private Integer minQty = 1;

    private Integer maxQty;

    private LocalDateTime effectiveTime;

    private LocalDateTime expireTime;
}
