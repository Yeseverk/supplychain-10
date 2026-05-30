package com.lyf.supplychain.product.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lyf.supplychain.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 分类属性模板实体。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product_attr_template")
public class ProductAttrTemplate extends BaseEntity {

    private Long categoryId;

    private String attrName;

    private String attrNameEn;

    private Integer attrType;

    private String attrOptions;

    private String attrUnit;

    private Integer isSkuSpec;

    private Integer isRequired;

    private Integer sortOrder;

    private Integer status;
}
