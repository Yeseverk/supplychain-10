package com.lyf.supplychain.supplier.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.DesensitizedUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.common.constant.ResultCode;
import com.lyf.supplychain.common.context.TenantContext;
import com.lyf.supplychain.common.exception.BusinessException;
import com.lyf.supplychain.common.oss.template.OssTemplate;
import com.lyf.supplychain.common.security.annotation.DataScope;
import com.lyf.supplychain.common.security.datascope.DataScopeQueryHelper;
import com.lyf.supplychain.common.security.datascope.DataScopeResource;
import com.lyf.supplychain.supplier.constant.SupplierAuditAction;
import com.lyf.supplychain.supplier.constant.SupplierErrorCode;
import com.lyf.supplychain.supplier.constant.NotificationConstants;
import com.lyf.supplychain.supplier.constant.SupplierStateMachine;
import com.lyf.supplychain.supplier.constant.SupplierStatus;
import com.lyf.supplychain.supplier.constant.SupplierType;
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
import com.lyf.supplychain.supplier.model.NotificationCommand;
import com.lyf.supplychain.supplier.service.NotificationService;
import com.lyf.supplychain.supplier.request.SupplierAuditRequest;
import com.lyf.supplychain.supplier.request.SupplierCertUploadRequest;
import com.lyf.supplychain.supplier.request.SupplierCreateRequest;
import com.lyf.supplychain.supplier.request.SupplierPageQuery;
import com.lyf.supplychain.supplier.request.SupplierUpdateRequest;
import com.lyf.supplychain.supplier.service.SupplierCodeGenerator;
import com.lyf.supplychain.supplier.service.SupplierService;
import com.lyf.supplychain.supplier.vo.SupplierCertUploadVO;
import com.lyf.supplychain.supplier.vo.SupplierDetailVO;
import com.lyf.supplychain.supplier.vo.SupplierListVO;
import com.aliyun.oss.model.ObjectMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * 供应商信息管理服务实现。
 *
 * @author liyunfei
 * @date 2026-05-16
 */
@Slf4j
@Service
public class SupplierServiceImpl extends ServiceImpl<SupplierMapper, Supplier> implements SupplierService {

    private static final int CERT_EXPIRE_WARNING_DAYS = 30;

    private static final String CREATE_ACTION = "创建供应商";

    private static final String UPDATE_ACTION = "编辑供应商";

    private static final String DELETE_ACTION = "删除供应商";

    private static final int BUSINESS_LICENSE_CERT_TYPE = 1;

    private static final long MAX_CERT_FILE_SIZE = 5L * 1024 * 1024;

    private static final String SUPPLIER_CERT_BIZ_TYPE = "supplier_cert";

    private static final Set<String> ALLOWED_CERT_MIME_TYPES = Set.of("image/jpeg", "image/png", "application/pdf");

    private static final Map<String, Set<String>> CERT_MIME_EXTENSIONS = Map.of(
            "image/jpeg", Set.of("jpg", "jpeg"),
            "image/png", Set.of("png"),
            "application/pdf", Set.of("pdf")
    );

    private static final int PORTAL_USER_TYPE = 3;

    private static final int USER_STATUS_NORMAL = 1;

    private static final String DEFAULT_PORTAL_PASSWORD = "Supplychain@123456";

    private final SupplierMapper supplierMapper;

    private final SupplierCertMapper certMapper;

    private final SupplierContactMapper contactMapper;

    private final SupplierScoreLogMapper scoreLogMapper;

    private final SupplierAuditLogMapper auditLogMapper;

    private final SupplierPortalUserMapper portalUserMapper;

    private final SupplierCodeGenerator codeGenerator;

    private final OssTemplate ossTemplate;

    private final Executor supplierTaskExecutor;

    private final NotificationService notificationService;

    public SupplierServiceImpl(SupplierMapper supplierMapper,
                               SupplierCertMapper certMapper,
                               SupplierContactMapper contactMapper,
                               SupplierScoreLogMapper scoreLogMapper,
                               SupplierAuditLogMapper auditLogMapper,
                               SupplierPortalUserMapper portalUserMapper,
                               SupplierCodeGenerator codeGenerator,
                               Optional<OssTemplate> ossTemplate,
                               @Qualifier("supplierTaskExecutor") Executor supplierTaskExecutor,
                               NotificationService notificationService) {
        this.supplierMapper = supplierMapper;
        this.certMapper = certMapper;
        this.contactMapper = contactMapper;
        this.scoreLogMapper = scoreLogMapper;
        this.auditLogMapper = auditLogMapper;
        this.portalUserMapper = portalUserMapper;
        this.codeGenerator = codeGenerator;
        this.ossTemplate = ossTemplate.orElse(null);
        this.supplierTaskExecutor = supplierTaskExecutor;
        this.notificationService = notificationService;
    }

