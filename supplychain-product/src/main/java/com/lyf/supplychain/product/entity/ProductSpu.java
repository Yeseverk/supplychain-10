package com.lyf.supplychain.product.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lyf.supplychain.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * SPU 商品标准单元实体。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product_spu")
public class ProductSpu extends BaseEntity {

    private String spuCode;

    private String spuName;

    private Long categoryId;

    private String categoryPath;

    private String brand;

    private String hsCode;

    private String originCountry;

    private String material;

    private String certifications;

    private Integer status;

    private LocalDateTime publishTime;

    private LocalDateTime shelfOffTime;

    private String spuDesc;

    private String packageDesc;

    private String remark;
}
