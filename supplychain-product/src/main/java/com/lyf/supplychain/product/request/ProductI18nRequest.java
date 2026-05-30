package com.lyf.supplychain.product.request;

import lombok.Data;

/**
 * 商品多语言保存请求。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
public class ProductI18nRequest {

    private String title;

    private String subtitle;

    private String bulletPoints;

    private String description;

    private String keywords;

    private String searchTerms;
}
