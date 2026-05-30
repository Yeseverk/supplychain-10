package com.lyf.supplychain.product.controller;

import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.common.feign.product.ProductSkuDTO;
import com.lyf.supplychain.product.entity.ProductSku;
import com.lyf.supplychain.product.mapper.ProductSkuMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商品内部边界接口。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@RestController
@RequestMapping("/internal/pim")
public class ProductInternalController {

    private final ProductSkuMapper skuMapper;

    public ProductInternalController(ProductSkuMapper skuMapper) {
        this.skuMapper = skuMapper;
    }

    /**
     * 查询 SKU 基础信息。
     *
     * @param skuId SKU ID
     * @return SKU 基础信息
     */
    @GetMapping("/skus/{skuId}")
    public R<ProductSkuDTO> getSku(@PathVariable("skuId") Long skuId) {
        ProductSku productSku = skuMapper.selectById(skuId);
        ProductSkuDTO sku = new ProductSkuDTO();
        sku.setSkuId(skuId);
        sku.setSkuCode(productSku == null ? "SKU-" + skuId : productSku.getSkuCode());
        sku.setSkuName(productSku == null ? "SKU-" + skuId : productSku.getSkuName());
        sku.setAbcClass(productSku == null ? "C" : productSku.getAbcClass());
        return R.ok(sku);
    }
}
