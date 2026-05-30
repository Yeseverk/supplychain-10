package com.lyf.supplychain.common.feign.purchase;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 采购申请创建请求。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Data
public class PurchaseRequisitionCreateRequest {

    private Integer reqSource;
    private String title;
    private Long warehouseId;
    private LocalDate expectDate;
    private BigDecimal totalAmount;
    private Integer priority;
    private Long applyUserId;
    private String applyUserName;
    private String remark;
    private List<PurchaseRequisitionCreateItemRequest> items;
}
