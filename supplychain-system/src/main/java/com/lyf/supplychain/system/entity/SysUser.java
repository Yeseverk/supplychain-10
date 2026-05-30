package com.lyf.supplychain.system.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lyf.supplychain.common.entity.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 系统用户实体。
 *
 * @author liyunfei
 * @date 2026-05-15
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    @NotBlank(message = "登录用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    private String realName;

    private String avatar;

    private String email;

    private String phone;

    private Integer userType;

    private Integer status;

    private Integer loginFailCount;

    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private LocalDateTime lockTime;

    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private LocalDateTime lockUntilTime;

    private LocalDateTime lastLoginTime;

    private String lastLoginIp;
}
