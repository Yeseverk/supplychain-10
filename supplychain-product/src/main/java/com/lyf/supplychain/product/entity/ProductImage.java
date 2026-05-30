package com.lyf.supplychain.product.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lyf.supplychain.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品图片实体。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product_image")
public class ProductImage extends BaseEntity {

    private Long spuId;

    private Long skuId;

    private Integer imageType;

    private String imageUrl;

    private String thumbUrl;

    private Integer imageWidth;

    private Integer imageHeight;

    private Long fileSize;

    private Integer sortOrder;

    private String altText;
}
