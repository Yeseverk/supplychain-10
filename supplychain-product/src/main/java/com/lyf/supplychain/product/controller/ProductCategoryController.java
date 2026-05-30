package com.lyf.supplychain.product.controller;

import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.common.security.annotation.TenantWriteGuard;
import com.lyf.supplychain.product.entity.ProductAttrTemplate;
import com.lyf.supplychain.product.entity.ProductCategory;
import com.lyf.supplychain.product.request.ProductCategoryRequest;
import com.lyf.supplychain.product.service.ProductCategoryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 商品分类接口。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@RestController
@RequestMapping({"/api/pim/categories", "/pim/categories"})
@com.lyf.supplychain.common.security.annotation.RequiresPermission(com.lyf.supplychain.common.security.constant.PermissionCodes.PIM_PRODUCT_MANAGE)
public class ProductCategoryController {

    private final ProductCategoryService categoryService;

    public ProductCategoryController(ProductCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * 查询分类树。
     *
     * @return 分类树
     */
    @GetMapping("/tree")
    public R<List<ProductCategory>> tree() {
        return R.ok(categoryService.tree());
    }

    /**
     * 新增分类。
     *
     * @param request 分类请求
     * @return 分类ID
     */
    @PostMapping
    @TenantWriteGuard(scene = "新增商品分类")
    public R<Long> create(@Valid @RequestBody ProductCategoryRequest request) {
        return R.ok(categoryService.create(request));
    }

    /**
     * 查询分类属性模板。
     *
     * @param id 分类ID
     * @return 属性模板列表
     */
    @GetMapping("/{id}/attrs")
    public R<List<ProductAttrTemplate>> attrs(@PathVariable("id") Long id) {
        return R.ok(categoryService.attrs(id));
    }
}
