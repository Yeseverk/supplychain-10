package com.lyf.supplychain.product.request;

import lombok.Data;

import java.util.List;

/**
 * 商品 AI 翻译请求。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
public class ProductTranslateRequest {

    private List<String> targetLangCodes;
}
