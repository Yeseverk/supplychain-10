package com.lyf.supplychain.common.feign.product;

import lombok.Data;

/**
 * 商品 SKU 基础信息。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Data
public class ProductSkuDTO {

    private Long skuId;

    private String skuCode;

    private String skuName;

    private String abcClass;
}
