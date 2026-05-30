package com.lyf.supplychain.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.core.util.StrUtil;
import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.common.context.TenantContext;
import com.lyf.supplychain.common.exception.BusinessException;
import com.lyf.supplychain.product.constant.ProductConstants;
import com.lyf.supplychain.product.entity.ProductSku;
import com.lyf.supplychain.product.entity.ProductSkuPrice;
import com.lyf.supplychain.product.mapper.ProductSkuMapper;
import com.lyf.supplychain.product.mapper.ProductSkuPriceMapper;
import com.lyf.supplychain.product.request.ProductPriceRequest;
import com.lyf.supplychain.product.request.ProductSkuBatchRequest;
import com.lyf.supplychain.product.request.ProductSkuPageQuery;
import com.lyf.supplychain.product.request.ProductSkuPriceItemRequest;
import com.lyf.supplychain.product.request.ProductSkuRequest;
import com.lyf.supplychain.product.service.ProductSkuService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * SKU 商品业务服务实现。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Service
public class ProductSkuServiceImpl implements ProductSkuService {

    private final ProductSkuMapper skuMapper;
    private final ProductSkuPriceMapper priceMapper;

    public ProductSkuServiceImpl(ProductSkuMapper skuMapper, ProductSkuPriceMapper priceMapper) {
        this.skuMapper = skuMapper;
        this.priceMapper = priceMapper;
    }

    @Override
    public PageResult<ProductSku> page(ProductSkuPageQuery query) {
        query.normalize();
        LambdaQueryWrapper<ProductSku> wrapper = new LambdaQueryWrapper<>();
        if (query.getSpuId() != null) {
            wrapper.eq(ProductSku::getSpuId, query.getSpuId());
        }
        if (query.getStatus() != null) {
            wrapper.eq(ProductSku::getStatus, query.getStatus());
        }
        if (StrUtil.isNotBlank(query.getKeyword())) {
            String keyword = query.getKeyword().trim();
            wrapper.and(item -> item.like(ProductSku::getSkuCode, keyword)
                    .or().like(ProductSku::getSkuName, keyword)
                    .or().like(ProductSku::getBarcode, keyword)
                    .or().like(ProductSku::getFnsku, keyword)
                    .or().like(ProductSku::getSpecValues, keyword)
                    .or().like(ProductSku::getRemark, keyword));
        }
        wrapper.orderByDesc(ProductSku::getCreateTime);
        return PageResult.from(skuMapper.selectPage(Page.of(query.getPageNum(), query.getPageSize()), wrapper));
    }

    /**
     * 查询 SPU 下的 SKU 列表。
     *
     * @param spuId SPU ID
     * @return SKU 列表
     */
    @Override
    public List<ProductSku> listBySpu(Long spuId) {
        return skuMapper.selectList(new LambdaQueryWrapper<ProductSku>()
                .eq(ProductSku::getSpuId, spuId)
                .orderByDesc(ProductSku::getCreateTime));
    }

    @Override
    public ProductSku detail(Long id) {
        ProductSku sku = skuMapper.selectById(id);
        if (sku == null) {
            BusinessException.throwException(14002, "SKU不存在或已停售");
        }
        return sku;
    }

