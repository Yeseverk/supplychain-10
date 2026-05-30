package com.lyf.supplychain.system.model.saas;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 租户自助注册请求。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
public class TenantRegisterRequest {

    @NotBlank(message = "公司名称不能为空")
    private String companyName;

    @NotBlank(message = "负责人姓名不能为空")
    private String contactName;

    @NotBlank(message = "负责人手机号不能为空")
    private String contactPhone;

    @Email(message = "负责人邮箱格式不正确")
    @NotBlank(message = "负责人邮箱不能为空")
    private String contactEmail;

    @NotBlank(message = "管理员密码不能为空")
    private String password;

    @NotNull(message = "套餐类型不能为空")
    private Integer planType;
}
