package com.lyf.supplychain.logistics.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lyf.supplychain.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 物流商实体。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("logistics_carrier")
public class LogisticsCarrier extends BaseEntity {

    private String carrierCode;
    private String carrierName;
    private String carrierNameEn;
    private Integer carrierType;
    private String logoUrl;
    private String apiBaseUrl;
    private String apiKey;
    private String apiSecret;
    private String apiAccount;
    private String apiVersion;
    private String trackApiUrl;
    private Integer supportLabel;
    private Integer supportTrack;
    private Integer status;
    private String remark;
}
