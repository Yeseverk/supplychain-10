package com.lyf.supplychain.logistics.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 运单创建请求。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
public class WaybillRequest {

    private Long channelId;
    @NotNull(message = "订单ID不能为空")
    private Long orderId;
    @NotBlank(message = "订单号不能为空")
    private String orderNo;
    @NotNull(message = "仓库不能为空")
    private Long warehouseId;
    @NotBlank(message = "收件人不能为空")
    private String receiverName;
    private String receiverPhone;
    @NotBlank(message = "目的国不能为空")
    private String countryCode;
    private String state;
    private String city;
    @NotBlank(message = "地址不能为空")
    private String addressLine1;
    private String addressLine2;
    @NotBlank(message = "邮编不能为空")
    private String zipCode;
    @NotNull(message = "实际重量不能为空")
    private BigDecimal actualWeightG;
    private Integer lengthMm;
    private Integer widthMm;
    private Integer heightMm;
    @NotNull(message = "申报价值不能为空")
    private BigDecimal declaredValue;
    private String declaredCurrency = "USD";
    @NotBlank(message = "英文申报名不能为空")
    private String declaredNameEn;
    private String hsCode;
    private Integer isGift = 0;
    private Boolean hasBattery = false;
    private Boolean hasLiquid = false;
    private Boolean hasPowder = false;
}
