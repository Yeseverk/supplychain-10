package com.lyf.supplychain.supplier.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

/**
 * 供应商资质文件上传请求。
 *
 * @author liyunfei
 * @date 2026-05-16
 */
@Data
public class SupplierCertUploadRequest {

    private MultipartFile file;

    private Integer certType;

    private String certName;

    private LocalDate issueDate;

    private LocalDate expireDate;

    private String certNo;

    private String remark;
}
