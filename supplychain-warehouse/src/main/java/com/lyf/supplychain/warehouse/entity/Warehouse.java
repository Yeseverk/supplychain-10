package com.lyf.supplychain.warehouse.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lyf.supplychain.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 仓库主表实体。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("warehouse")
public class Warehouse extends BaseEntity {

    private String warehouseCode;
    private String warehouseName;
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
    private Integer totalLocations;
    private Integer usedLocations;
    private Integer isDefault;
    private Integer status;
    private String remark;
}
