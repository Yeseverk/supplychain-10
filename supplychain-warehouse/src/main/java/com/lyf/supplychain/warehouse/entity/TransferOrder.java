package com.lyf.supplychain.warehouse.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lyf.supplychain.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 仓库调拨单实体。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("transfer_order")
public class TransferOrder extends BaseEntity {

    private String transferNo;
    private Long fromWarehouseId;
    private String fromWarehouseName;
    private Long toWarehouseId;
    private String toWarehouseName;
    private String transferReason;
    private String logisticsCompany;
    private String trackingNo;
    private LocalDate planDate;
    private LocalDate shipDate;
    private LocalDate arriveDate;
    private Integer status;
    private String remark;
}
