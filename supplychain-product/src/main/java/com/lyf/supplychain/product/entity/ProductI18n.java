package com.lyf.supplychain.product.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lyf.supplychain.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品多语言内容实体。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product_i18n")
public class ProductI18n extends BaseEntity {

    private String refType;

    private Long refId;

    private String langCode;

    private String title;

    private String subtitle;

    private String bulletPoints;

    private String description;

    private String keywords;

    private String searchTerms;

    private Integer isAiTranslated;
}
