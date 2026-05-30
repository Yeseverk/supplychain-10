package com.lyf.supplychain.warehouse.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 入库确认请求。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Data
public class InboundConfirmRequest {

    private LocalDate actualDate;
    private Long operatorId;
    private String operatorName;
    @Valid
    @NotEmpty(message = "确认明细不能为空")
    private List<WmsItemRequest> items;
}