    /**
     * 供应商列表查询
     *
     * @param query 查询参数
     * @return
     */
    @Override
    @DataScope(resource = DataScopeResource.SUPPLIER)
    public PageResult<SupplierListVO> pageSuppliers(SupplierPageQuery query) {
        // 校验分页参数
        query.normalize();
        // 动态条件拼接 buildPageWrapper
        Page<Supplier> page = supplierMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), buildPageWrapper(query));
        // 把外界传递过来的条件 动态拼接完成后 查询得到结果集
        // 再把这些结果集重新进行 查询 筛选出 已经过期的一些供应商的ID
        Set<Long> warningSupplierIds = queryCertExpireWarningSupplierIds(page.getRecords());
        Page<SupplierListVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        // 给VO赋值
        // 从数据库中得到的时候 Supplier  我们要返回的是 SupplierListVO
        // 把实体转成VO 转换的过程中 要对特殊的供应商标记
        voPage.setRecords(page.getRecords().stream()
                .map(supplier -> toListVO(supplier, warningSupplierIds))
                .toList());
        return PageResult.from(voPage);
    }

    /**
     * 供应商详情查询
     *
     * @param id 供应商ID
     * @return
     */
    @Override
    public SupplierDetailVO detail(Long id) {
        // 校验 ID
        if (ObjectUtil.isNull(id) || id <= 0) BusinessException.throwException("参数非法");
        // 查询主表
        Supplier supplier = supplierMapper.selectById(id);
        if (ObjectUtil.isNull(supplier)) {
            throwSupplierException(SupplierErrorCode.SUPPLIER_NOT_FOUND);
        }
        // 在主表查询后 拿到主表的ID 然后开始异步查询每个关联表的数据
        // certFuture 结果集 可能是一个集合 也有可能是异常
        CompletableFuture<List<SupplierCert>> certFuture = asyncQuery("供应商资质", () -> certMapper.selectList(
                new LambdaQueryWrapper<SupplierCert>().eq(SupplierCert::getSupplierId, id)));
        CompletableFuture<List<SupplierContact>> contactFuture = asyncQuery("供应商联系人", () -> contactMapper.selectList(
                new LambdaQueryWrapper<SupplierContact>().eq(SupplierContact::getSupplierId, id)));
        CompletableFuture<List<SupplierScoreLog>> scoreFuture = asyncQuery("供应商评分", () -> scoreLogMapper.selectList(
                new LambdaQueryWrapper<SupplierScoreLog>()
                        .eq(SupplierScoreLog::getSupplierId, id)
                        .orderByDesc(SupplierScoreLog::getScoreMonth)
                        .last("LIMIT 12")));
        CompletableFuture<List<SupplierAuditLog>> auditFuture = asyncQuery("供应商审核日志", () -> auditLogMapper.selectList(
                new LambdaQueryWrapper<SupplierAuditLog>()
                        .eq(SupplierAuditLog::getSupplierId, id)
                        .orderByDesc(SupplierAuditLog::getOperateTime)));
        try {
            // join()  阻塞操作  要在这个地方 等待上面的所有的异步都执行完成
            CompletableFuture.allOf(certFuture, contactFuture, scoreFuture, auditFuture).join();
        } catch (CompletionException exception) {
            log.error("供应商详情关联数据查询失败，supplierId={}", id, exception);
            BusinessException.throwException("供应商关联数据查询失败");
        }

        // 主表 以及 4个 关联表 都已经查询完成
        SupplierDetailVO detail = BeanUtil.copyProperties(supplier, SupplierDetailVO.class);
        detail.setCerts(certFuture.join());
        detail.setContacts(contactFuture.join());
        detail.setScoreLogs(scoreFuture.join());
        detail.setAuditLogs(auditFuture.join());
        return detail;
    }

    /**
     * 新增供应商
     *
     * @param request 新增请求
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(SupplierCreateRequest request) {
        // 参数校验 判断格式和非空
        validateSupplierType(request.getSupplierType());
        // 获取当前登录的租户的ID
        Long tenantId = currentTenantId();
        // 同一个租户下面 供应商名称应该是唯一的
        validateDuplicateSupplierName(tenantId, null, request.getSupplierName());
        validateDuplicateEmail(tenantId, null, request.getContactEmail());
        // 构建实体
        Supplier supplier = buildCreateSupplier(request, tenantId);
        // 执行新增操作
        supplierMapper.insert(supplier);
        // 记录日志
        insertAuditLog(supplier.getId(), null, supplier.getStatus(), CREATE_ACTION, "创建草稿供应商");
        return supplier.getId();
    }

    /**
     * 编辑供应商
     * @param id      供应商ID
     * @param request 编辑请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, SupplierUpdateRequest request) {
        Supplier existing = requireSupplier(id);
        // 状态判断 等待审核是不允许修改
        if (SupplierStatus.PENDING_AUDIT.getCode().equals(existing.getStatus())) {
            throwSupplierStatusException("审核进行中，请等待审核结果");
        }
        // 根据状态 进行参数的校验
        validateUpdateRequest(existing, request);
        // 构建 新的 实体类
        Supplier update = buildUpdateSupplier(existing, request);
        Integer toStatus = existing.getStatus();
        if (SupplierStatus.REJECTED.getCode().equals(existing.getStatus())) {
            ensureTransitionAllowed(existing.getStatus(), SupplierStatus.DRAFT.getCode());
            toStatus = SupplierStatus.DRAFT.getCode();
            update.setStatus(toStatus);
        }
        // CAS 更新
        update.setVersion(request.getVersion() + 1);
        int rows = supplierMapper.update(update, new LambdaUpdateWrapper<Supplier>()
                .eq(Supplier::getId, id)
                .eq(Supplier::getVersion, request.getVersion())); // 对比当前的版本号 CAS
        if (rows == 0) {
            BusinessException.throwException(ResultCode.DATA_VERSION_CONFLICT);
        }
        // 记录操作日志
        insertAuditLog(id, existing.getStatus(), toStatus, UPDATE_ACTION, "编辑供应商信息");
    }

    /**
     * 上传供应商资质文件。
     *
     * @param id 供应商ID
     * @param request 上传请求
     * @return 上传结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SupplierCertUploadVO uploadCert(Long id, SupplierCertUploadRequest request) {
        MultipartFile file = request == null ? null : request.getFile();
        validateCertUploadRequest(request, file);
        byte[] bytes = readFileBytes(file);
        // 校验文件的类型
        String fileType = validateCertFileType(file, bytes);
        Supplier supplier = requireSupplier(id);
        if (ossTemplate == null) {
            throwSupplierException(SupplierErrorCode.SUPPLIER_OSS_NOT_CONFIGURED);
        }

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(fileType);
        // 调用自定义的starter实现OSS的上传功能
        // 租户/业务/UUID的名称
        // objectName 存储在OSS上的一个 路径+文件名称
        String objectName = ossTemplate.buildDirectoryObjectName(currentTenantId(), SUPPLIER_CERT_BIZ_TYPE, file.getOriginalFilename());
        // 实现上传功能 并把上传成功后的路径 返回
        String fileUrl = ossTemplate.uploadAndReturnUrl(objectName, new ByteArrayInputStream(bytes), metadata);
        // 构建了一个资质表的实体类
        SupplierCert cert = buildSupplierCert(supplier, request, file, fileType, fileUrl);
        // 往供货商的资质表中插入数据
        certMapper.insert(cert);
        return toCertUploadVO(cert);
    }

    /**
     * 提交供应商审核。
     *
     * @param id 供应商ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitAudit(Long id) {
        // 根据ID查询供应商 判断这个供应商是否存在
        Supplier existing = requireSupplier(id);
        // 通过状态机来判断 是否可以进行流转
        // 从当前的供应商中获取当前的状态 existing.getStatus()
        // SupplierStatus.PENDING_AUDIT.getCode() 待审核
        // 更新前的判断 也就是只有是草稿的状态 才可以往 待审核流转 也就是说 如果当前不是 草稿 直接抛异常
        ensureTransitionAllowed(existing.getStatus(), SupplierStatus.PENDING_AUDIT.getCode());
        // 保证 提交审核的供应商资料 必须是完整的
        validateSubmitRequired(existing);
        // 判断当前供应商 是否有上传证件
        validateBusinessLicenseUploaded(id);
        // 使用MP进行更新的时候 必须要传递一个Bean
        Supplier update = new Supplier();
        update.setStatus(SupplierStatus.PENDING_AUDIT.getCode());
        int rows = supplierMapper.update(update, new LambdaUpdateWrapper<Supplier>()
                .eq(Supplier::getId, id) // 根据供应商ID进行更新
                .eq(Supplier::getStatus, SupplierStatus.DRAFT.getCode())); // CAS 幂等性保障
        if (rows == 0) {
            throwSupplierStatusException("供应商状态已变化，请刷新后重试");
        }
        // 插入审核日志
        insertAuditLog(id, existing.getStatus(), SupplierStatus.PENDING_AUDIT.getCode(),
                SupplierAuditAction.SUBMIT.getDescription(), "提交供应商审核");
        // 发送通知
        sendRoleNotice(existing.getTenantId(),
                NotificationConstants.ROLE_PURCHASE_MANAGER,
                "供应商待审核提醒",
                "供应商【" + existing.getSupplierName() + "】已提交审核，请及时处理",
                NotificationConstants.BIZ_TYPE_SUPPLIER_AUDIT,
                String.valueOf(id),
                NotificationConstants.PRIORITY_HIGH);
    }

    /**
     * 审核通过供应商。
     *
     * @param id      供应商ID
     * @param request 审核请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long id, SupplierAuditRequest request) {
        // 查询供应商 判断是否存在
        Supplier existing = requireSupplier(id);
        // 当前必须是待审核的状态 才能改成 审核通过
        // 如果已经审核通过 就直接返回
        if (SupplierStatus.APPROVED.getCode().equals(existing.getStatus())) {
            return;
        }
        // 通过状态机来进行判断 只能是待审核 --> 审核通过
        ensureTransitionAllowed(existing.getStatus(), SupplierStatus.APPROVED.getCode());
        // 判断当前登录的用户 不能是该供应商的提交用户 也就是 自己不能审核自己提交的
        validateAuditOperator(existing);
        // 卖家给供应商开通的一个 账号
        SupplierPortalUser portalUser = null;
        // 获取已经存在的供应商账号 如果有 说明该供应商已经被其他的人/事务 已经创建过了
        Long portalUserId = existing.getPortalUserId();
        if (ObjectUtil.isNull(portalUserId)) {
            // 说明还没有被创建
            // 生成雪花算法
            portalUserId = IdWorker.getId();
            // 构建供应商的对象信息
            portalUser = buildPortalUser(existing, portalUserId);
        }
        // 更新供应商的状态 把状态 --> 审核通过
        Supplier update = buildAuditUpdate(SupplierStatus.APPROVED, request);
        update.setPortalEnabled(1); // 代表已经开通了供应商账号
        update.setPortalUserId(portalUserId); // 给供应商开通的账号信息
        // 开始更新
        int rows = updateStatusWithExpected(id, SupplierStatus.PENDING_AUDIT, update);
        if (rows == 0) {
            Supplier latest = requireSupplier(id);
            if (SupplierStatus.APPROVED.getCode().equals(latest.getStatus())) {
                return;
            }
            throwSupplierStatusException("供应商状态已变化，请刷新后重试");
        }
        if (ObjectUtil.isNotNull(portalUser)) {
            // 往sys_user表中新增数据
            int insertRows = portalUserMapper.insert(portalUser);
            if (insertRows == 0) {
                BusinessException.throwException("Portal账号创建失败");
            }
        }
        // 记录审核日志
        insertAuditLog(id, existing.getStatus(), SupplierStatus.APPROVED.getCode(),
                SupplierAuditAction.APPROVE.getDescription(), auditRemarkOrDefault(request, "审核通过"));
        // 发送通知
        sendSupplierNotice(existing,
                "供应商审核通过",
                "供应商【" + existing.getSupplierName() + "】审核已通过，Portal 账号已开通",
                NotificationConstants.BIZ_TYPE_SUPPLIER_AUDIT,
                String.valueOf(id),
                NotificationConstants.PRIORITY_HIGH);
    }

    /**
     * 审核拒绝供应商。
     *
     * @param id      供应商ID
     * @param request 审核请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(Long id, SupplierAuditRequest request) {
        requireAuditRemark(request);
        changeAuditStatus(id, SupplierStatus.PENDING_AUDIT, SupplierStatus.REJECTED,
                SupplierAuditAction.REJECT, request, "供应商审核拒绝");
    }

    /**
     * 要求供应商补充资料。
     *
     * @param id      供应商ID
     * @param request 审核请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void requestSupplement(Long id, SupplierAuditRequest request) {
        requireAuditRemark(request);
        changeAuditStatus(id, SupplierStatus.PENDING_AUDIT, SupplierStatus.DRAFT,
                SupplierAuditAction.SUPPLEMENT, request, "供应商需要补充资料");
    }

    /**
     * 停用供应商。
     *
     * @param id      供应商ID
     * @param request 审核请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disable(Long id, SupplierAuditRequest request) {
        Supplier existing = requireSupplier(id);
        ensureTransitionAllowed(existing.getStatus(), SupplierStatus.DISABLED.getCode());
        Supplier update = buildAuditUpdate(SupplierStatus.DISABLED, request);
        update.setPortalEnabled(0);
        int rows = updateStatusWithExpected(id, SupplierStatus.APPROVED, update);
        if (rows == 0) {
            throwSupplierStatusException("供应商状态已变化，请刷新后重试");
        }
        insertAuditLog(id, existing.getStatus(), SupplierStatus.DISABLED.getCode(),
                SupplierAuditAction.DISABLE.getDescription(), auditRemarkOrDefault(request, "停用供应商"));
        sendSupplierNotice(existing,
                "供应商已停用",
                "供应商【" + existing.getSupplierName() + "】已被停用，如需恢复请联系采购负责人",
                NotificationConstants.BIZ_TYPE_SUPPLIER_AUDIT,
                String.valueOf(id),
                NotificationConstants.PRIORITY_HIGH);
    }

    /**
     * 重新启用供应商。
     *
     * @param id      供应商ID
     * @param request 审核请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enable(Long id, SupplierAuditRequest request) {
        Supplier existing = requireSupplier(id);
        ensureTransitionAllowed(existing.getStatus(), SupplierStatus.APPROVED.getCode());
        Supplier update = buildAuditUpdate(SupplierStatus.APPROVED, request);
        update.setPortalEnabled(1);
        int rows = updateStatusWithExpected(id, SupplierStatus.DISABLED, update);
        if (rows == 0) {
            throwSupplierStatusException("供应商状态已变化，请刷新后重试");
        }
        insertAuditLog(id, existing.getStatus(), SupplierStatus.APPROVED.getCode(),
                SupplierAuditAction.ENABLE.getDescription(), auditRemarkOrDefault(request, "重新启用供应商"));
        sendSupplierNotice(existing,
                "供应商已重新启用",
                "供应商【" + existing.getSupplierName() + "】已重新启用",
                NotificationConstants.BIZ_TYPE_SUPPLIER_AUDIT,
                String.valueOf(id),
                NotificationConstants.PRIORITY_NORMAL);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Supplier existing = requireSupplier(id);
        // 审核中的 供应商 不可以直接删除
        // 如果有 订货单 还没有结束 不允许删除 当前还未实现
        if (SupplierStatus.PENDING_AUDIT.getCode().equals(existing.getStatus())) {
            throwSupplierStatusException("审核进行中，请等待审核结果");
        }
        if (!SupplierStateMachine.canDelete(existing.getStatus())) {
            throwSupplierStatusException("当前状态不允许删除供应商");
        }

        Supplier update = new Supplier();
        update.setPortalEnabled(0);
        // 逻辑删除
        update.setIsDeleted(1);
        int rows = supplierMapper.update(update, new LambdaUpdateWrapper<Supplier>().eq(Supplier::getId, id));
        if (rows == 0) {
            throwSupplierException(SupplierErrorCode.SUPPLIER_NOT_FOUND);
        }
        // 记录操作日志
        insertAuditLog(id, existing.getStatus(), existing.getStatus(), DELETE_ACTION, "逻辑删除供应商");
    }

    /**
     * MyBatis-Plus 的动态条件拼接
     *
     * @param query
     * @return
     */
    private QueryWrapper<Supplier> buildPageWrapper(SupplierPageQuery query) {
        QueryWrapper<Supplier> wrapper = DataScopeQueryHelper.apply(new QueryWrapper<Supplier>(),
                "create_by", null, null, "id");
        return wrapper.like(StrUtil.isNotBlank(query.getSupplierName()), "supplier_name", query.getSupplierName())
                .eq(ObjectUtil.isNotNull(query.getSupplierType()), "supplier_type", query.getSupplierType())
                .eq(ObjectUtil.isNotNull(query.getStatus()), "status", query.getStatus())
                .eq(StrUtil.isNotBlank(query.getGrade()), "grade", query.getGrade())
                .ge(ObjectUtil.isNotNull(query.getCreateStartTime()), "create_time", query.getCreateStartTime())
                .le(ObjectUtil.isNotNull(query.getCreateEndTime()), "create_time", query.getCreateEndTime())
                .orderByDesc("create_time");
    }

    private Set<Long> queryCertExpireWarningSupplierIds(List<Supplier> suppliers) {
        if (CollUtil.isEmpty(suppliers)) {
            return Collections.emptySet();
        }
        List<Long> supplierIds = suppliers.stream().map(Supplier::getId).toList();
        LocalDate warningDate = LocalDate.now().plusDays(CERT_EXPIRE_WARNING_DAYS);
        return certMapper.selectList(new LambdaQueryWrapper<SupplierCert>()
                        .in(SupplierCert::getSupplierId, supplierIds)
                        .eq(SupplierCert::getIsExpired, 0)
                        .isNotNull(SupplierCert::getExpireDate)
                        .le(SupplierCert::getExpireDate, warningDate))
                .stream()
                .map(SupplierCert::getSupplierId)
                .collect(Collectors.toSet());
    }

    private SupplierListVO toListVO(Supplier supplier, Set<Long> warningSupplierIds) {
        SupplierListVO vo = BeanUtil.copyProperties(supplier, SupplierListVO.class);
        vo.setSupplierTypeName(SupplierType.descriptionOf(supplier.getSupplierType()));
        vo.setStatusName(SupplierStatus.descriptionOf(supplier.getStatus()));
        // DesensitizedUtil.mobilePhone Hutool工具包中的方法 隐藏手机号码的中间四位
        vo.setContactPhone(DesensitizedUtil.mobilePhone(supplier.getContactPhone()));
        // 在显示供应商列表的时候 会通过一些警告的提示(前端可以通过不同的颜色来标识一些特殊的供应商)
        // 那么在这里就是要判断当前的供应商是否属于是 特殊的供应商
        vo.setCertExpireWarning(warningSupplierIds.contains(supplier.getId()));
        return vo;
    }

    private <T> CompletableFuture<List<T>> asyncQuery(String queryName, SupplierRelationQuery<T> query) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return query.query();
            } catch (RuntimeException exception) {
                log.error("{}查询失败", queryName, exception);
                throw exception;
            }
        }, supplierTaskExecutor);
    }

    private Supplier buildCreateSupplier(SupplierCreateRequest request, Long tenantId) {
        Supplier supplier = BeanUtil.copyProperties(request, Supplier.class);
        supplier.setTenantId(tenantId);
        // 供应商编码
        supplier.setSupplierCode(codeGenerator.generate(tenantId));
        supplier.setStatus(SupplierStatus.DRAFT.getCode());
        supplier.setGrade("C");
        supplier.setPortalEnabled(0);
        supplier.setCategoryIds(JSONUtil.toJsonStr(ObjectUtil.defaultIfNull(request.getCategoryIds(), Collections.emptyList())));
        supplier.setTags(JSONUtil.toJsonStr(ObjectUtil.defaultIfNull(request.getTags(), Collections.emptyList())));
        supplier.setCurrency(StrUtil.blankToDefault(request.getCurrency(), "CNY"));
        supplier.setPaymentDays(ObjectUtil.defaultIfNull(request.getPaymentDays(), 0));
        return supplier;
    }

    private Supplier buildUpdateSupplier(Supplier existing, SupplierUpdateRequest request) {
        Supplier update = new Supplier();
        if (SupplierStatus.DRAFT.getCode().equals(existing.getStatus()) || SupplierStatus.REJECTED.getCode().equals(existing.getStatus())) {
            BeanUtil.copyProperties(request, update);
            update.setCategoryIds(JSONUtil.toJsonStr(ObjectUtil.defaultIfNull(request.getCategoryIds(), Collections.emptyList())));
            update.setTags(JSONUtil.toJsonStr(ObjectUtil.defaultIfNull(request.getTags(), Collections.emptyList())));
        } else {
            update.setContactName(request.getContactName());
            update.setContactPhone(request.getContactPhone());
            update.setContactEmail(request.getContactEmail());
            update.setContactWechat(request.getContactWechat());
            update.setContactWhatsapp(request.getContactWhatsapp());
            update.setRemark(request.getRemark());
            update.setTags(JSONUtil.toJsonStr(ObjectUtil.defaultIfNull(request.getTags(), Collections.emptyList())));
        }
        return update;
    }

    private void validateUpdateRequest(Supplier existing, SupplierUpdateRequest request) {
        // 对应草稿状态下 可以全部更新  对应已经审核通过的 只能部分更新
        if (!SupplierStatus.APPROVED.getCode().equals(existing.getStatus()) && !SupplierStatus.DISABLED.getCode().equals(existing.getStatus())) {
            validateRequiredForFullUpdate(request);
            validateSupplierType(request.getSupplierType());
            validateDuplicateSupplierName(currentTenantId(), existing.getId(), request.getSupplierName());
        }
        if (StrUtil.isNotBlank(request.getContactEmail())) {
            validateDuplicateEmail(currentTenantId(), existing.getId(), request.getContactEmail());
        }
    }

    private void validateRequiredForFullUpdate(SupplierUpdateRequest request) {
        if (StrUtil.isBlank(request.getSupplierName())) {
            BusinessException.throwException(ResultCode.PARAM_ERROR.getCode(), "供应商名称不能为空");
        }
        if (ObjectUtil.isNull(request.getSupplierType())) {
            BusinessException.throwException(ResultCode.PARAM_ERROR.getCode(), "供应商类型不能为空");
        }
        if (StrUtil.isBlank(request.getContactPhone())) {
            BusinessException.throwException(ResultCode.PARAM_ERROR.getCode(), "手机号不能为空");
        }
        if (StrUtil.isBlank(request.getContactEmail())) {
            BusinessException.throwException(ResultCode.PARAM_ERROR.getCode(), "邮箱不能为空");
        }
        if (ObjectUtil.isNotNull(request.getMoq()) && request.getMoq() <= 0) {
            BusinessException.throwException(ResultCode.PARAM_ERROR.getCode(), "最小起订量必须大于0");
        }
        if (ObjectUtil.isNotNull(request.getLeadTimeDays()) && (request.getLeadTimeDays() < 1 || request.getLeadTimeDays() > 365)) {
            BusinessException.throwException(ResultCode.PARAM_ERROR.getCode(), "交货周期必须在1-365天之间");
        }
    }

    private void validateSubmitRequired(Supplier supplier) {
        if (StrUtil.isBlank(supplier.getSupplierName())) {
            BusinessException.throwException(ResultCode.PARAM_ERROR.getCode(), "供应商名称不能为空");
        }
        if (ObjectUtil.isNull(supplier.getSupplierType())) {
            BusinessException.throwException(ResultCode.PARAM_ERROR.getCode(), "供应商类型不能为空");
        }
        validateSupplierType(supplier.getSupplierType());
        if (StrUtil.isBlank(supplier.getContactName())) {
            BusinessException.throwException(ResultCode.PARAM_ERROR.getCode(), "主联系人不能为空");
        }
        if (StrUtil.isBlank(supplier.getContactPhone())) {
            BusinessException.throwException(ResultCode.PARAM_ERROR.getCode(), "手机号不能为空");
        }
        if (StrUtil.isBlank(supplier.getContactEmail())) {
            BusinessException.throwException(ResultCode.PARAM_ERROR.getCode(), "邮箱不能为空");
        }
    }

    private void validateBusinessLicenseUploaded(Long supplierId) {
        Long count = certMapper.selectCount(new LambdaQueryWrapper<SupplierCert>()
                .eq(SupplierCert::getSupplierId, supplierId)
                .eq(SupplierCert::getCertType, BUSINESS_LICENSE_CERT_TYPE)
                .eq(SupplierCert::getIsDeleted, 0));
        if (count == null || count == 0) {
            throwSupplierException(SupplierErrorCode.SUPPLIER_BUSINESS_LICENSE_REQUIRED);
        }
    }

    private void validateCertUploadRequest(SupplierCertUploadRequest request, MultipartFile file) {
        if (request == null || file == null || file.isEmpty()) {
            BusinessException.throwException(ResultCode.PARAM_ERROR.getCode(), "请选择要上传的文件");
        }
        if (file.getSize() > MAX_CERT_FILE_SIZE) {
            BusinessException.throwException(ResultCode.PARAM_ERROR.getCode(), "文件大小不能超过5MB");
        }
        if (ObjectUtil.isNull(request.getCertType())) {
            BusinessException.throwException(ResultCode.PARAM_ERROR.getCode(), "资质类型不能为空");
        }
        if (StrUtil.isBlank(request.getCertName())) {
            BusinessException.throwException(ResultCode.PARAM_ERROR.getCode(), "资质名称不能为空");
        }
    }

    private byte[] readFileBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException exception) {
            log.error("读取供应商资质文件失败，fileName={}", file.getOriginalFilename(), exception);
            BusinessException.throwException("读取上传文件失败");
            return new byte[0];
        }
    }

    private String validateCertFileType(MultipartFile file, byte[] bytes) {
        // 获取文件的MIME类型
        String fileType = normalizeContentType(file.getContentType());
        if (!ALLOWED_CERT_MIME_TYPES.contains(fileType) || !isAllowedExtension(fileType, file.getOriginalFilename())) {
            throwSupplierException(SupplierErrorCode.SUPPLIER_CERT_FILE_TYPE_NOT_ALLOWED);
        }
        if (!matchesMagicBytes(fileType, bytes)) {
            throwSupplierException(SupplierErrorCode.SUPPLIER_CERT_FILE_CONTENT_MISMATCH);
        }
        return fileType;
    }

    private String normalizeContentType(String contentType) {
        if (StrUtil.isBlank(contentType)) {
            return "";
        }
        return contentType.split(";")[0].trim().toLowerCase(Locale.ROOT);
    }

    private boolean isAllowedExtension(String fileType, String originalFilename) {
        String extension = extractExtension(originalFilename);
        return CERT_MIME_EXTENSIONS.getOrDefault(fileType, Collections.emptySet()).contains(extension);
    }

    private String extractExtension(String originalFilename) {
        if (StrUtil.isBlank(originalFilename)) {
            return "";
        }
        int index = originalFilename.lastIndexOf(".");
        if (index < 0 || index == originalFilename.length() - 1) {
            return "";
        }
        return originalFilename.substring(index + 1).toLowerCase(Locale.ROOT);
    }

    private boolean matchesMagicBytes(String fileType, byte[] bytes) {
        if ("image/jpeg".equals(fileType)) {
            return bytes.length >= 3
                    && (bytes[0] & 0xFF) == 0xFF
                    && (bytes[1] & 0xFF) == 0xD8
                    && (bytes[2] & 0xFF) == 0xFF;
        }
        if ("image/png".equals(fileType)) {
            return bytes.length >= 8
                    && (bytes[0] & 0xFF) == 0x89
                    && bytes[1] == 0x50
                    && bytes[2] == 0x4E
                    && bytes[3] == 0x47
                    && bytes[4] == 0x0D
                    && bytes[5] == 0x0A
                    && bytes[6] == 0x1A
                    && bytes[7] == 0x0A;
        }
        if ("application/pdf".equals(fileType)) {
            return bytes.length >= 4
                    && bytes[0] == 0x25
                    && bytes[1] == 0x50
                    && bytes[2] == 0x44
                    && bytes[3] == 0x46;
        }
        return false;
    }

    private SupplierCert buildSupplierCert(Supplier supplier,
                                           SupplierCertUploadRequest request,
                                           MultipartFile file,
                                           String fileType,
                                           String fileUrl) {
        SupplierCert cert = new SupplierCert();
        cert.setTenantId(supplier.getTenantId());
        cert.setSupplierId(supplier.getId());
        cert.setCertType(request.getCertType());
        cert.setCertName(request.getCertName());
        cert.setFileName(file.getOriginalFilename());
        cert.setFileUrl(fileUrl);
        cert.setFileSize(file.getSize());
        cert.setFileType(fileType);
        cert.setIssueDate(request.getIssueDate());
        cert.setExpireDate(request.getExpireDate());
        cert.setIsExpired(0);
        cert.setCertNo(request.getCertNo());
        cert.setRemark(request.getRemark());
        return cert;
    }

    private SupplierCertUploadVO toCertUploadVO(SupplierCert cert) {
        SupplierCertUploadVO vo = new SupplierCertUploadVO();
        vo.setId(cert.getId());
        vo.setFileName(cert.getFileName());
        vo.setFileUrl(cert.getFileUrl());
        vo.setFileSize(cert.getFileSize());
        vo.setFileType(cert.getFileType());
        return vo;
    }

    private void changeAuditStatus(Long id,
                                   SupplierStatus expectedStatus,
                                   SupplierStatus targetStatus,
                                   SupplierAuditAction action,
                                   SupplierAuditRequest request,
                                   String notice) {
        Supplier existing = requireSupplier(id);
        ensureTransitionAllowed(existing.getStatus(), targetStatus.getCode());
        validateAuditOperator(existing);
        Supplier update = buildAuditUpdate(targetStatus, request);
        int rows = updateStatusWithExpected(id, expectedStatus, update);
        if (rows == 0) {
            throwSupplierStatusException("供应商状态已变化，请刷新后重试");
        }
        insertAuditLog(id, existing.getStatus(), targetStatus.getCode(), action.getDescription(), request.getAuditRemark());
        sendSupplierNotice(existing,
                notice,
                notice + "，原因：" + request.getAuditRemark(),
                NotificationConstants.BIZ_TYPE_SUPPLIER_AUDIT,
                String.valueOf(id),
                SupplierStatus.REJECTED.equals(targetStatus) ? NotificationConstants.PRIORITY_HIGH : NotificationConstants.PRIORITY_NORMAL);
    }

    private Supplier buildAuditUpdate(SupplierStatus targetStatus, SupplierAuditRequest request) {
        Supplier update = new Supplier();
        update.setStatus(targetStatus.getCode());
        update.setAuditUserId(ObjectUtil.defaultIfNull(TenantContext.getUserId(), 0L));
        update.setAuditTime(LocalDateTime.now());
        update.setAuditRemark(auditRemarkOrDefault(request, targetStatus.getDescription()));
        return update;
    }

    private SupplierPortalUser buildPortalUser(Supplier supplier, Long portalUserId) {
        SupplierPortalUser portalUser = new SupplierPortalUser();
        portalUser.setId(portalUserId);
        portalUser.setTenantId(supplier.getTenantId());
        portalUser.setUsername(supplier.getContactEmail());
        portalUser.setPassword(BCrypt.hashpw(DEFAULT_PORTAL_PASSWORD, BCrypt.gensalt()));
        portalUser.setRealName(StrUtil.blankToDefault(supplier.getContactName(), supplier.getSupplierName()));
        portalUser.setEmail(supplier.getContactEmail());
        portalUser.setPhone(supplier.getContactPhone());
        portalUser.setUserType(PORTAL_USER_TYPE);
        portalUser.setStatus(USER_STATUS_NORMAL);
        portalUser.setLoginFailCount(0);
        portalUser.setCreateBy(ObjectUtil.defaultIfNull(TenantContext.getUserId(), 0L));
        portalUser.setUpdateBy(ObjectUtil.defaultIfNull(TenantContext.getUserId(), 0L));
        return portalUser;
    }

    private int updateStatusWithExpected(Long id, SupplierStatus expectedStatus, Supplier update) {
        return supplierMapper.update(update, new LambdaUpdateWrapper<Supplier>()
                .eq(Supplier::getId, id)
                .eq(Supplier::getStatus, expectedStatus.getCode())); // CAS 如果已经是审核通过 就失败
    }

    private String auditRemarkOrDefault(SupplierAuditRequest request, String defaultRemark) {
        if (request == null || StrUtil.isBlank(request.getAuditRemark())) {
            return defaultRemark;
        }
        return request.getAuditRemark();
    }

    private void requireAuditRemark(SupplierAuditRequest request) {
        if (request == null || StrUtil.isBlank(request.getAuditRemark())) {
            throwSupplierException(SupplierErrorCode.SUPPLIER_AUDIT_REMARK_REQUIRED);
        }
    }

    private void validateAuditOperator(Supplier supplier) {
        Long currentUserId = TenantContext.getUserId();
        if (ObjectUtil.isNotNull(currentUserId) && currentUserId.equals(supplier.getCreateBy())) {
            throwSupplierException(SupplierErrorCode.SUPPLIER_SELF_AUDIT_NOT_ALLOWED);
        }
    }

    private void ensureTransitionAllowed(Integer fromStatus, Integer toStatus) {
        // SupplierStateMachine 供应商状态机
        if (!SupplierStateMachine.canTransit(fromStatus, toStatus)) {
            throwSupplierStatusException("供应商状态不允许从" + SupplierStatus.descriptionOf(fromStatus)
                    + "流转到" + SupplierStatus.descriptionOf(toStatus));
        }
    }

    private void sendRoleNotice(Long tenantId,
                                String roleKey,
                                String title,
                                String content,
                                String bizType,
                                String bizId,
                                String priority) {
        notificationService.send(NotificationCommand.builder()
                .tenantId(tenantId)
                .receiverType(NotificationConstants.RECEIVER_TYPE_ROLE)
                .receiverKey(roleKey)
                .title(title)
                .content(content)
                .bizType(bizType)
                .bizId(bizId)
                .priority(priority)
                .build());
    }

    private void sendSupplierNotice(Supplier supplier,
                                    String title,
                                    String content,
                                    String bizType,
                                    String bizId,
                                    String priority) {
        notificationService.send(NotificationCommand.builder()
                .tenantId(supplier.getTenantId())
                .receiverType(NotificationConstants.RECEIVER_TYPE_ROLE)
                .receiverKey(NotificationConstants.ROLE_PURCHASE_SPECIALIST)
                .title(title)
                .content(content)
                .bizType(bizType)
                .bizId(bizId)
                .priority(priority)
                .mailTo(supplier.getContactEmail())
                .mailSubject(title)
                .mailContent(content)
                .build());
    }

    private Supplier requireSupplier(Long id) {
        Supplier supplier = supplierMapper.selectById(id);
        if (ObjectUtil.isNull(supplier)) {
            throwSupplierException(SupplierErrorCode.SUPPLIER_NOT_FOUND);
        }
        return supplier;
    }

    private void validateSupplierType(Integer supplierType) {
        if (!SupplierType.contains(supplierType)) {
            BusinessException.throwException(ResultCode.PARAM_ERROR.getCode(), "供应商类型不合法");
        }
    }

    private void validateDuplicateSupplierName(Long tenantId, Long excludeId, String supplierName) {
        Long count = supplierMapper.selectCount(new LambdaQueryWrapper<Supplier>()
                .eq(Supplier::getTenantId, tenantId)
                .eq(Supplier::getSupplierName, supplierName)
                .ne(ObjectUtil.isNotNull(excludeId), Supplier::getId, excludeId));
        if (count != null && count > 0) {
            throwSupplierException(SupplierErrorCode.SUPPLIER_NAME_EXISTS);
        }
    }

    private void validateDuplicateEmail(Long tenantId, Long excludeId, String contactEmail) {
        Long count = supplierMapper.selectCount(new LambdaQueryWrapper<Supplier>()
                .eq(Supplier::getTenantId, tenantId)
                .eq(Supplier::getContactEmail, contactEmail)
                .ne(ObjectUtil.isNotNull(excludeId), Supplier::getId, excludeId));
        if (count != null && count > 0) {
            throwSupplierException(SupplierErrorCode.SUPPLIER_EMAIL_EXISTS);
        }
    }

    private void insertAuditLog(Long supplierId, Integer fromStatus, Integer toStatus, String action, String remark) {
        SupplierAuditLog auditLog = new SupplierAuditLog();
        auditLog.setTenantId(currentTenantId());
        auditLog.setSupplierId(supplierId);
        auditLog.setFromStatus(fromStatus);
        auditLog.setToStatus(toStatus);
        auditLog.setAction(action);
        auditLog.setAuditRemark(remark);
        auditLog.setOperatorId(ObjectUtil.defaultIfNull(TenantContext.getUserId(), 0L));
        auditLog.setOperatorName("系统操作人");
        auditLog.setOperateTime(LocalDateTime.now());
        auditLogMapper.insert(auditLog);
    }

    private Long currentTenantId() {
        return ObjectUtil.defaultIfNull(TenantContext.getTenantId(), 0L);
    }

    private void throwSupplierException(SupplierErrorCode errorCode) {
        BusinessException.throwException(errorCode.getCode(), errorCode.getMessage());
    }

    private void throwSupplierStatusException(String message) {
        BusinessException.throwException(SupplierErrorCode.SUPPLIER_STATUS_NOT_ALLOWED.getCode(), message);
    }

    @FunctionalInterface
    private interface SupplierRelationQuery<T> {

        List<T> query();
    }
}
