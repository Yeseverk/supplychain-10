package com.lyf.supplychain.supplier.job;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.lyf.supplychain.supplier.constant.NotificationConstants;
import com.lyf.supplychain.supplier.constant.SupplierStatus;
import com.lyf.supplychain.supplier.entity.Supplier;
import com.lyf.supplychain.supplier.entity.SupplierRiskEvent;
import com.lyf.supplychain.supplier.entity.SupplierTenantConfig;
import com.lyf.supplychain.supplier.mapper.SupplierMapper;
import com.lyf.supplychain.supplier.mapper.SupplierRiskEventMapper;
import com.lyf.supplychain.supplier.mapper.SupplierTenantConfigMapper;
import com.lyf.supplychain.supplier.model.NotificationCommand;
import com.lyf.supplychain.supplier.service.NotificationService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 多供应商策略风险扫描 XXL-JOB 任务。
 *
 * @author liyunfei
 * @date 2026-05-18
 */
@Slf4j
@Component
public class SupplierMultiSupplierRiskJob {

    private static final String MIN_SUPPLIER_COUNT_CONFIG = "supplier.multi.min_count";

    private static final String MIN_HEALTHY_GRADE_CONFIG = "supplier.multi.min_healthy_grade";

    private static final int DEFAULT_MIN_SUPPLIER_COUNT = 2;

    private static final String DEFAULT_MIN_HEALTHY_GRADE = "B";

    private static final String RISK_TYPE_SUPPLIER_COUNT_LOW = "SUPPLIER_COUNT_LOW";

    private static final String RISK_TYPE_ALL_SUPPLIER_GRADE_LOW = "ALL_SUPPLIER_GRADE_LOW";

    private static final int RISK_STATUS_OPEN = 1;

    private static final int RISK_STATUS_RESOLVED = 2;

    private final SupplierMapper supplierMapper;

    private final SupplierRiskEventMapper riskEventMapper;

    private final SupplierTenantConfigMapper tenantConfigMapper;

    private final NotificationService notificationService;

    private final Clock clock;

    public SupplierMultiSupplierRiskJob(SupplierMapper supplierMapper,
                                        SupplierRiskEventMapper riskEventMapper,
                                        SupplierTenantConfigMapper tenantConfigMapper,
                                        NotificationService notificationService,
                                        Clock clock) {
        this.supplierMapper = supplierMapper;
        this.riskEventMapper = riskEventMapper;
        this.tenantConfigMapper = tenantConfigMapper;
        this.notificationService = notificationService;
        this.clock = clock;
    }

    /**
     * XXL-JOB 入口，扫描多供应商策略风险。
     */
    @XxlJob("supplierMultiSupplierRiskJob")
    public void execute() {
        SupplierRiskScanJobResult result = executeRiskScan();
        String message = "多供应商策略风险扫描完成，品类数量=" + result.getCategoryCount()
                + "，新增风险=" + result.getCreatedCount()
                + "，已有风险=" + result.getExistsCount()
                + "，解除风险=" + result.getResolvedCount()
                + "，失败数量=" + result.getFailedCount();
        XxlJobHelper.log(message);
        XxlJobHelper.handleSuccess(message);
    }

    /**
     * 执行多供应商策略风险扫描。
     *
     * @return 扫描结果
     */
    public SupplierRiskScanJobResult executeRiskScan() {
        SupplierRiskScanJobResult result = new SupplierRiskScanJobResult();
        // 查询已经通过审核的所有的供应商 不区分租户
        List<Supplier> suppliers = queryApprovedSuppliers();
        // 按照分组 进行 统计
        // Key 商品分类的ID  Value 该分类对应的供应商集合
        Map<Long, List<Supplier>> categorySupplierMap = groupByCategory(suppliers);
        Set<String> currentRiskKeys = new HashSet<>();
        for (Map.Entry<Long, List<Supplier>> entry : categorySupplierMap.entrySet()) {
            result.incrementCategory();
            try {
                // 扫描分类
                scanCategory(entry.getKey(), entry.getValue(), currentRiskKeys, result);
            } catch (Exception exception) {
                result.incrementFailed();
                log.error("多供应商策略风险扫描失败，categoryId={}", entry.getKey(), exception);
            }
        }
        // currentRiskKeys 存了两类风险
        // 一类是 不满足最低的数量的风险 一类是 不满足 最低的等级的风险
        resolveStaleRiskEvents(currentRiskKeys, result);
        log.info("多供应商策略风险扫描完成，categories={}, created={}, exists={}, resolved={}, failed={}",
                result.getCategoryCount(), result.getCreatedCount(), result.getExistsCount(),
                result.getResolvedCount(), result.getFailedCount());
        return result;
    }