    /**
     * 批量创建 SKU。
     *
     * @param spuId   SPU ID
     * @param request 批量请求
     * @return SKU 数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer batchCreate(Long spuId, ProductSkuBatchRequest request) {
        int index = 1;
        for (ProductSkuRequest item : request.getSkus()) {
            ProductSku sku = toSku(spuId, item);
            if (sku.getSkuCode() == null || sku.getSkuCode().isBlank()) {
                sku.setSkuCode("SKU-" + spuId + "-" + String.format("%04d", index));
            }
            skuMapper.insert(sku);
            index++;
        }
        return request.getSkus().size();
    }

    /**
     * 编辑 SKU。
     *
     * @param id      SKU ID
     * @param request SKU 请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, ProductSkuRequest request) {
        ProductSku sku = skuMapper.selectById(id);
        if (sku == null) {
            BusinessException.throwException(14002, "SKU不存在或已停售");
        }
        ProductSku update = toSku(sku.getSpuId(), request);
        update.setId(id);
        update.setSkuCode(request.getSkuCode() == null ? sku.getSkuCode() : request.getSkuCode());
        skuMapper.updateById(update);
    }

    /**
     * 保存 SKU 价格。
     *
     * @param id      SKU ID
     * @param request 价格请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void savePrices(Long id, ProductPriceRequest request) {
        ProductSku sku = skuMapper.selectById(id);
        if (sku == null) {
            BusinessException.throwException(14002, "SKU不存在或已停售");
        }
        priceMapper.delete(new LambdaUpdateWrapper<ProductSkuPrice>().eq(ProductSkuPrice::getSkuId, id));
        for (ProductSkuPriceItemRequest item : request.getPrices()) {
            ProductSkuPrice price = new ProductSkuPrice();
            price.setTenantId(TenantContext.getTenantId());
            price.setSkuId(id);
            price.setPriceType(item.getPriceType());
            price.setPlatform(item.getPlatform() == null ? ProductConstants.PLATFORM_ALL : item.getPlatform());
            price.setCountryCode(item.getCountryCode());
            price.setPrice(item.getPrice());
            price.setCurrency(item.getCurrency());
            price.setMinQty(item.getMinQty());
            price.setMaxQty(item.getMaxQty());
            price.setEffectiveTime(item.getEffectiveTime());
            price.setExpireTime(item.getExpireTime());
            price.setIsActive(1);
            priceMapper.insert(price);
        }
    }

    /**
     * 查询 SKU 下拉选项。
     *
     * @param keyword 关键词
     * @return SKU 列表
     */
    @Override
    public List<ProductSku> options(String keyword) {
        LambdaQueryWrapper<ProductSku> wrapper = new LambdaQueryWrapper<ProductSku>()
                .eq(ProductSku::getStatus, ProductConstants.SKU_ON_SALE)
                .orderByDesc(ProductSku::getCreateTime)
                .last("limit 50");
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(item -> item.like(ProductSku::getSkuName, keyword).or().like(ProductSku::getSkuCode, keyword));
        }
        return skuMapper.selectList(wrapper);
    }

    /**
     * 按价格优先级查询有效价格。
     *
     * @param skuId       SKU ID
     * @param platform    平台
     * @param countryCode 国家
     * @return 价格记录
     */
    @Override
    public ProductSkuPrice choosePrice(Long skuId, String platform, String countryCode) {
        LocalDateTime now = LocalDateTime.now();
        return priceMapper.selectList(new LambdaQueryWrapper<ProductSkuPrice>()
                        .eq(ProductSkuPrice::getSkuId, skuId)
                        .eq(ProductSkuPrice::getIsActive, 1))
                .stream()
                .filter(item -> item.getExpireTime() == null || item.getExpireTime().isAfter(now))
                .filter(item -> item.getEffectiveTime() == null || item.getEffectiveTime().isBefore(now))
                .filter(item -> ProductConstants.PLATFORM_ALL.equals(item.getPlatform()) || item.getPlatform().equals(platform))
                .filter(item -> item.getCountryCode() == null || item.getCountryCode().equals(countryCode))
                .min(Comparator.comparingInt(this::pricePriority))
                .orElse(null);
    }

    private ProductSku toSku(Long spuId, ProductSkuRequest request) {
        ProductSku sku = new ProductSku();
        sku.setTenantId(TenantContext.getTenantId());
        sku.setSpuId(spuId);
        sku.setSkuCode(request.getSkuCode());
        sku.setSkuName(request.getSkuName());
        sku.setBarcode(request.getBarcode());
        sku.setFnsku(request.getFnsku());
        sku.setSpecValues(request.getSpecValues() == null ? "{}" : request.getSpecValues());
        sku.setSpecValuesEn(request.getSpecValuesEn());
        sku.setNetWeightG(request.getNetWeightG());
        sku.setGrossWeightG(request.getGrossWeightG());
        sku.setLengthMm(request.getLengthMm());
        sku.setWidthMm(request.getWidthMm());
        sku.setHeightMm(request.getHeightMm());
        sku.setIsBattery(request.getIsBattery());
        sku.setIsLiquid(request.getIsLiquid());
        sku.setIsPowder(request.getIsPowder());
        sku.setCostPrice(request.getCostPrice());
        sku.setCostCurrency(request.getCostCurrency());
        sku.setAbcClass(request.getAbcClass());
        sku.setStatus(request.getStatus() == null ? ProductConstants.SKU_DRAFT : request.getStatus());
        sku.setRemark(request.getRemark());
        return sku;
    }

    private int pricePriority(ProductSkuPrice price) {
        if (ProductConstants.PRICE_ACTIVITY == price.getPriceType()) {
            return 1;
        }
        if (ProductConstants.PRICE_PLATFORM == price.getPriceType() && price.getCountryCode() != null) {
            return 2;
        }
        if (ProductConstants.PRICE_PLATFORM == price.getPriceType()) {
            return 3;
        }
        if (ProductConstants.PRICE_SUGGESTED == price.getPriceType()) {
            return 4;
        }
        return 9;
    }
}
