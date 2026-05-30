package com.lyf.supplychain.product.controller;

import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.common.security.annotation.TenantWriteGuard;
import com.lyf.supplychain.product.entity.ProductSku;
import com.lyf.supplychain.product.request.ProductPriceRequest;
import com.lyf.supplychain.product.request.ProductSkuBatchRequest;
import com.lyf.supplychain.product.request.ProductSkuPageQuery;
import com.lyf.supplychain.product.request.ProductSkuRequest;
import com.lyf.supplychain.product.service.ProductSkuService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * SKU 商品接口。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@RestController
@com.lyf.supplychain.common.security.annotation.RequiresPermission(com.lyf.supplychain.common.security.constant.PermissionCodes.PIM_PRODUCT_MANAGE)
public class ProductSkuController {

    private final ProductSkuService skuService;

    public ProductSkuController(ProductSkuService skuService) {
        this.skuService = skuService;
    }

    /**
     * 分页查询 SKU。
     *
     * @param query 查询参数
     * @return SKU 分页结果
     */
    @GetMapping({"/api/pim/skus", "/pim/skus"})
    public R<PageResult<ProductSku>> page(ProductSkuPageQuery query) {
        return R.ok(skuService.page(query));
    }

    /**
     * 查询 SPU 下 SKU 列表。
     *
     * @param spuId SPU ID
     * @return SKU 列表
     */
    @GetMapping({"/api/pim/spus/{spuId}/skus", "/pim/spus/{spuId}/skus"})
    public R<List<ProductSku>> listBySpu(@PathVariable("spuId") Long spuId) {
        return R.ok(skuService.listBySpu(spuId));
    }

    /**
     * 查询 SKU 详情。
     *
     * @param id SKU ID
     * @return SKU 详情
     */
    @GetMapping({"/api/pim/skus/{id}", "/pim/skus/{id}"})
    public R<ProductSku> detail(@PathVariable("id") Long id) {
        return R.ok(skuService.detail(id));
    }

    /**
     * 批量创建 SKU。
     *
     * @param spuId   SPU ID
     * @param request 批量请求
     * @return 创建数量
     */
    @PostMapping({"/api/pim/spus/{spuId}/skus/batch", "/pim/spus/{spuId}/skus/batch"})
    @TenantWriteGuard(scene = "批量创建SKU")
    public R<Integer> batchCreate(@PathVariable("spuId") Long spuId,
                                  @Valid @RequestBody ProductSkuBatchRequest request) {
        return R.ok(skuService.batchCreate(spuId, request));
    }

    /**
     * 编辑 SKU。
     *
     * @param id      SKU ID
     * @param request SKU 请求
     * @return 无数据响应
     */
    @PutMapping({"/api/pim/skus/{id}", "/pim/skus/{id}"})
    @TenantWriteGuard(scene = "编辑SKU")
    public R<Void> update(@PathVariable("id") Long id, @Valid @RequestBody ProductSkuRequest request) {
        skuService.update(id, request);
        return R.ok();
    }

    /**
     * 保存 SKU 价格。
     *
     * @param id      SKU ID
     * @param request 价格请求
     * @return 无数据响应
     */
    @PutMapping({"/api/pim/skus/{id}/prices", "/pim/skus/{id}/prices"})
    @TenantWriteGuard(scene = "保存SKU价格")
    public R<Void> savePrices(@PathVariable("id") Long id, @Valid @RequestBody ProductPriceRequest request) {
        skuService.savePrices(id, request);
        return R.ok();
    }

    /**
     * SKU 下拉选项。
     *
     * @param keyword 关键词
     * @return SKU 列表
     */
    @GetMapping({"/api/pim/skus/options", "/pim/skus/options"})
    public R<List<ProductSku>> options(@RequestParam(value = "keyword", required = false) String keyword) {
        return R.ok(skuService.options(keyword));
    }
}
