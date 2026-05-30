package com.lyf.supplychain.product.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * SKU 批量创建请求。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
public class ProductSkuBatchRequest {

    @Valid
    @NotEmpty(message = "SKU列表不能为空")
    private List<ProductSkuRequest> skus;
}
