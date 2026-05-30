package com.lyf.supplychain.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.lyf.supplychain.common.exception.BusinessException;
import com.lyf.supplychain.product.constant.ProductConstants;
import com.lyf.supplychain.product.entity.ProductSpu;
import com.lyf.supplychain.product.mapper.ProductI18nMapper;
import com.lyf.supplychain.product.mapper.ProductImageMapper;
import com.lyf.supplychain.product.mapper.ProductSkuMapper;
import com.lyf.supplychain.product.mapper.ProductSkuPriceMapper;
import com.lyf.supplychain.product.mapper.ProductSpuMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductSpuServiceImplTest {

    @Mock
    private ProductSpuMapper spuMapper;
    @Mock
    private ProductSkuMapper skuMapper;
    @Mock
    private ProductSkuPriceMapper priceMapper;
    @Mock
    private ProductI18nMapper i18nMapper;
    @Mock
    private ProductImageMapper imageMapper;

    private ProductSpuServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ProductSpuServiceImpl(spuMapper, skuMapper, priceMapper, i18nMapper, imageMapper);
    }

    @Test
    void onSaleRelistsOffSaleProductWithoutFirstPublishValidation() {
        ProductSpu spu = spu(1001L, ProductConstants.SPU_OFF_SALE);
        when(spuMapper.selectById(1001L)).thenReturn(spu);

        service.onSale(1001L);

        ArgumentCaptor<ProductSpu> captor = ArgumentCaptor.forClass(ProductSpu.class);
        verify(spuMapper).updateById(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(ProductConstants.SPU_ON_SALE);
        assertThat(captor.getValue().getPublishTime()).isBeforeOrEqualTo(LocalDateTime.now());
        verify(skuMapper, never()).selectCount(any(Wrapper.class));
    }

    @Test
    void onSaleRejectsStoppedProduct() {
        when(spuMapper.selectById(1002L)).thenReturn(spu(1002L, ProductConstants.SPU_STOPPED));

        assertThatThrownBy(() -> service.onSale(1002L))
                .isInstanceOf(BusinessException.class);

        verify(spuMapper, never()).updateById(any(ProductSpu.class));
    }

    @Test
    void offSaleOnlyAllowsOnSaleProduct() {
        ProductSpu spu = spu(1003L, ProductConstants.SPU_ON_SALE);
        when(spuMapper.selectById(1003L)).thenReturn(spu);

        service.offSale(1003L);

        ArgumentCaptor<ProductSpu> captor = ArgumentCaptor.forClass(ProductSpu.class);
        verify(spuMapper).updateById(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(ProductConstants.SPU_OFF_SALE);
    }

    @Test
    void offSaleRejectsNonOnSaleProduct() {
        when(spuMapper.selectById(1004L)).thenReturn(spu(1004L, ProductConstants.SPU_DRAFT));

        assertThatThrownBy(() -> service.offSale(1004L))
                .isInstanceOf(BusinessException.class);

        verify(spuMapper, never()).updateById(any(ProductSpu.class));
    }

    private ProductSpu spu(Long id, int status) {
        ProductSpu spu = new ProductSpu();
        spu.setId(id);
        spu.setStatus(status);
        spu.setSpuName("test-spu");
        return spu;
    }
}
