package com.lyf.supplychain.finance.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * VAT 生成请求。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
public class VatGenerateRequest {

    @NotBlank(message = "申报期不能为空")
    private String period;

    private String countryCode;
}
