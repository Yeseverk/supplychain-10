package com.lyf.supplychain.product.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * SKU 价格保存请求。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
public class ProductPriceRequest {

    @NotEmpty(message = "价格列表不能为空")
    private List<ProductSkuPriceItemRequest> prices;
}
