package com.lyf.supplychain.supplier.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lyf.supplychain.supplier.constant.NotificationConstants;
import com.lyf.supplychain.supplier.constant.SupplierStatus;
import com.lyf.supplychain.supplier.entity.Supplier;
import com.lyf.supplychain.supplier.entity.SupplierTenantConfig;
import com.lyf.supplychain.supplier.entity.SupplierWatchlist;
import com.lyf.supplychain.supplier.mapper.SupplierMapper;
import com.lyf.supplychain.supplier.mapper.SupplierTenantConfigMapper;
import com.lyf.supplychain.supplier.mapper.SupplierWatchlistMapper;
import com.lyf.supplychain.supplier.model.NotificationCommand;
import com.lyf.supplychain.supplier.service.NotificationService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 供应商分层分级策略扫描 XXL-JOB 任务。
 *
 * @author liyunfei
 * @date 2026-05-18
 */
@Slf4j
@Component
public class SupplierLayeringStrategyJob {

    private static final String WATCHLIST_GRADES_CONFIG = "supplier.layering.watchlist.grades";

    private static final String DEFAULT_WATCHLIST_GRADES = "B,C";

    private static final int WATCHLIST_STATUS_ACTIVE = 1;

    private final SupplierMapper supplierMapper;

    private final SupplierWatchlistMapper watchlistMapper;

    private final SupplierTenantConfigMapper tenantConfigMapper;

    private final NotificationService notificationService;

    private final Clock clock;

    public SupplierLayeringStrategyJob(SupplierMapper supplierMapper,
                                       SupplierWatchlistMapper watchlistMapper,
                                       SupplierTenantConfigMapper tenantConfigMapper,
                                       NotificationService notificationService,
                                       Clock clock) {
        this.supplierMapper = supplierMapper;
        this.watchlistMapper = watchlistMapper;
        this.tenantConfigMapper = tenantConfigMapper;
        this.notificationService = notificationService;
        this.clock = clock;
    }

    /**
     * XXL-JOB 入口，扫描供应商分层分级建议。
     */
    @XxlJob("supplierLayeringStrategyJob")
    public void execute() {
        SupplierLayeringJobResult result = executeLayering();
        String message = "供应商分层分级扫描完成，扫描数量=" + result.getScannedCount()
                + "，新增观察=" + result.getWatchlistCreatedCount()
                + "，已存在观察=" + result.getWatchlistExistsCount()
                + "，失败数量=" + result.getFailedCount();
        XxlJobHelper.log(message);
        XxlJobHelper.handleSuccess(message);
    }

    /**
     * 执行供应商分层分级建议扫描。
     *
     * @return 扫描结果
     */
    public SupplierLayeringJobResult executeLayering() {
        SupplierLayeringJobResult result = new SupplierLayeringJobResult();
        // 拿到所有审核通过且未被删除的供货商
        // 供应商表中的所有 数据 未区分租户
        List<Supplier> suppliers = supplierMapper.selectList(new LambdaQueryWrapper<Supplier>()
                .eq(Supplier::getStatus, SupplierStatus.APPROVED.getCode())
                .eq(Supplier::getIsDeleted, 0));
        // 遍历供货商
        for (Supplier supplier : suppliers) {
            result.incrementScanned();
            try {
                processSupplier(supplier, result);
            } catch (Exception exception) {
                result.incrementFailed();
                log.error("供应商分层分级扫描失败，supplierId={}", supplier.getId(), exception);
            }
        }
        log.info("供应商分层分级扫描完成，scanned={}, created={}, exists={}, failed={}",
                result.getScannedCount(), result.getWatchlistCreatedCount(),
                result.getWatchlistExistsCount(), result.getFailedCount());
        return result;
    }

