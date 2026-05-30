package com.lyf.supplychain.purchase.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 采购申请保存请求。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Data
public class PurchaseRequisitionRequest {

    private Integer reqSource;

    @NotBlank(message = "申请标题不能为空")
    private String title;

    @NotNull(message = "仓库ID不能为空")
    private Long warehouseId;

    private LocalDate expectDate;

    private BigDecimal totalAmount;

    private Integer priority;

    @NotNull(message = "申请人ID不能为空")
    private Long applyUserId;

    @NotBlank(message = "申请人姓名不能为空")
    private String applyUserName;

    private String remark;

    @Valid
    @NotEmpty(message = "采购申请明细不能为空")
    private List<PurchaseItemRequest> items;
}
