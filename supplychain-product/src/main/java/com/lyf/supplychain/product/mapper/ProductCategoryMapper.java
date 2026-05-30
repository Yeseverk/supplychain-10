package com.lyf.supplychain.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lyf.supplychain.product.entity.ProductCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品分类 Mapper。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Mapper
public interface ProductCategoryMapper extends BaseMapper<ProductCategory> {
}
