package com.lyf.supplychain.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.common.context.TenantContext;
import com.lyf.supplychain.common.exception.BusinessException;
import com.lyf.supplychain.product.constant.ProductConstants;
import com.lyf.supplychain.product.entity.ProductI18n;
import com.lyf.supplychain.product.entity.ProductImage;
import com.lyf.supplychain.product.entity.ProductSku;
import com.lyf.supplychain.product.entity.ProductSkuPrice;
import com.lyf.supplychain.product.entity.ProductSpu;
import com.lyf.supplychain.product.mapper.ProductI18nMapper;
import com.lyf.supplychain.product.mapper.ProductImageMapper;
import com.lyf.supplychain.product.mapper.ProductSkuMapper;
import com.lyf.supplychain.product.mapper.ProductSkuPriceMapper;
import com.lyf.supplychain.product.mapper.ProductSpuMapper;
import com.lyf.supplychain.product.request.ProductI18nRequest;
import com.lyf.supplychain.product.request.ProductImageRequest;
import com.lyf.supplychain.product.request.ProductSpuPageQuery;
import com.lyf.supplychain.product.request.ProductSpuRequest;
import com.lyf.supplychain.product.request.ProductTranslateRequest;
import com.lyf.supplychain.product.service.ProductSpuService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * SPU 商品业务服务实现。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Service
public class ProductSpuServiceImpl implements ProductSpuService {

    private final ProductSpuMapper spuMapper;
    private final ProductSkuMapper skuMapper;
    private final ProductSkuPriceMapper priceMapper;
    private final ProductI18nMapper i18nMapper;
    private final ProductImageMapper imageMapper;

    public ProductSpuServiceImpl(ProductSpuMapper spuMapper,
                                 ProductSkuMapper skuMapper,
                                 ProductSkuPriceMapper priceMapper,
                                 ProductI18nMapper i18nMapper,
                                 ProductImageMapper imageMapper) {
        this.spuMapper = spuMapper;
        this.skuMapper = skuMapper;
        this.priceMapper = priceMapper;
        this.i18nMapper = i18nMapper;
        this.imageMapper = imageMapper;
    }

