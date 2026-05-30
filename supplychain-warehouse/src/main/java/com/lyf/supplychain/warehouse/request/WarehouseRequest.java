package com.lyf.supplychain.warehouse.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 仓库保存请求。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Data
public class WarehouseRequest {

    @NotBlank(message = "仓库编码不能为空")
    private String warehouseCode;
    @NotBlank(message = "仓库名称不能为空")
    private String warehouseName;
    @NotNull(message = "仓库类型不能为空")
    private Integer warehouseType;
    private String countryCode;
    private String countryName;
    private String province;
    private String city;
    private String address;
    private String zipCode;
    private String contactName;
    private String contactPhone;
    private String contactEmail;
    private BigDecimal areaSqm;
    private Integer isDefault;
    private Integer status;
    private String remark;
}
