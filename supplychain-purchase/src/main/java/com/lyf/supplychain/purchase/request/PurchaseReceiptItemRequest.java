package com.lyf.supplychain.purchase.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 收货单明细请求。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Data
public class PurchaseReceiptItemRequest {

    @NotNull(message = "采购明细ID不能为空")
    private Long poItemId;

    @NotNull(message = "应到数量不能为空")
    private Integer expectedQty;

    @NotNull(message = "实收数量不能为空")
    private Integer actualQty;

    private Integer passQty;

    private Integer rejectQty;

    private String rejectReason;

    private Long locationId;
}
