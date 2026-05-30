package com.lyf.supplychain.finance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * BI KPI 阈值实体。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
@TableName("bi_kpi_threshold")
public class BiKpiThreshold {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long tenantId;
    private String kpiCode;
    private String kpiName;
    private BigDecimal warningValue;
    private BigDecimal dangerValue;
    private Integer compareType;
    private String notifyRoles;
    private Integer isEnabled;
    private LocalDateTime createTime;
}
