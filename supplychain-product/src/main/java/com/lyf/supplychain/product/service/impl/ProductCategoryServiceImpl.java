package com.lyf.supplychain.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lyf.supplychain.common.context.TenantContext;
import com.lyf.supplychain.common.exception.BusinessException;
import com.lyf.supplychain.product.entity.ProductAttrTemplate;
import com.lyf.supplychain.product.entity.ProductCategory;
import com.lyf.supplychain.product.mapper.ProductAttrTemplateMapper;
import com.lyf.supplychain.product.mapper.ProductCategoryMapper;
import com.lyf.supplychain.product.request.ProductCategoryRequest;
import com.lyf.supplychain.product.service.ProductCategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 商品分类业务服务实现。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Service
public class ProductCategoryServiceImpl implements ProductCategoryService {

    private final ProductCategoryMapper categoryMapper;
    private final ProductAttrTemplateMapper attrTemplateMapper;

    public ProductCategoryServiceImpl(ProductCategoryMapper categoryMapper, ProductAttrTemplateMapper attrTemplateMapper) {
        this.categoryMapper = categoryMapper;
        this.attrTemplateMapper = attrTemplateMapper;
    }

    /**
     * 查询分类树形数据。
     *
     * @return 分类列表
     */
    @Override
    public List<ProductCategory> tree() {
        return categoryMapper.selectList(new LambdaQueryWrapper<ProductCategory>()
                .eq(ProductCategory::getStatus, 1)
                .orderByAsc(ProductCategory::getLevel, ProductCategory::getSortOrder));
    }

    /**
     * 新增商品分类并维护路径枚举。
     *
     * @param request 分类请求
     * @return 分类ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(ProductCategoryRequest request) {
        ProductCategory parent = null;
        if (request.getParentId() != null && request.getParentId() > 0) {
            parent = categoryMapper.selectById(request.getParentId());
            if (parent == null) {
                BusinessException.throwException(14004, "分类不存在");
            }
            if (parent.getLevel() >= 5) {
                BusinessException.throwException(14004, "商品分类最多支持5级");
            }
        }
        ProductCategory category = new ProductCategory();
        category.setTenantId(TenantContext.getTenantId());
        category.setParentId(request.getParentId() == null ? 0L : request.getParentId());
        category.setCategoryName(request.getCategoryName());
        category.setCategoryNameEn(request.getCategoryNameEn());
        category.setIconUrl(request.getIconUrl());
        category.setSortOrder(request.getSortOrder());
        category.setStatus(1);
        category.setLevel(parent == null ? 1 : parent.getLevel() + 1);
        category.setPath(parent == null ? "/" : parent.getPath());
        categoryMapper.insert(category);
        category.setPath(category.getPath() + category.getId() + "/");
        categoryMapper.updateById(category);
        return category.getId();
    }

    /**
     * 查询分类属性模板。
     *
     * @param categoryId 分类ID
     * @return 属性模板列表
     */
    @Override
    public List<ProductAttrTemplate> attrs(Long categoryId) {
        return attrTemplateMapper.selectList(new LambdaQueryWrapper<ProductAttrTemplate>()
                .eq(ProductAttrTemplate::getCategoryId, categoryId)
                .eq(ProductAttrTemplate::getStatus, 1)
                .orderByAsc(ProductAttrTemplate::getSortOrder));
    }
}
