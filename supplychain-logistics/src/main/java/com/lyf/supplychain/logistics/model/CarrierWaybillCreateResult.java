package com.lyf.supplychain.logistics.model;

import lombok.Data;

/**
 * 物流商创建运单返回结果。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Data
public class CarrierWaybillCreateResult {

    private String trackingNo;
    private String labelUrl;
    private String labelFormat;
}
