package com.lyf.supplychain.purchase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 询价明细实体。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Data
@TableName("purchase_inquiry_item")
public class PurchaseInquiryItem {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long tenantId;
    private Long inquiryId;
    private Long skuId;
    private String skuCode;
    private String skuName;
    private Integer inquiryQty;
    private BigDecimal quotedPrice;
    private Integer quotedQty;
    private Integer deliveryDays;
    private Integer minOrderQty;
    private String remark;
}
