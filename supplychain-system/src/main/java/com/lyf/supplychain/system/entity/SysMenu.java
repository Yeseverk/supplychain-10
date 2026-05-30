package com.lyf.supplychain.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lyf.supplychain.common.entity.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 菜单权限实体。
 *
 * @author liyunfei
 * @date 2026-05-15
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
public class SysMenu extends BaseEntity {

    private Long parentId;

    @NotBlank(message = "菜单名称不能为空")
    private String menuName;

    @NotNull(message = "菜单类型不能为空")
    private Integer menuType;

    private String permission;

    private String path;

    private String component;

    private String icon;

    private Integer sort;

    private Integer isVisible;

    private Integer status;

    @TableField(exist = false)
    private Long tenantId;

    @TableField(exist = false)
    private Integer version;
}
