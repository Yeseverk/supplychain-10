package com.lyf.supplychain.supplier.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lyf.supplychain.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 供应商品类风险事件实体。
 *
 * @author liyunfei
 * @date 2026-05-18
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("supplier_risk_event")
public class SupplierRiskEvent extends BaseEntity {

    private Long categoryId;

    private String riskType;

    private String riskLevel;

    private String riskReason;

    private String systemSuggestion;

    private Integer supplierCount;

    private String bestGrade;

    private Integer status;

    private LocalDate firstDetectedDate;

    private LocalDate lastDetectedDate;

    private LocalDateTime resolvedTime;
}
