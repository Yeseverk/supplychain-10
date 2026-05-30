package com.lyf.supplychain.product.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 商品图片保存请求。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
public class ProductImageRequest {

    private Long skuId;

    @NotNull(message = "图片类型不能为空")
    private Integer imageType;

    @NotBlank(message = "图片地址不能为空")
    private String imageUrl;

    private String thumbUrl;

    private Integer imageWidth;

    private Integer imageHeight;

    private Long fileSize;

    private Integer sortOrder = 0;

    private String altText;
}
