package com.lyf.supplychain.supplier.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lyf.supplychain.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 供应商重点观察名单实体。
 *
 * @author liyunfei
 * @date 2026-05-18
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("supplier_watchlist")
public class SupplierWatchlist extends BaseEntity {

    private Long supplierId;

    private String currentGrade;

    private BigDecimal currentScore;

    private String watchReason;

    private String systemSuggestion;

    private Integer status;

    private LocalDateTime watchTime;
}