    private List<Supplier> queryApprovedSuppliers() {
        return supplierMapper.selectList(new LambdaQueryWrapper<Supplier>()
                .eq(Supplier::getStatus, SupplierStatus.APPROVED.getCode())
                .eq(Supplier::getIsDeleted, 0));
    }

    private Map<Long, List<Supplier>> groupByCategory(List<Supplier> suppliers) {
        Map<Long, List<Supplier>> categorySupplierMap = new HashMap<>();
        for (Supplier supplier : suppliers) {
            for (Long categoryId : parseCategoryIds(supplier.getCategoryIds())) {
                categorySupplierMap.computeIfAbsent(categoryId, key -> new ArrayList<>()).add(supplier);
            }
        }
        return categorySupplierMap;
    }

    private List<Long> parseCategoryIds(String categoryIds) {
        if (categoryIds == null || categoryIds.isBlank()) {
            return List.of();
        }
        JSONArray array = JSONUtil.parseArray(categoryIds);
        List<Long> ids = new ArrayList<>();
        for (Object item : array) {
            ids.add(Long.valueOf(String.valueOf(item)));
        }
        return ids;
    }

    private void scanCategory(Long categoryId,
                              List<Supplier> suppliers,
                              Set<String> currentRiskKeys,
                              SupplierRiskScanJobResult result) {

        // 获取当前这一次循环中的多个供应商的共同的租户ID
        Long tenantId = suppliers.get(0).getTenantId();
        // 查询当前租户的 允许的最小的 供应商数量
        int minSupplierCount = queryIntConfig(tenantId, MIN_SUPPLIER_COUNT_CONFIG, DEFAULT_MIN_SUPPLIER_COUNT);
        // 查询当前租户的 允许的最低的 供应商的级别
        String minHealthyGrade = queryStringConfig(tenantId, MIN_HEALTHY_GRADE_CONFIG, DEFAULT_MIN_HEALTHY_GRADE);
        // 判断当去的供应商的数量 是否 达到了最小的供应商数量
        if (suppliers.size() < minSupplierCount) {
            // 存在风险
            String riskKey = riskKey(tenantId, categoryId, RISK_TYPE_SUPPLIER_COUNT_LOW);
            // 把已经筛选出来的风险 存储到 Set<String> currentRiskKeys
            // 查看当前租户 一共有多少个风险
            currentRiskKeys.add(riskKey);
            createRiskIfAbsent(tenantId, categoryId, RISK_TYPE_SUPPLIER_COUNT_LOW, "HIGH",
                    "该品类可用供应商数量不足，当前数量=" + suppliers.size() + "，租户配置最少数量=" + minSupplierCount,
                    "建议开发新的备选供应商。系统仅预警，不强制阻断采购。", suppliers, result);
        }
        // suppliers 某个租户的 某个分组下的 供应商 集合
        boolean allGradeLow = suppliers.stream()
                .allMatch(supplier -> gradeRank(safeGrade(supplier.getGrade())) < gradeRank(minHealthyGrade));
        if (allGradeLow) {
            String riskKey = riskKey(tenantId, categoryId, RISK_TYPE_ALL_SUPPLIER_GRADE_LOW);
            // 存储到 Set<String> currentRiskKeys
            currentRiskKeys.add(riskKey);
            // 风险事件 的插入
            createRiskIfAbsent(tenantId, categoryId, RISK_TYPE_ALL_SUPPLIER_GRADE_LOW, "HIGH",
                    "该品类所有可用供应商评级均低于" + minHealthyGrade + "级",
                    "建议优先开发A级及以上供应商。系统仅预警，不强制阻断采购。", suppliers, result);
        }
    }

    private void createRiskIfAbsent(Long tenantId,
                                    Long categoryId,
                                    String riskType,
                                    String riskLevel,
                                    String riskReason,
                                    String systemSuggestion,
                                    List<Supplier> suppliers,
                                    SupplierRiskScanJobResult result) {
        // 幂等性查询
        // 判断当前的风险是否已经存在
        SupplierRiskEvent existing = riskEventMapper.selectOne(new LambdaQueryWrapper<SupplierRiskEvent>()
                .eq(SupplierRiskEvent::getTenantId, tenantId)
                .eq(SupplierRiskEvent::getCategoryId, categoryId)
                .eq(SupplierRiskEvent::getRiskType, riskType)
                .eq(SupplierRiskEvent::getStatus, RISK_STATUS_OPEN)
                .eq(SupplierRiskEvent::getIsDeleted, 0)
                .last("LIMIT 1"));
        if (existing != null) {
            result.incrementExists();
            return;
        }
        SupplierRiskEvent riskEvent = new SupplierRiskEvent();
        riskEvent.setTenantId(tenantId);
        riskEvent.setCategoryId(categoryId);
        riskEvent.setRiskType(riskType);
        riskEvent.setRiskLevel(riskLevel);
        riskEvent.setRiskReason(riskReason);
        riskEvent.setSystemSuggestion(systemSuggestion);
        riskEvent.setSupplierCount(suppliers.size());
        riskEvent.setBestGrade(bestGrade(suppliers));
        riskEvent.setStatus(RISK_STATUS_OPEN);
        riskEvent.setFirstDetectedDate(LocalDate.now(clock));
        riskEvent.setLastDetectedDate(LocalDate.now(clock));
        // 风险事件表中新增一条记录
        riskEventMapper.insert(riskEvent);
        result.incrementCreated();
        sendRiskNotice(riskEvent);
    }

