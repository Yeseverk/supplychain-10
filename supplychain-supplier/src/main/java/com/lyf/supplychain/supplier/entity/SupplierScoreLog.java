package com.lyf.supplychain.supplier.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 供应商月度评分记录实体。
 *
 * @author liyunfei
 * @date 2026-05-16
 */
@Data
@TableName("supplier_score_log")
public class SupplierScoreLog {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long tenantId;

    private Long supplierId;

    private String scoreMonth;

    private Integer totalOrders;

    private Integer deliveredOnTime;

    private Integer qualityPassed;

    private Integer qualityTotal;

    private BigDecimal responseHoursAvg;

    private BigDecimal priceComparison;

    private BigDecimal deliveryScore;

    private BigDecimal qualityScore;

    private BigDecimal responseScore;

    private BigDecimal priceScore;

    private BigDecimal totalScore;

    private String grade;

    private Integer gradeChanged;

    private String prevGrade;

    private String calcRemark;

    private LocalDateTime calcTime;
}
