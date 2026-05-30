package com.lyf.supplychain.supplier.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 新增供应商请求。
 *
 * @author liyunfei
 * @date 2026-05-16
 */
@Data
public class SupplierCreateRequest {

    @NotBlank(message = "供应商名称不能为空")
    @Size(min = 2, max = 128, message = "供应商名称长度必须在2-128字符之间")
    private String supplierName;

    @NotNull(message = "供应商类型不能为空")
    private Integer supplierType;

    private List<Long> categoryIds;

    private String province;

    private String city;

    private String address;

    private String website;

    private Integer companySize;

    private Integer foundedYear;

    @NotBlank(message = "主联系人不能为空")
    private String contactName;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
    private String contactPhone;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String contactEmail;

    private String contactWechat;

    private String contactWhatsapp;

    private String bankName;

    private String bankAccount;

    private String bankAccountName;

    private String taxNo;

    private Integer invoiceType;

    @Min(value = 1, message = "最小起订量必须大于0")
    private Integer moq;

    @Min(value = 1, message = "交货周期必须在1-365天之间")
    @Max(value = 365, message = "交货周期必须在1-365天之间")
    private Integer leadTimeDays;

    private Integer monthlyCapacity;

    private String currency;

    private Integer paymentDays;

    private String remark;

    private List<String> tags;
}
