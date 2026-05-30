package com.lyf.supplychain.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 订单收货地址实体。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
@TableName("order_address")
public class OrderAddress {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long tenantId;

    private Long orderId;

    private String receiverName;

    private String phone;

    private String email;

    private String countryCode;

    private String countryName;

    private String state;

    private String city;

    private String addressLine1;

    private String addressLine2;

    private String zipCode;

    private String fullAddress;

    private Integer isVerified;
}
