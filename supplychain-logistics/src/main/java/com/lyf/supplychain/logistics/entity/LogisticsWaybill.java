package com.lyf.supplychain.logistics.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lyf.supplychain.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 物流运单实体。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("logistics_waybill")
public class LogisticsWaybill extends BaseEntity {

    private String waybillNo;
    private String trackingNo;
    private Long carrierId;
    private Long channelId;
    private Long orderId;
    private String orderNo;
    private Long warehouseId;
    private String receiverName;
    private String receiverPhone;
    private String countryCode;
    private String state;
    private String city;
    private String addressLine1;
    private String addressLine2;
    private String zipCode;
    private BigDecimal actualWeightG;
    private BigDecimal volumeWeightG;
    private BigDecimal chargeWeightG;
    private Integer lengthMm;
    private Integer widthMm;
    private Integer heightMm;
    private Integer packageCount;
    private BigDecimal declaredValue;
    private String declaredCurrency;
    private String declaredNameEn;
    private String hsCode;
    private Integer isGift;
    private BigDecimal estimatedFee;
    private BigDecimal actualFee;
    private String feeCurrency;
    private Integer status;
    private String labelUrl;
    private String labelFormat;
    private LocalDateTime createWaybillTime;
    private LocalDateTime pickupTime;
    private LocalDateTime signedTime;
    private String exceptionDesc;
}
