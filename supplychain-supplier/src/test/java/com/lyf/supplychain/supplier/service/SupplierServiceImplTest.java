package com.lyf.supplychain.supplier.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.common.context.TenantContext;
import com.lyf.supplychain.common.exception.BusinessException;
import com.lyf.supplychain.common.oss.template.OssTemplate;
import com.lyf.supplychain.supplier.constant.SupplierErrorCode;
import com.lyf.supplychain.supplier.constant.SupplierStatus;
import com.lyf.supplychain.supplier.entity.Supplier;
import com.lyf.supplychain.supplier.entity.SupplierAuditLog;
import com.lyf.supplychain.supplier.entity.SupplierCert;
import com.lyf.supplychain.supplier.entity.SupplierContact;
import com.lyf.supplychain.supplier.entity.SupplierPortalUser;
import com.lyf.supplychain.supplier.entity.SupplierScoreLog;
import com.lyf.supplychain.supplier.mapper.SupplierAuditLogMapper;
import com.lyf.supplychain.supplier.mapper.SupplierCertMapper;
import com.lyf.supplychain.supplier.mapper.SupplierContactMapper;
import com.lyf.supplychain.supplier.mapper.SupplierMapper;
import com.lyf.supplychain.supplier.mapper.SupplierPortalUserMapper;
import com.lyf.supplychain.supplier.mapper.SupplierScoreLogMapper;
import com.lyf.supplychain.supplier.request.SupplierCertUploadRequest;
import com.lyf.supplychain.supplier.request.SupplierCreateRequest;
import com.lyf.supplychain.supplier.request.SupplierAuditRequest;
import com.lyf.supplychain.supplier.request.SupplierPageQuery;
import com.lyf.supplychain.supplier.request.SupplierUpdateRequest;
import com.lyf.supplychain.supplier.service.impl.SupplierServiceImpl;
import com.lyf.supplychain.supplier.vo.SupplierDetailVO;
import com.lyf.supplychain.supplier.vo.SupplierListVO;
import com.lyf.supplychain.supplier.vo.SupplierCertUploadVO;
import com.aliyun.oss.model.ObjectMetadata;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 供应商服务实现测试。
 *
 * @author liyunfei
 * @date 2026-05-16
 */
@ExtendWith(MockitoExtension.class)
class SupplierServiceImplTest {

    @Mock
    private SupplierMapper supplierMapper;

    @Mock
    private SupplierCertMapper certMapper;

    @Mock
    private SupplierContactMapper contactMapper;

    @Mock
    private SupplierScoreLogMapper scoreLogMapper;

    @Mock
    private SupplierAuditLogMapper auditLogMapper;

    @Mock
    private SupplierPortalUserMapper portalUserMapper;

    @Mock
    private SupplierCodeGenerator codeGenerator;

    @Mock
    private OssTemplate ossTemplate;

    @Mock
    private NotificationService notificationService;

    private SupplierServiceImpl supplierService;

