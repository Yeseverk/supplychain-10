package com.lyf.supplychain.supplier.controller;

import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.common.handler.GlobalExceptionHandler;
import com.lyf.supplychain.supplier.service.SupplierService;
import com.lyf.supplychain.supplier.vo.SupplierCertUploadVO;
import com.lyf.supplychain.supplier.vo.SupplierListVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

/**
 * 供应商控制器测试。
 *
 * @author liyunfei
 * @date 2026-05-16
 */
@ExtendWith(MockitoExtension.class)
class SupplierControllerTest {

    @Mock
    private SupplierService supplierService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(new SupplierController(supplierService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void pageShouldReturnUnifiedResponse() throws Exception {
        SupplierListVO supplier = new SupplierListVO();
        supplier.setSupplierCode("SUP-20260516-0001");
        supplier.setSupplierName("广州市测试电子有限公司");
        supplier.setContactPhone("138****5678");
        PageResult<SupplierListVO> pageResult = new PageResult<>();
        pageResult.setPageNum(1L);
        pageResult.setPageSize(10L);
        pageResult.setTotal(1L);
        pageResult.setPages(1L);
        pageResult.setRecords(List.of(supplier));
        when(supplierService.pageSuppliers(any())).thenReturn(pageResult);

        mockMvc.perform(get("/api/srm/suppliers")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records[0].supplierCode").value("SUP-20260516-0001"))
                .andExpect(jsonPath("$.data.records[0].contactPhone").value("138****5678"));
    }

    @Test
    void approveShouldReturnUnifiedResponse() throws Exception {
        mockMvc.perform(put("/api/srm/suppliers/1/approve")
                        .contentType("application/json")
                        .content("{\"auditRemark\":\"资质齐全，审核通过\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(supplierService).approve(eq(1L), any());
    }

    @Test
    void uploadCertShouldReturnUnifiedResponse() throws Exception {
        SupplierCertUploadVO uploadVO = new SupplierCertUploadVO();
        uploadVO.setId(10L);
        uploadVO.setFileName("license.png");
        uploadVO.setFileUrl("https://static.example.com/0/supplier_cert/2026-05-16/abc.png");
        uploadVO.setFileType("image/png");
        when(supplierService.uploadCert(eq(1L), any())).thenReturn(uploadVO);

        mockMvc.perform(multipart("/api/srm/suppliers/1/certs")
                        .file("file", new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47})
                        .param("certType", "1")
                        .param("certName", "营业执照"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.fileUrl").value("https://static.example.com/0/supplier_cert/2026-05-16/abc.png"));

        verify(supplierService).uploadCert(eq(1L), any());
    }
}
