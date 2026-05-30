package com.lyf.supplychain.product.controller;

import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.common.security.annotation.TenantWriteGuard;
import com.lyf.supplychain.product.entity.ProductI18n;
import com.lyf.supplychain.product.entity.ProductSpu;
import com.lyf.supplychain.product.request.ProductI18nRequest;
import com.lyf.supplychain.product.request.ProductImageRequest;
import com.lyf.supplychain.product.request.ProductSpuPageQuery;
import com.lyf.supplychain.product.request.ProductSpuRequest;
import com.lyf.supplychain.product.request.ProductTranslateRequest;
import com.lyf.supplychain.product.service.ProductSpuService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * SPU 商品接口。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@RestController
@RequestMapping({"/api/pim/spus", "/pim/spus"})
@com.lyf.supplychain.common.security.annotation.RequiresPermission(com.lyf.supplychain.common.security.constant.PermissionCodes.PIM_PRODUCT_MANAGE)
public class ProductSpuController {

    private final ProductSpuService spuService;

    public ProductSpuController(ProductSpuService spuService) {
        this.spuService = spuService;
    }

    /**
     * 分页查询 SPU。
     *
     * @param query 分页参数
     * @return SPU 分页结果
     */
    @GetMapping
    public R<PageResult<ProductSpu>> page(ProductSpuPageQuery query) {
        return R.ok(spuService.page(query));
    }

    /**
     * 查询 SPU 详情。
     *
     * @param id SPU ID
     * @return SPU
     */
    @GetMapping("/{id}")
    public R<ProductSpu> detail(@PathVariable("id") Long id) {
        return R.ok(spuService.detail(id));
    }

    /**
     * 新增 SPU。
     *
     * @param request SPU 请求
     * @return SPU ID
     */
    @PostMapping
    @TenantWriteGuard(scene = "新增SPU商品")
    public R<Long> create(@Valid @RequestBody ProductSpuRequest request) {
        return R.ok(spuService.create(request));
    }

    /**
     * 编辑 SPU。
     *
     * @param id      SPU ID
     * @param request SPU 请求
     * @return 无数据响应
     */
    @PutMapping("/{id}")
    @TenantWriteGuard(scene = "编辑SPU商品")
    public R<Void> update(@PathVariable("id") Long id, @Valid @RequestBody ProductSpuRequest request) {
        spuService.update(id, request);
        return R.ok();
    }

    /**
     * 删除 SPU。
     *
     * @param id SPU ID
     * @return 无数据响应
     */
    @DeleteMapping("/{id}")
    @TenantWriteGuard(scene = "删除SPU商品")
    public R<Void> delete(@PathVariable("id") Long id) {
        spuService.delete(id);
        return R.ok();
    }

    /**
     * 提交审核。
     *
     * @param id SPU ID
     * @return 无数据响应
     */
    @PutMapping("/{id}/submit")
    @TenantWriteGuard(scene = "提交商品审核")
    public R<Void> submit(@PathVariable("id") Long id) {
        spuService.submit(id);
        return R.ok();
    }

    /**
     * 商品上架。
     *
     * @param id SPU ID
     * @return 无数据响应
     */
    @PutMapping("/{id}/on-sale")
    @TenantWriteGuard(scene = "商品上架")
    public R<Void> onSale(@PathVariable("id") Long id) {
        spuService.onSale(id);
        return R.ok();
    }

    /**
     * 商品下架。
     *
     * @param id SPU ID
     * @return 无数据响应
     */
    @PutMapping("/{id}/off-sale")
    @TenantWriteGuard(scene = "商品下架")
    public R<Void> offSale(@PathVariable("id") Long id) {
        spuService.offSale(id);
        return R.ok();
    }

    /**
     * 查询多语言内容。
     *
     * @param id SPU ID
     * @return 多语言内容
     */
    @GetMapping("/{id}/i18n")
    public R<List<ProductI18n>> i18n(@PathVariable("id") Long id) {
        return R.ok(spuService.i18n(id));
    }

    /**
     * 保存多语言内容。
     *
     * @param id       SPU ID
     * @param langCode 语言代码
     * @param request  多语言请求
     * @return 无数据响应
     */
    @PutMapping("/{id}/i18n/{langCode}")
    @TenantWriteGuard(scene = "保存商品多语言")
    public R<Void> saveI18n(@PathVariable("id") Long id,
                            @PathVariable("langCode") String langCode,
                            @RequestBody ProductI18nRequest request) {
        spuService.saveI18n(id, langCode, request);
        return R.ok();
    }

    /**
     * AI 一键翻译。
     *
     * @param id      SPU ID
     * @param request 翻译请求
     * @return 无数据响应
     */
    @PostMapping("/{id}/i18n/translate")
    @TenantWriteGuard(scene = "AI翻译商品文案")
    public R<Void> translate(@PathVariable("id") Long id, @RequestBody ProductTranslateRequest request) {
        spuService.translate(id, request);
        return R.ok();
    }

    /**
     * 保存商品图片。
     *
     * @param id      SPU ID
     * @param request 图片请求
     * @return 图片ID
     */
    @PostMapping("/{id}/images")
    @TenantWriteGuard(scene = "保存商品图片")
    public R<Long> saveImage(@PathVariable("id") Long id, @Valid @RequestBody ProductImageRequest request) {
        return R.ok(spuService.saveImage(id, request));
    }
}
