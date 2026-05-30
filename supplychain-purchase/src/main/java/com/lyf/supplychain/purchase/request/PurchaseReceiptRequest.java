package com.lyf.supplychain.purchase.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 采购收货单创建请求。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Data
public class PurchaseReceiptRequest {

    @NotNull(message = "采购单ID不能为空")
    private Long poId;

    @NotNull(message = "收货日期不能为空")
    private LocalDate receiveDate;

    @NotNull(message = "收货人ID不能为空")
    private Long receiverId;

    @NotBlank(message = "收货人姓名不能为空")
    private String receiverName;

    private String remark;

    @Valid
    @NotEmpty(message = "收货明细不能为空")
    private List<PurchaseReceiptItemRequest> items;
}
