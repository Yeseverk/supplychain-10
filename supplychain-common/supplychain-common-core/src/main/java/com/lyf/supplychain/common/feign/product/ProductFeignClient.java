package com.lyf.supplychain.common.feign.product;

import com.lyf.supplychain.common.api.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 商品服务 Feign 客户端。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@FeignClient(name = "supplychain-product", path = "/internal/pim")
public interface ProductFeignClient {

    /**
     * 查询 SKU 基础信息。
     *
     * @param skuId SKU ID
     * @return SKU 基础信息
     */
    @GetMapping("/skus/{skuId}")
    R<ProductSkuDTO> getSku(@PathVariable("skuId") Long skuId);
}