    @BeforeEach
    void setUp() {
        Executor directExecutor = Runnable::run;
        supplierService = new SupplierServiceImpl(
                supplierMapper,
                certMapper,
                contactMapper,
                scoreLogMapper,
                auditLogMapper,
                portalUserMapper,
                codeGenerator,
                Optional.of(ossTemplate),
                directExecutor,
                notificationService
        );
        TenantContext.set(0L, 100L);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void pageShouldMaskPhoneAndMarkCertExpireWarning() {
        Supplier supplier = new Supplier();
        supplier.setId(1L);
        supplier.setSupplierCode("SUP-20260516-0001");
        supplier.setSupplierName("广州市测试电子有限公司");
        supplier.setSupplierType(1);
        supplier.setContactName("张经理");
        supplier.setContactPhone("13812345678");
        supplier.setProvince("广东省");
        supplier.setCity("广州市");
        supplier.setGrade("A");
        supplier.setScore(new BigDecimal("88.50"));
        supplier.setStatus(SupplierStatus.APPROVED.getCode());
        supplier.setMoq(100);
        supplier.setLeadTimeDays(7);
        supplier.setCreateTime(LocalDateTime.of(2026, 5, 16, 10, 0));
        Page<Supplier> page = new Page<>(1, 10, 1);
        page.setRecords(List.of(supplier));
        when(supplierMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);
        SupplierCert warningCert = new SupplierCert();
        warningCert.setSupplierId(1L);
        when(certMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(warningCert));

        PageResult<SupplierListVO> result = supplierService.pageSuppliers(new SupplierPageQuery());

        assertThat(result.getTotal()).isEqualTo(1);
        SupplierListVO record = result.getRecords().get(0);
        assertThat(record.getContactPhone()).isEqualTo("138****5678");
        assertThat(record.getSupplierTypeName()).isEqualTo("工厂供应商");
        assertThat(record.getStatusName()).isEqualTo("已通过");
        assertThat(record.getCertExpireWarning()).isTrue();
    }

    @Test
    void detailShouldCombineMainAndRelationData() {
        Supplier supplier = new Supplier();
        supplier.setId(1L);
        supplier.setSupplierName("广州市测试电子有限公司");
        when(supplierMapper.selectById(1L)).thenReturn(supplier);
        when(certMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(new SupplierCert()));
        when(contactMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(new SupplierContact()));
        when(scoreLogMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(new SupplierScoreLog()));
        when(auditLogMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(new SupplierAuditLog()));

        SupplierDetailVO detail = supplierService.detail(1L);

        assertThat(detail.getSupplierName()).isEqualTo("广州市测试电子有限公司");
        assertThat(detail.getCerts()).hasSize(1);
        assertThat(detail.getContacts()).hasSize(1);
        assertThat(detail.getScoreLogs()).hasSize(1);
        assertThat(detail.getAuditLogs()).hasSize(1);
    }

    @Test
    void createShouldRejectDuplicateSupplierName() {
        when(supplierMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        assertThatThrownBy(() -> supplierService.create(validCreateRequest()))
                .isInstanceOf(BusinessException.class)
                .hasMessage(SupplierErrorCode.SUPPLIER_NAME_EXISTS.getMessage());
    }

    @Test
    void updateShouldRejectPendingAuditSupplier() {
        Supplier supplier = new Supplier();
        supplier.setId(1L);
        supplier.setStatus(SupplierStatus.PENDING_AUDIT.getCode());
        when(supplierMapper.selectById(1L)).thenReturn(supplier);

        SupplierUpdateRequest request = new SupplierUpdateRequest();
        request.setVersion(1);

        assertThatThrownBy(() -> supplierService.update(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("审核进行中，请等待审核结果");
    }

    @Test
    void updateShouldUseCasVersion() {
        Supplier supplier = new Supplier();
        supplier.setId(1L);
        supplier.setSupplierName("旧供应商");
        supplier.setContactEmail("old@example.com");
        supplier.setStatus(SupplierStatus.DRAFT.getCode());
        when(supplierMapper.selectById(1L)).thenReturn(supplier);
        when(supplierMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(supplierMapper.update(any(Supplier.class), any(LambdaUpdateWrapper.class))).thenReturn(0);

        SupplierUpdateRequest request = new SupplierUpdateRequest();
        request.setSupplierName("新供应商");
        request.setSupplierType(1);
        request.setContactName("张经理");
        request.setContactPhone("13812345678");
        request.setContactEmail("new@example.com");
        request.setVersion(3);

        assertThatThrownBy(() -> supplierService.update(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("数据已被他人修改，请刷新后重试");
    }

    @Test
    void deleteShouldRejectPendingAuditSupplier() {
        Supplier supplier = new Supplier();
        supplier.setId(1L);
        supplier.setStatus(SupplierStatus.PENDING_AUDIT.getCode());
        when(supplierMapper.selectById(1L)).thenReturn(supplier);

        assertThatThrownBy(() -> supplierService.delete(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("审核进行中，请等待审核结果");
    }

    @Test
    void deleteShouldUseLogicDelete() {
        Supplier supplier = new Supplier();
        supplier.setId(1L);
        supplier.setStatus(SupplierStatus.DRAFT.getCode());
        when(supplierMapper.selectById(1L)).thenReturn(supplier);
        when(supplierMapper.update(any(Supplier.class), any(LambdaUpdateWrapper.class))).thenReturn(1);

        supplierService.delete(1L);

        verify(supplierMapper).update(any(Supplier.class), any(LambdaUpdateWrapper.class));
    }

    @Test
    void submitAuditShouldRequireBusinessLicense() {
        Supplier supplier = pendingRequiredSupplier(SupplierStatus.DRAFT.getCode());
        when(supplierMapper.selectById(1L)).thenReturn(supplier);
        when(certMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        assertThatThrownBy(() -> supplierService.submitAudit(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage(SupplierErrorCode.SUPPLIER_BUSINESS_LICENSE_REQUIRED.getMessage());
    }

    @Test
    void submitAuditShouldMoveDraftToPendingAndWriteLog() {
        Supplier supplier = pendingRequiredSupplier(SupplierStatus.DRAFT.getCode());
        when(supplierMapper.selectById(1L)).thenReturn(supplier);
        when(certMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);
        when(supplierMapper.update(any(Supplier.class), any(LambdaUpdateWrapper.class))).thenReturn(1);

        supplierService.submitAudit(1L);

        verify(supplierMapper).update(any(Supplier.class), any(LambdaUpdateWrapper.class));
        verify(auditLogMapper).insert(any(SupplierAuditLog.class));
    }

    @Test
    void approveShouldReturnSuccessWhenAlreadyApproved() {
        Supplier supplier = pendingRequiredSupplier(SupplierStatus.APPROVED.getCode());
        supplier.setPortalUserId(1L);
        when(supplierMapper.selectById(1L)).thenReturn(supplier);

        supplierService.approve(1L, new SupplierAuditRequest());

        verify(supplierMapper, never()).update(any(Supplier.class), any(LambdaUpdateWrapper.class));
        verify(auditLogMapper, never()).insert(any(SupplierAuditLog.class));
    }

    @Test
    void approveShouldUsePendingStatusConditionAndWriteLog() {
        Supplier supplier = pendingRequiredSupplier(SupplierStatus.PENDING_AUDIT.getCode());
        when(supplierMapper.selectById(1L)).thenReturn(supplier);
        when(supplierMapper.update(any(Supplier.class), any(LambdaUpdateWrapper.class))).thenReturn(1);
        when(portalUserMapper.insert(any(SupplierPortalUser.class))).thenReturn(1);

        supplierService.approve(1L, new SupplierAuditRequest());

        verify(supplierMapper).update(any(Supplier.class), any(LambdaUpdateWrapper.class));
        verify(portalUserMapper).insert(any(SupplierPortalUser.class));
        verify(auditLogMapper).insert(any(SupplierAuditLog.class));
    }

    @Test
    void approveShouldRejectSelfAudit() {
        Supplier supplier = pendingRequiredSupplier(SupplierStatus.PENDING_AUDIT.getCode());
        supplier.setCreateBy(100L);
        when(supplierMapper.selectById(1L)).thenReturn(supplier);

        assertThatThrownBy(() -> supplierService.approve(1L, new SupplierAuditRequest()))
                .isInstanceOf(BusinessException.class)
                .hasMessage(SupplierErrorCode.SUPPLIER_SELF_AUDIT_NOT_ALLOWED.getMessage());
    }

    @Test
    void rejectShouldRequireRemark() {
        SupplierAuditRequest request = new SupplierAuditRequest();

        assertThatThrownBy(() -> supplierService.reject(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(SupplierErrorCode.SUPPLIER_AUDIT_REMARK_REQUIRED.getMessage());
    }

    @Test
    void requestSupplementShouldMovePendingToDraftAndWriteLog() {
        Supplier supplier = pendingRequiredSupplier(SupplierStatus.PENDING_AUDIT.getCode());
        when(supplierMapper.selectById(1L)).thenReturn(supplier);
        when(supplierMapper.update(any(Supplier.class), any(LambdaUpdateWrapper.class))).thenReturn(1);
        SupplierAuditRequest request = new SupplierAuditRequest();
        request.setAuditRemark("请补充营业执照原件扫描件");

        supplierService.requestSupplement(1L, request);

        verify(supplierMapper).update(any(Supplier.class), any(LambdaUpdateWrapper.class));
        verify(auditLogMapper).insert(any(SupplierAuditLog.class));
    }

    @Test
    void uploadCertShouldValidateSizeLimit() {
        MockMultipartFile file = new MockMultipartFile("file", "license.pdf", "application/pdf", new byte[5 * 1024 * 1024 + 1]);
        SupplierCertUploadRequest request = validCertUploadRequest(file);

        assertThatThrownBy(() -> supplierService.uploadCert(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("文件大小不能超过5MB");
    }

    @Test
    void uploadCertShouldValidateMimeType() {
        MockMultipartFile file = new MockMultipartFile("file", "license.txt", "text/plain", "hello".getBytes());
        SupplierCertUploadRequest request = validCertUploadRequest(file);

        assertThatThrownBy(() -> supplierService.uploadCert(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(SupplierErrorCode.SUPPLIER_CERT_FILE_TYPE_NOT_ALLOWED.getMessage());
    }

    @Test
    void uploadCertShouldValidateMagicBytes() {
        MockMultipartFile file = new MockMultipartFile("file", "license.pdf", "application/pdf", "not pdf".getBytes());
        SupplierCertUploadRequest request = validCertUploadRequest(file);

        assertThatThrownBy(() -> supplierService.uploadCert(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(SupplierErrorCode.SUPPLIER_CERT_FILE_CONTENT_MISMATCH.getMessage());
    }

    @Test
    void uploadCertShouldUploadToOssAndInsertSupplierCert() {
        Supplier supplier = pendingRequiredSupplier(SupplierStatus.DRAFT.getCode());
        byte[] pngBytes = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x01};
        MockMultipartFile file = new MockMultipartFile("file", "license.png", "image/png", pngBytes);
        SupplierCertUploadRequest request = validCertUploadRequest(file);
        when(supplierMapper.selectById(1L)).thenReturn(supplier);
        when(ossTemplate.buildDirectoryObjectName(0L, "supplier_cert", "license.png")).thenReturn("0/supplier_cert/2026-05-16/abc.png");
        when(ossTemplate.uploadAndReturnUrl(any(String.class), any(InputStream.class), any(ObjectMetadata.class)))
                .thenReturn("https://static.example.com/0/supplier_cert/2026-05-16/abc.png");
        when(certMapper.insert(any(SupplierCert.class))).thenAnswer(invocation -> {
            SupplierCert cert = invocation.getArgument(0);
            cert.setId(10L);
            return 1;
        });

        SupplierCertUploadVO result = supplierService.uploadCert(1L, request);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getFileName()).isEqualTo("license.png");
        assertThat(result.getFileType()).isEqualTo("image/png");
        assertThat(result.getFileUrl()).isEqualTo("https://static.example.com/0/supplier_cert/2026-05-16/abc.png");
        verify(certMapper).insert(any(SupplierCert.class));
    }

    private SupplierCreateRequest validCreateRequest() {
        SupplierCreateRequest request = new SupplierCreateRequest();
        request.setSupplierName("广州市测试电子有限公司");
        request.setSupplierType(1);
        request.setContactName("张经理");
        request.setContactPhone("13812345678");
        request.setContactEmail("test@example.com");
        request.setMoq(100);
        request.setLeadTimeDays(7);
        return request;
    }

    private SupplierCertUploadRequest validCertUploadRequest(MockMultipartFile file) {
        SupplierCertUploadRequest request = new SupplierCertUploadRequest();
        request.setFile(file);
        request.setCertType(1);
        request.setCertName("营业执照");
        request.setCertNo("91440101MA5TEST");
        request.setRemark("测试上传");
        return request;
    }

    private Supplier pendingRequiredSupplier(Integer status) {
        Supplier supplier = new Supplier();
        supplier.setId(1L);
        supplier.setTenantId(0L);
        supplier.setSupplierName("广州市测试电子有限公司");
        supplier.setSupplierType(1);
        supplier.setContactName("张经理");
        supplier.setContactPhone("13812345678");
        supplier.setContactEmail("test@example.com");
        supplier.setStatus(status);
        supplier.setVersion(1);
        return supplier;
    }
}
