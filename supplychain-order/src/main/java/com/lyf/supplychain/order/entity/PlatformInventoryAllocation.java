package com.lyf.supplychain.order.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lyf.supplychain.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 多平台库存分配实体。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("inventory_platform_allocation")
public class PlatformInventoryAllocation extends BaseEntity {

    private Long skuId;

    private String skuCode;

    private String platform;

    private Long storeId;

    private Integer allocatedQty;

    private Integer frozenQty;

    private Integer availableQty;

    private Integer soldQty;

    private Integer allocationRatio;

    private Integer status;

    private Integer lastSyncStatus;

    private LocalDateTime lastSyncTime;

    private String lastSyncMessage;
}
