package com.lyf.supplychain.logistics.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 退货运单创建请求。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
public class ReturnRequest {

    private Long originalWaybillId;
    @NotNull(message = "订单不能为空")
    private Long orderId;
    private Long refundId;
    @NotNull(message = "退货类型不能为空")
    private Integer returnType;
    private Long carrierId;
    private String returnTrackingNo;
    private String fromCountry;
    private Long toWarehouseId;
    private LocalDate expectedArriveDate;
    private String remark;
}
