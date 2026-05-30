package com.lyf.supplychain.product.service;

import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.product.entity.ProductI18n;
import com.lyf.supplychain.product.entity.ProductImage;
import com.lyf.supplychain.product.entity.ProductSpu;
import com.lyf.supplychain.product.request.ProductI18nRequest;
import com.lyf.supplychain.product.request.ProductImageRequest;
import com.lyf.supplychain.product.request.ProductSpuPageQuery;
import com.lyf.supplychain.product.request.ProductSpuRequest;
import com.lyf.supplychain.product.request.ProductTranslateRequest;

import java.util.List;

/**
 * SPU 商品业务服务。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
public interface ProductSpuService {

    /**
     * 分页查询 SPU。
     *
     * @param query 分页参数
     * @return SPU 分页结果
     */
    PageResult<ProductSpu> page(ProductSpuPageQuery query);

    /**
     * 查询 SPU 详情。
     *
     * @param id SPU ID
     * @return SPU
     */
    ProductSpu detail(Long id);

    /**
     * 创建 SPU 草稿。
     *
     * @param request SPU 请求
     * @return SPU ID
     */
    Long create(ProductSpuRequest request);

    /**
     * 编辑 SPU。
     *
     * @param id      SPU ID
     * @param request SPU 请求
     */
    void update(Long id, ProductSpuRequest request);

    /**
     * 删除草稿 SPU。
     *
     * @param id SPU ID
     */
    void delete(Long id);

    /**
     * 提交商品审核。
     *
     * @param id SPU ID
     */
    void submit(Long id);

    /**
     * 商品上架并执行完整性校验。
     *
     * @param id SPU ID
     */
    void onSale(Long id);

    /**
     * 商品下架。
     *
     * @param id SPU ID
     */
    void offSale(Long id);

    /**
     * 查询 SPU 多语言内容。
     *
     * @param id SPU ID
     * @return 多语言列表
     */
    List<ProductI18n> i18n(Long id);

    /**
     * 保存指定语言内容。
     *
     * @param id       SPU ID
     * @param langCode 语言代码
     * @param request  多语言请求
     */
    void saveI18n(Long id, String langCode, ProductI18nRequest request);

    /**
     * AI 翻译商品内容并保存草稿。
     *
     * @param id      SPU ID
     * @param request 翻译请求
     */
    void translate(Long id, ProductTranslateRequest request);

    /**
     * 保存商品图片记录。
     *
     * @param id      SPU ID
     * @param request 图片请求
     * @return 图片ID
     */
    Long saveImage(Long id, ProductImageRequest request);

    /**
     * 自动下架到期商品。
     *
     * @return 下架数量
     */
    int autoOffSaleExpired();
}
