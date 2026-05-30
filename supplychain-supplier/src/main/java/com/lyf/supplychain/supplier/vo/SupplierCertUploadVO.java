package com.lyf.supplychain.supplier.vo;

import lombok.Data;

/**
 * 供应商资质文件上传结果。
 *
 * @author liyunfei
 * @date 2026-05-16
 */
@Data
public class SupplierCertUploadVO {

    private Long id;

    private String fileName;

    private String fileUrl;

    private Long fileSize;

    private String fileType;
}
