package com.lyf.supplychain.common.feign.supplier;

import lombok.Data;

/**
 * 供应商简要信息响应。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Data
public class SupplierBriefResponse {

    private Long id;

    private String supplierCode;

    private String supplierName;

    private String grade;

    private Integer status;
}
