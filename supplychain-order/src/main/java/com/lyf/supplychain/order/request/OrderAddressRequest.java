package com.lyf.supplychain.order.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 订单地址请求。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
public class OrderAddressRequest {

    @NotBlank(message = "收件人不能为空")
    private String receiverName;

    private String phone;

    private String email;

    @NotBlank(message = "国家不能为空")
    private String countryCode;

    private String countryName;

    private String state;

    private String city;

    @NotBlank(message = "地址不能为空")
    private String addressLine1;

    private String addressLine2;

    @NotBlank(message = "邮编不能为空")
    private String zipCode;
}
