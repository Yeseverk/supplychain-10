package com.lyf.supplychain.product.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * SPU 保存请求。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
public class ProductSpuRequest {

    @NotBlank(message = "商品名称不能为空")
    private String spuName;

    @NotNull(message = "分类不能为空")
    private Long categoryId;

    private String categoryPath;

    private String brand;

    private String hsCode;

    private String originCountry;

    private String material;

    private String certifications;

    private LocalDateTime shelfOffTime;

    private String spuDesc;

    private String packageDesc;

    private String remark;
}
