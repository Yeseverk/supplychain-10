package com.lyf.supplychain.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lyf.supplychain.common.entity.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 租户信息实体。
 *
 * @author liyunfei
 * @date 2026-05-15
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_tenant")
public class SysTenant extends BaseEntity {

    @NotBlank(message = "租户编码不能为空")
    private String tenantCode;

    @NotBlank(message = "公司名称不能为空")
    private String companyName;

    @NotBlank(message = "负责人姓名不能为空")
    private String contactName;

    @NotBlank(message = "负责人手机号不能为空")
    private String contactPhone;

    @NotBlank(message = "负责人邮箱不能为空")
    private String contactEmail;

    private Integer planType;

    private LocalDateTime planStartTime;

    private LocalDateTime planEndTime;

    private LocalDateTime trialEndTime;

    private Integer status;

    private Integer maxSupplier;

    private Integer maxWarehouse;

    private Integer maxMonthlyOrder;

    private Integer maxPlatform;

    private Long adminUserId;

    private String remark;

    @TableField(exist = false)
    private Long tenantId;
}