    /**
     * 分页查询 SPU。
     *
     * @param query 分页参数
     * @return SPU 分页结果
     */
    @Override
    public PageResult<ProductSpu> page(ProductSpuPageQuery query) {
        query.normalize();
        LambdaQueryWrapper<ProductSpu> wrapper = new LambdaQueryWrapper<ProductSpu>()
                .orderByDesc(ProductSpu::getCreateTime);
        if (query.getStatus() != null) {
            wrapper.eq(ProductSpu::getStatus, query.getStatus());
        }
        if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
            String keyword = query.getKeyword().trim();
            wrapper.and(item -> item.like(ProductSpu::getSpuCode, keyword)
                    .or().like(ProductSpu::getSpuName, keyword)
                    .or().like(ProductSpu::getBrand, keyword)
                    .or().like(ProductSpu::getCategoryPath, keyword));
        }
        Page<ProductSpu> page = spuMapper.selectPage(Page.of(query.getPageNum(), query.getPageSize()),
                wrapper);
        return PageResult.from(page);
    }

    /**
     * 查询 SPU 详情。
     *
     * @param id SPU ID
     * @return SPU
     */
    @Override
    public ProductSpu detail(Long id) {
        ProductSpu spu = spuMapper.selectById(id);
        if (spu == null) {
            BusinessException.throwException(14001, "SPU不存在");
        }
        return spu;
    }

    /**
     * 创建 SPU 草稿。
     *
     * @param request SPU 请求
     * @return SPU ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(ProductSpuRequest request) {
        ProductSpu spu = toSpu(request);
        spu.setSpuCode("SPU-" + java.time.LocalDate.now().toString().replace("-", "") + "-" + System.nanoTime() % 10000);
        spu.setStatus(ProductConstants.SPU_DRAFT);
        spuMapper.insert(spu);
        return spu.getId();
    }

    /**
     * 编辑 SPU。
     *
     * @param id      SPU ID
     * @param request SPU 请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, ProductSpuRequest request) {
        ProductSpu old = detail(id);
        if (ProductConstants.SPU_ON_SALE == old.getStatus()) {
            BusinessException.throwException(14005, "已上架商品请先下架后编辑");
        }
        ProductSpu update = toSpu(request);
        update.setId(id);
        spuMapper.updateById(update);
    }

    /**
     * 删除草稿 SPU。
     *
     * @param id SPU ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        ProductSpu spu = detail(id);
        if (ProductConstants.SPU_DRAFT != spu.getStatus()) {
            BusinessException.throwException(14005, "仅草稿商品允许删除");
        }
        spuMapper.deleteById(id);
    }

    /**
     * 提交商品审核。
     *
     * @param id SPU ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submit(Long id) {
        ProductSpu spu = detail(id);
        if (ProductConstants.SPU_DRAFT != spu.getStatus()) {
            BusinessException.throwException(14005, "只有草稿商品可以提交审核");
        }
        spu.setStatus(ProductConstants.SPU_PENDING_AUDIT);
        spuMapper.updateById(spu);
    }

    /**
     * 商品上架并执行完整性校验。
     *
     * @param id SPU ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onSale(Long id) {
        ProductSpu spu = detail(id);
        if (ProductConstants.SPU_STOPPED == spu.getStatus()) {
            BusinessException.throwException(14005, "已停售商品不能重新上架");
        }
        if (ProductConstants.SPU_OFF_SALE != spu.getStatus()) {
            validatePublish(spu);
        }
        spu.setStatus(ProductConstants.SPU_ON_SALE);
        spu.setPublishTime(LocalDateTime.now());
        spuMapper.updateById(spu);
    }

    /**
     * 商品下架。
     *
     * @param id SPU ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void offSale(Long id) {
        ProductSpu spu = detail(id);
        if (ProductConstants.SPU_ON_SALE != spu.getStatus()) {
            BusinessException.throwException(14005, "只有已上架商品可以下架");
        }
        spu.setStatus(ProductConstants.SPU_OFF_SALE);
        spuMapper.updateById(spu);
    }

    /**
     * 查询 SPU 多语言内容。
     *
     * @param id SPU ID
     * @return 多语言列表
     */
    @Override
    public List<ProductI18n> i18n(Long id) {
        return i18nMapper.selectList(new LambdaQueryWrapper<ProductI18n>()
                .eq(ProductI18n::getRefType, ProductConstants.REF_TYPE_SPU)
                .eq(ProductI18n::getRefId, id));
    }

    /**
     * 保存指定语言内容。
     *
     * @param id       SPU ID
     * @param langCode 语言代码
     * @param request  多语言请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveI18n(Long id, String langCode, ProductI18nRequest request) {
        detail(id);
        ProductI18n i18n = i18nMapper.selectOne(new LambdaQueryWrapper<ProductI18n>()
                .eq(ProductI18n::getRefType, ProductConstants.REF_TYPE_SPU)
                .eq(ProductI18n::getRefId, id)
                .eq(ProductI18n::getLangCode, langCode)
                .last("limit 1"));
        boolean create = i18n == null;
        if (create) {
            i18n = new ProductI18n();
            i18n.setTenantId(TenantContext.getTenantId());
            i18n.setRefType(ProductConstants.REF_TYPE_SPU);
            i18n.setRefId(id);
            i18n.setLangCode(langCode);
        }
        i18n.setTitle(request.getTitle());
        i18n.setSubtitle(request.getSubtitle());
        i18n.setBulletPoints(request.getBulletPoints());
        i18n.setDescription(request.getDescription());
        i18n.setKeywords(request.getKeywords());
        i18n.setSearchTerms(request.getSearchTerms());
        i18n.setIsAiTranslated(0);
        if (create) {
            i18nMapper.insert(i18n);
        } else {
            i18nMapper.updateById(i18n);
        }
    }

    /**
     * AI 翻译商品内容并保存草稿。
     *
     * @param id      SPU ID
     * @param request 翻译请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void translate(Long id, ProductTranslateRequest request) {
        ProductI18n source = i18nMapper.selectOne(new LambdaQueryWrapper<ProductI18n>()
                .eq(ProductI18n::getRefType, ProductConstants.REF_TYPE_SPU)
                .eq(ProductI18n::getRefId, id)
                .eq(ProductI18n::getLangCode, ProductConstants.LANG_ZH_CN)
                .last("limit 1"));
        if (source == null) {
            BusinessException.throwException(14009, "缺少中文内容，无法执行AI翻译");
        }
        for (String langCode : request.getTargetLangCodes()) {
            ProductI18nRequest target = new ProductI18nRequest();
            target.setTitle("[AI-" + langCode + "] " + source.getTitle());
            target.setSubtitle(source.getSubtitle());
            target.setBulletPoints(source.getBulletPoints());
            target.setDescription("[AI-" + langCode + "] " + source.getDescription());
            saveI18n(id, langCode, target);
            i18nMapper.update(null, new LambdaUpdateWrapper<ProductI18n>()
                    .eq(ProductI18n::getRefType, ProductConstants.REF_TYPE_SPU)
                    .eq(ProductI18n::getRefId, id)
                    .eq(ProductI18n::getLangCode, langCode)
                    .set(ProductI18n::getIsAiTranslated, 1));
        }
    }

    /**
     * 保存商品图片记录。
     *
     * @param id      SPU ID
     * @param request 图片请求
     * @return 图片ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveImage(Long id, ProductImageRequest request) {
        detail(id);
        ProductImage image = new ProductImage();
        image.setTenantId(TenantContext.getTenantId());
        image.setSpuId(id);
        image.setSkuId(request.getSkuId());
        image.setImageType(request.getImageType());
        image.setImageUrl(request.getImageUrl());
        image.setThumbUrl(request.getThumbUrl());
        image.setImageWidth(request.getImageWidth());
        image.setImageHeight(request.getImageHeight());
        image.setFileSize(request.getFileSize());
        image.setSortOrder(request.getSortOrder());
        image.setAltText(request.getAltText());
        imageMapper.insert(image);
        return image.getId();
    }

    /**
     * 自动下架到期商品。
     *
     * @return 下架数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int autoOffSaleExpired() {
        return spuMapper.update(null, new LambdaUpdateWrapper<ProductSpu>()
                .eq(ProductSpu::getStatus, ProductConstants.SPU_ON_SALE)
                .isNotNull(ProductSpu::getShelfOffTime)
                .le(ProductSpu::getShelfOffTime, LocalDateTime.now())
                .set(ProductSpu::getStatus, ProductConstants.SPU_OFF_SALE));
    }

    private ProductSpu toSpu(ProductSpuRequest request) {
        ProductSpu spu = new ProductSpu();
        spu.setTenantId(TenantContext.getTenantId());
        spu.setSpuName(request.getSpuName());
        spu.setCategoryId(request.getCategoryId());
        spu.setCategoryPath(request.getCategoryPath() == null ? "/" + request.getCategoryId() + "/" : request.getCategoryPath());
        spu.setBrand(request.getBrand());
        spu.setHsCode(request.getHsCode());
        spu.setOriginCountry(request.getOriginCountry());
        spu.setMaterial(request.getMaterial());
        spu.setCertifications(request.getCertifications());
        spu.setShelfOffTime(request.getShelfOffTime());
        spu.setSpuDesc(request.getSpuDesc());
        spu.setPackageDesc(request.getPackageDesc());
        spu.setRemark(request.getRemark());
        return spu;
    }

    private void validatePublish(ProductSpu spu) {
        if (spu.getHsCode() == null || !spu.getHsCode().matches("\\d{6,10}")) {
            BusinessException.throwException(14007, "HS编码格式不正确（需6-10位数字）");
        }
        long mainImageCount = imageMapper.selectCount(new LambdaQueryWrapper<ProductImage>()
                .eq(ProductImage::getSpuId, spu.getId())
                .eq(ProductImage::getImageType, 1));
        if (mainImageCount <= 0) {
            BusinessException.throwException(14005, "请上传至少1张主图");
        }
        long enCount = i18nMapper.selectCount(new LambdaQueryWrapper<ProductI18n>()
                .eq(ProductI18n::getRefType, ProductConstants.REF_TYPE_SPU)
                .eq(ProductI18n::getRefId, spu.getId())
                .eq(ProductI18n::getLangCode, ProductConstants.LANG_EN_US)
                .isNotNull(ProductI18n::getTitle));
        if (enCount <= 0) {
            BusinessException.throwException(14005, "请填写英文标题");
        }
        List<ProductSku> skus = skuMapper.selectList(new LambdaQueryWrapper<ProductSku>()
                .eq(ProductSku::getSpuId, spu.getId()));
        if (skus.isEmpty()) {
            BusinessException.throwException(14006, "必须至少有1个SKU才能上架");
        }
        for (ProductSku sku : skus) {
            if (sku.getGrossWeightG() == null || sku.getGrossWeightG().compareTo(BigDecimal.ZERO) <= 0) {
                BusinessException.throwException(14005, "SKU重量未填写，无法计算运费");
            }
            if (sku.getCostPrice() == null || sku.getCostPrice().compareTo(BigDecimal.ZERO) <= 0) {
                BusinessException.throwException(14005, "成本价为空，无法计算利润");
            }
            long priceCount = priceMapper.selectCount(new LambdaQueryWrapper<ProductSkuPrice>()
                    .eq(ProductSkuPrice::getSkuId, sku.getId())
                    .eq(ProductSkuPrice::getIsActive, 1));
            if (priceCount <= 0) {
                BusinessException.throwException(14005, "请设置商品售价");
            }
        }
    }
}
