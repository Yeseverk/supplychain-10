package com.lyf.supplychain.product.service;

import com.lyf.supplychain.product.entity.ProductAttrTemplate;
import com.lyf.supplychain.product.entity.ProductCategory;
import com.lyf.supplychain.product.request.ProductCategoryRequest;

import java.util.List;

/**
 * 商品分类业务服务。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
public interface ProductCategoryService {

    /**
     * 查询分类树形数据。
     *
     * @return 分类列表
     */
    List<ProductCategory> tree();

    /**
     * 新增商品分类并维护路径枚举。
     *
     * @param request 分类请求
     * @return 分类ID
     */
    Long create(ProductCategoryRequest request);

    /**
     * 查询分类属性模板。
     *
     * @param categoryId 分类ID
     * @return 属性模板列表
     */
    List<ProductAttrTemplate> attrs(Long categoryId);
}
