package com.lyf.supplychain.supplier.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

/**
 * 编辑供应商请求。
 *
 * @author liyunfei
 * @date 2026-05-16
 */
@Data
public class SupplierUpdateRequest {

    @NotNull(message = "版本号不能为空")
    private Integer version;

    private String supplierName;

    private Integer supplierType;

    private List<Long> categoryIds;

    private String province;

    private String city;

    private String address;

    private String website;

    private Integer companySize;

    private Integer foundedYear;

    private String contactName;

    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
    private String contactPhone;

    @Email(message = "邮箱格式不正确")
    private String contactEmail;

    private String contactWechat;

    private String contactWhatsapp;

    private String bankName;

    private String bankAccount;

    private String bankAccountName;

    private String taxNo;

    private Integer invoiceType;

    private Integer moq;

    private Integer leadTimeDays;

    private Integer monthlyCapacity;

    private String currency;

    private Integer paymentDays;

    private String remark;

    private List<String> tags;
}
