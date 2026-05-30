package com.lyf.supplychain.supplier.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lyf.supplychain.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 供应商 Portal 登录用户实体。
 *
 * @author liyunfei
 * @date 2026-05-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SupplierPortalUser extends BaseEntity {

    private String username;

    private String password;

    private String realName;

    private String avatar;

    private String email;

    private String phone;

    private Integer userType;

    private Integer status;

    private Integer loginFailCount;

    private LocalDateTime lastLoginTime;

    private String lastLoginIp;
}
