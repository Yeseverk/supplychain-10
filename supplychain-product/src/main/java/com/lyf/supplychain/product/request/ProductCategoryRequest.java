package com.lyf.supplychain.product.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 商品分类请求。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
public class ProductCategoryRequest {

    private Long parentId = 0L;

    @NotBlank(message = "分类名称不能为空")
    private String categoryName;

    private String categoryNameEn;

    private String iconUrl;

    private Integer sortOrder = 0;
}
