package com.lyf.supplychain.product.service;

import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.product.entity.ProductSku;
import com.lyf.supplychain.product.entity.ProductSkuPrice;
import com.lyf.supplychain.product.request.ProductPriceRequest;
import com.lyf.supplychain.product.request.ProductSkuBatchRequest;
import com.lyf.supplychain.product.request.ProductSkuPageQuery;
import com.lyf.supplychain.product.request.ProductSkuRequest;

import java.util.List;

/**
 * SKU 商品业务服务。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
public interface ProductSkuService {

    /**
     * 分页查询 SKU。
     *
     * @param query 查询参数
     * @return SKU 分页结果
     */
    PageResult<ProductSku> page(ProductSkuPageQuery query);

    /**
     * 查询 SPU 下的 SKU 列表。
     *
     * @param spuId SPU ID
     * @return SKU 列表
     */
    List<ProductSku> listBySpu(Long spuId);

    /**
     * 查询 SKU 详情。
     *
     * @param id SKU ID
     * @return SKU 详情
     */
    ProductSku detail(Long id);

    /**
     * 批量创建 SKU。
     *
     * @param spuId   SPU ID
     * @param request 批量请求
     * @return SKU 数量
     */
    Integer batchCreate(Long spuId, ProductSkuBatchRequest request);

    /**
     * 编辑 SKU。
     *
     * @param id      SKU ID
     * @param request SKU 请求
     */
    void update(Long id, ProductSkuRequest request);

    /**
     * 保存 SKU 价格。
     *
     * @param id      SKU ID
     * @param request 价格请求
     */
    void savePrices(Long id, ProductPriceRequest request);

    /**
     * 查询 SKU 下拉选项。
     *
     * @param keyword 关键词
     * @return SKU 列表
     */
    List<ProductSku> options(String keyword);

    /**
     * 按价格优先级查询有效价格。
     *
     * @param skuId       SKU ID
     * @param platform    平台
     * @param countryCode 国家
     * @return 价格记录
     */
    ProductSkuPrice choosePrice(Long skuId, String platform, String countryCode);
}