    private void sendRiskNotice(SupplierRiskEvent riskEvent) {
        String title = "多供应商策略风险预警";
        String content = "品类【" + riskEvent.getCategoryId() + "】存在风险：" + riskEvent.getRiskReason()
                + "。系统建议：" + riskEvent.getSystemSuggestion();
        notificationService.send(NotificationCommand.builder()
                .tenantId(riskEvent.getTenantId())
                .receiverType(NotificationConstants.RECEIVER_TYPE_ROLE)
                .receiverKey(NotificationConstants.ROLE_PURCHASE_MANAGER)
                .title(title)
                .content(content)
                .bizType(NotificationConstants.BIZ_TYPE_SUPPLIER_RISK)
                .bizId(riskEvent.getTenantId() + ":" + riskEvent.getCategoryId() + ":" + riskEvent.getRiskType())
                .priority(NotificationConstants.PRIORITY_HIGH)
                .build());
    }

    private void resolveStaleRiskEvents(Set<String> currentRiskKeys, SupplierRiskScanJobResult result) {
        // 查询所有的风险
        List<SupplierRiskEvent> activeEvents = riskEventMapper.selectList(new LambdaQueryWrapper<SupplierRiskEvent>()
                .eq(SupplierRiskEvent::getStatus, RISK_STATUS_OPEN) // 生效中的风险
                .eq(SupplierRiskEvent::getIsDeleted, 0)); // 未被删除的风险
        // 遍历每个风险
        for (SupplierRiskEvent activeEvent : activeEvents) {
            String key = riskKey(activeEvent.getTenantId(), activeEvent.getCategoryId(), activeEvent.getRiskType());
            if (currentRiskKeys.contains(key)) {
                // 说明 本次扫描到的风险 包含 数据库中的风险
                // 也就是说 本次扫描到的风险 已经在 数据库中存在了
                continue;
            }
            // 不包含
            // 代码走到这说明  扫描的风险 在数据库中
            // 本地循环到的这个数据库中的风险 在本次扫描中是不存在的
            SupplierRiskEvent update = new SupplierRiskEvent();
            update.setStatus(RISK_STATUS_RESOLVED); // 已解除
            update.setResolvedTime(LocalDateTime.now(clock));
            update.setIsDeleted(1); // 逻辑删除
            riskEventMapper.update(update, new LambdaUpdateWrapper<SupplierRiskEvent>()
                    .eq(SupplierRiskEvent::getId, activeEvent.getId())
                    .eq(SupplierRiskEvent::getStatus, RISK_STATUS_OPEN));
            result.incrementResolved();
        }
    }

    private int queryIntConfig(Long tenantId, String configKey, int defaultValue) {
        String configValue = queryStringConfig(tenantId, configKey, String.valueOf(defaultValue));
        try {
            return Integer.parseInt(configValue);
        } catch (NumberFormatException exception) {
            return defaultValue;
        }
    }

    private String queryStringConfig(Long tenantId, String configKey, String defaultValue) {
        SupplierTenantConfig config = tenantConfigMapper.selectOne(new LambdaQueryWrapper<SupplierTenantConfig>()
                .eq(SupplierTenantConfig::getTenantId, tenantId)
                .eq(SupplierTenantConfig::getConfigKey, configKey)
                .eq(SupplierTenantConfig::getEnabled, 1)
                .eq(SupplierTenantConfig::getIsDeleted, 0)
                .last("LIMIT 1"));
        return Optional.ofNullable(config)
                .map(SupplierTenantConfig::getConfigValue)
                .filter(value -> !value.isBlank())
                .orElse(defaultValue);
    }

    private String bestGrade(List<Supplier> suppliers) {
        return suppliers.stream()
                .map(supplier -> safeGrade(supplier.getGrade()))
                .max(Comparator.comparingInt(this::gradeRank))
                .orElse("C");
    }

    private int gradeRank(String grade) {
        return switch (safeGrade(grade)) {
            case "S" -> 4;
            case "A" -> 3;
            case "B" -> 2;
            default -> 1;
        };
    }

    private String safeGrade(String grade) {
        return grade == null ? "C" : grade;
    }

    private String riskKey(Long tenantId, Long categoryId, String riskType) {
        return tenantId + ":" + categoryId + ":" + riskType;
    }
}
