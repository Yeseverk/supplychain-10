package com.lyf.supplychain.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 套餐功能开关实体。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
@TableName("sys_plan_feature")
public class SysPlanFeature {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @NotNull(message = "套餐类型不能为空")
    private Integer planType;

    @NotBlank(message = "功能编码不能为空")
    private String featureCode;

    @NotBlank(message = "功能名称不能为空")
    private String featureName;

    private Integer isEnabled;

    private Integer limitValue;

    private String limitUnit;
}
