package com.lyf.supplychain.finance.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * AI 自然语言查询请求。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
public class AiQueryRequest {

    @NotBlank(message = "问题不能为空")
    private String question;
}
