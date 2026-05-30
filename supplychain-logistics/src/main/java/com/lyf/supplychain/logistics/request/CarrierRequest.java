package com.lyf.supplychain.logistics.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 物流商保存请求。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
public class CarrierRequest {

    @NotBlank(message = "物流商编码不能为空")
    private String carrierCode;
    @NotBlank(message = "物流商名称不能为空")
    private String carrierName;
    private String carrierNameEn;
    @NotNull(message = "物流商类型不能为空")
    private Integer carrierType;
    private String logoUrl;
    private String apiBaseUrl;
    private String apiKey;
    private String apiSecret;
    private String apiAccount;
    private String apiVersion;
    private String trackApiUrl;
    private Integer supportLabel = 1;
    private Integer supportTrack = 1;
    private Integer status = 1;
    private String remark;
}