    private void processSupplier(Supplier supplier, SupplierLayeringJobResult result) {

        // 获取供应商的租户ID 根据这个租户ID 查询
        // watchlistGrades 配置表中的 监控级别  B C
        Set<String> watchlistGrades = queryWatchlistGrades(supplier.getTenantId());
        // 判断当前的供应商的级别是否为空 如果为空 默认是 C
        // 新的供应商 不参与评级 默认是 C
        String grade = safeGrade(supplier.getGrade());
        // 当前的供应商的级别 是否在 监控级别中
        // 举例: 比如当前的供应商的级别是C 租户要求的 B C
        // 说明当前的供应商 是不是就不满足 没有达到 租户的要求 就要进入到 重点观察的名单中
        // 如果不包含 就返回 如果包含就要记录到表中
        if (!watchlistGrades.contains(grade)) {
            return;
        }
        // selectCount 只是判断 当前的供应商 是否已经存在与 监控表中
        Long count = watchlistMapper.selectCount(new LambdaQueryWrapper<SupplierWatchlist>()
                .eq(SupplierWatchlist::getSupplierId, supplier.getId())
                .eq(SupplierWatchlist::getStatus, WATCHLIST_STATUS_ACTIVE)
                .eq(SupplierWatchlist::getIsDeleted, 0));
        if (Optional.ofNullable(count).orElse(0L) > 0) {
            // 已经存在了 直接返回 不要再往名单中添加了
            result.incrementWatchlistExists();
            return;
        }
        // 创建监控名单的实体
        SupplierWatchlist watchlist = new SupplierWatchlist();
        watchlist.setTenantId(supplier.getTenantId());
        watchlist.setSupplierId(supplier.getId());
        watchlist.setCurrentGrade(grade);
        watchlist.setCurrentScore(Optional.ofNullable(supplier.getScore()).orElse(BigDecimal.ZERO));
        watchlist.setWatchReason(resolveWatchReason(grade));
        watchlist.setSystemSuggestion(resolveSystemSuggestion(grade));
        watchlist.setStatus(WATCHLIST_STATUS_ACTIVE);
        watchlist.setWatchTime(LocalDateTime.now(clock));
        // 插入当前供应商到监控表中
        watchlistMapper.insert(watchlist);
        result.incrementWatchlistCreated();
        sendWatchlistNotice(supplier, watchlist);
    }

    private void sendWatchlistNotice(Supplier supplier, SupplierWatchlist watchlist) {
        String title = "供应商进入重点观察名单";
        String content = "供应商【" + supplier.getSupplierName() + "】当前评级为 " + watchlist.getCurrentGrade()
                + "，已进入重点观察名单。系统建议：" + watchlist.getSystemSuggestion();
        notificationService.send(NotificationCommand.builder()
                .tenantId(supplier.getTenantId())
                .receiverType(NotificationConstants.RECEIVER_TYPE_ROLE)
                .receiverKey(NotificationConstants.ROLE_PURCHASE_MANAGER)
                .title(title)
                .content(content)
                .bizType(NotificationConstants.BIZ_TYPE_SUPPLIER_LAYERING)
                .bizId(String.valueOf(supplier.getId()))
                .priority(NotificationConstants.PRIORITY_HIGH)
                .mailTo(supplier.getContactEmail())
                .mailSubject(title)
                .mailContent(content)
                .build());
    }

    private Set<String> queryWatchlistGrades(Long tenantId) {
        // 这个配置项 是SaaS平台的运营人员进行管理的
        // 也就是说 所有的租户 都必须要按照这个配置项的值  进行判断
        // 举例: 同一品类建议维护的最少可用供应商数量 2
        // 也就是说 如果某个租户的 某个品类的供应商 如果少于 2个 就是存在风险的
        SupplierTenantConfig config = tenantConfigMapper.selectOne(new LambdaQueryWrapper<SupplierTenantConfig>()
                .eq(SupplierTenantConfig::getTenantId, tenantId)
                .eq(SupplierTenantConfig::getConfigKey, WATCHLIST_GRADES_CONFIG)
                .eq(SupplierTenantConfig::getEnabled, 1)
                .eq(SupplierTenantConfig::getIsDeleted, 0)
                .last("LIMIT 1"));
        String configValue = Optional.ofNullable(config)
                .map(SupplierTenantConfig::getConfigValue)
                .filter(value -> !value.isBlank())
                .orElse(DEFAULT_WATCHLIST_GRADES);
        return Arrays.stream(configValue.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .collect(Collectors.toSet());
    }

    private String resolveWatchReason(String grade) {
        return switch (grade) {
            case "B" -> "B级待改进供应商";
            case "C" -> "C级预警供应商";
            default -> "租户配置要求观察的供应商评级";
        };
    }

    private String resolveSystemSuggestion(String grade) {
        return switch (grade) {
            case "B" -> "建议发送改进通知书并持续观察，采购份额调整由租户采购负责人自行决定";
            case "C" -> "建议纳入预警跟踪并开发替代供应商，是否继续采购由租户自行决定";
            default -> "系统建议关注该供应商后续绩效表现";
        };
    }

    private String safeGrade(String grade) {
        return grade == null ? "C" : grade;
    }
}
