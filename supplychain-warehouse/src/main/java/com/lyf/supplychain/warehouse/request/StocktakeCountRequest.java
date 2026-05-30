package com.lyf.supplychain.warehouse.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 实盘数据提交请求。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Data
public class StocktakeCountRequest {

    private Long pickerId;
    @Valid
    @NotEmpty(message = "实盘明细不能为空")
    private List<WmsItemRequest> items;
}
