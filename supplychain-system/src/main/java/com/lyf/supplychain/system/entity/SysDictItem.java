package com.lyf.supplychain.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lyf.supplychain.common.entity.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据字典明细实体。
 *
 * @author liyunfei
 * @date 2026-05-15
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_item")
public class SysDictItem extends BaseEntity {

    @NotNull(message = "字典类型ID不能为空")
    private Long dictTypeId;

    @NotBlank(message = "字典编码不能为空")
    private String dictCode;

    @NotBlank(message = "字典项值不能为空")
    private String itemValue;

    @NotBlank(message = "字典项标签不能为空")
    private String itemLabel;

    private String itemLabelEn;

    private Integer sort;

    private String cssClass;

    private Integer status;

    private String remark;

    @TableField(exist = false)
    private Long tenantId;

    @TableField(exist = false)
    private Long createBy;

    @TableField(exist = false)
    private Long updateBy;

    @TableField(exist = false)
    private Integer version;
}
