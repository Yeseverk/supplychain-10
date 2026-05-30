package com.lyf.supplychain.supplier.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.lyf.supplychain.supplier.constant.NotificationConstants;
import com.lyf.supplychain.supplier.constant.SupplierStatus;
import com.lyf.supplychain.supplier.entity.Supplier;
import com.lyf.supplychain.supplier.entity.SupplierScoreLog;
import com.lyf.supplychain.supplier.mapper.SupplierMapper;
import com.lyf.supplychain.supplier.mapper.SupplierPerformanceDataMapper;
import com.lyf.supplychain.supplier.mapper.SupplierScoreLogMapper;
import com.lyf.supplychain.supplier.model.NotificationCommand;
import com.lyf.supplychain.supplier.model.SupplierMonthlyScore;
import com.lyf.supplychain.supplier.model.SupplierPerformanceMetrics;
import com.lyf.supplychain.supplier.service.NotificationService;
import com.lyf.supplychain.supplier.service.SupplierPerformanceScoreCalculator;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 供应商月度绩效评分 XXL-JOB 任务。
 *
 * @author liyunfei
 * @date 2026-05-18
 */
@Slf4j
@Component
public class SupplierPerformanceScoreJob {

    private static final DateTimeFormatter SCORE_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

    private static final String NO_PURCHASE_REMARK = "本月无采购数据，评分沿用上月";

    private static final String NEW_SUPPLIER_REMARK = "新供应商历史数据不足1个月，等待下月评分";

    private final SupplierMapper supplierMapper;

    private final SupplierScoreLogMapper scoreLogMapper;

    private final SupplierPerformanceDataMapper performanceDataMapper;

    private final SupplierPerformanceScoreCalculator scoreCalculator;

    private final NotificationService notificationService;

    private final Clock clock;

    public SupplierPerformanceScoreJob(SupplierMapper supplierMapper,
                                       SupplierScoreLogMapper scoreLogMapper,
                                       SupplierPerformanceDataMapper performanceDataMapper,
                                       SupplierPerformanceScoreCalculator scoreCalculator,
                                       NotificationService notificationService,
                                       Clock clock) {
        this.supplierMapper = supplierMapper;
        this.scoreLogMapper = scoreLogMapper;
        this.performanceDataMapper = performanceDataMapper;
        this.scoreCalculator = scoreCalculator;
        this.notificationService = notificationService;
        this.clock = clock;
    }

    /**
     * XXL-JOB 入口，每月计算上月供应商绩效评分。
     */
    @XxlJob("supplierPerformanceScoreJob")
    public void execute() {
        // 按月执行计算
        SupplierPerformanceJobResult result = executeMonthlyScore();
        // 每个月的评分/评级 都要有记录到数据库
        String message = "供应商绩效评分任务完成，scoreMonth=" + result.getScoreMonth()
                + "，扫描数量=" + result.getScannedCount()
                + "，评分数量=" + result.getScoredCount()
                + "，跳过数量=" + result.getSkippedCount()
                + "，评级变化数量=" + result.getChangedCount()
                + "，失败数量=" + result.getFailedCount();
        XxlJobHelper.log(message);
        XxlJobHelper.handleSuccess(message);
    }

    /**
     * 执行月度供应商绩效评分。
     *
     * @return 任务执行结果
     */
    public SupplierPerformanceJobResult executeMonthlyScore() {
        // 获取当前日期
        LocalDate executeDate = LocalDate.now(clock);
        // 获取上月日期
        LocalDate scoreMonthDate = executeDate.minusMonths(1).withDayOfMonth(1);
        // 日期格式化
        String scoreMonth = scoreMonthDate.format(SCORE_MONTH_FORMATTER);
        // 创建结果集
        SupplierPerformanceJobResult result = new SupplierPerformanceJobResult();
        // 哪个月的评分/评级
        result.setScoreMonth(scoreMonth);
        // 查询所有的已通过审核且未被删除的供应商列表
        List<Supplier> suppliers = queryApprovedSuppliers();
        for (Supplier supplier : suppliers) {
            // 每计算一个 累加 CAS 操作
            result.incrementScanned();
            try {
                // 根据每个供应商 去做计算得分
                processSupplier(supplier, scoreMonthDate, scoreMonth, result);
            } catch (Exception exception) {
                // 计算异常次数 累加 CAS 操作
                result.incrementFailed();
                log.error("供应商绩效评分失败，supplierId={}, scoreMonth={}", supplier.getId(), scoreMonth, exception);
            }
        }
        log.info("供应商月度绩效汇总报告：scoreMonth={}, scanned={}, scored={}, skipped={}, changed={}, failed={}",
                result.getScoreMonth(), result.getScannedCount(), result.getScoredCount(),
                result.getSkippedCount(), result.getChangedCount(), result.getFailedCount());
        return result;
    }

    private List<Supplier> queryApprovedSuppliers() {
        return supplierMapper.selectList(new LambdaQueryWrapper<Supplier>()
                .eq(Supplier::getStatus, SupplierStatus.APPROVED.getCode())
                .eq(Supplier::getIsDeleted, 0));
    }

    /**
     * 每个供应商评分计算实现
     *
     * @param supplier
     * @param scoreMonthDate
     * @param scoreMonth
     * @param result
     */
    private void processSupplier(Supplier supplier,
                                 LocalDate scoreMonthDate,
                                 String scoreMonth,
                                 SupplierPerformanceJobResult result) {
        // 幂等校验
        // 如果该供应商 当前月份已经存在了 就不会重复计算
        if (isScoreLogExists(supplier.getId(), scoreMonth)) {
            result.incrementSkipped();
            log.info("供应商绩效评分已存在，跳过重复计算，supplierId={}, scoreMonth={}", supplier.getId(), scoreMonth);
            return;
        }
        // 如果是新供应商 跳过 下个月再计算
        if (isNewSupplier(supplier, scoreMonthDate)) {
            writeKeepPreviousScoreLog(supplier, scoreMonth, NEW_SUPPLIER_REMARK);
            result.incrementSkipped();
            return;
        }
        // 开始统计四个维度的数据
        SupplierPerformanceMetrics metrics = queryMetrics(supplier, scoreMonth);
        // 分析 计算 四个维度的数据
        SupplierMonthlyScore monthlyScore = scoreCalculator.calculate(metrics);
        // 如果本月没有采购单 沿用上个月的评分信息
        if (!monthlyScore.isPurchaseDataEnough()) {
            writeKeepPreviousScoreLog(supplier, scoreMonth, NO_PURCHASE_REMARK);
            result.incrementSkipped();
            return;
        }
        // 设置默认评级为 C
        String prevGrade = safeGrade(supplier.getGrade());
        // 判断当前评级是否和上月评级相同
        boolean gradeChanged = !Objects.equals(prevGrade, monthlyScore.getGrade());
        // 写入 评分/评级 日志表
        insertScoreLog(supplier, scoreMonth, metrics, monthlyScore, prevGrade, gradeChanged);
        // 更新 月度 评分信息
        updateSupplierScore(supplier.getId(), monthlyScore, scoreMonth);
        // 计入结果 评分次数
        result.incrementScored();
        // 如果不相同 发送级别变更通知
        if (gradeChanged) {
            result.incrementChanged();
            sendGradeChangedNotice(supplier, prevGrade, monthlyScore.getGrade(), monthlyScore.getTotalScore());
        }
    }

    private boolean isScoreLogExists(Long supplierId, String scoreMonth) {
        Long count = scoreLogMapper.selectCount(new LambdaQueryWrapper<SupplierScoreLog>()
                .eq(SupplierScoreLog::getSupplierId, supplierId)
                .eq(SupplierScoreLog::getScoreMonth, scoreMonth));
        return Optional.ofNullable(count).orElse(0L) > 0;
    }

    private boolean isNewSupplier(Supplier supplier, LocalDate scoreMonthDate) {
        LocalDateTime createTime = supplier.getCreateTime();
        return createTime != null && !createTime.toLocalDate().isBefore(scoreMonthDate);
    }

    private SupplierPerformanceMetrics queryMetrics(Supplier supplier, String scoreMonth) {
        // 租户ID
        Long tenantId = supplier.getTenantId();
        // 供应商ID
        Long supplierId = supplier.getId();
        // 查询供应商当月采购到货指标
        SupplierPerformanceMetrics deliveryMetrics = Optional
                .ofNullable(performanceDataMapper.selectDeliveryMetrics(tenantId, supplierId, scoreMonth))
                .orElseGet(SupplierPerformanceMetrics::new);
        // 查询供应商质检指标
        SupplierPerformanceMetrics qualityMetrics = Optional
                .ofNullable(performanceDataMapper.selectQualityMetrics(tenantId, supplierId, scoreMonth))
                .orElseGet(SupplierPerformanceMetrics::new);
        SupplierPerformanceMetrics metrics = new SupplierPerformanceMetrics();
        metrics.setTotalOrders(safeInteger(deliveryMetrics.getTotalOrders()));
        metrics.setDeliveredOnTime(safeInteger(deliveryMetrics.getDeliveredOnTime()));
        metrics.setQualityTotal(safeInteger(qualityMetrics.getQualityTotal()));
        metrics.setQualityPassed(safeInteger(qualityMetrics.getQualityPassed()));
        // 查询供应商平均响应时间
        metrics.setResponseHoursAvg(performanceDataMapper.selectResponseHoursAvg(tenantId, supplierId, scoreMonth));
        // 查询供应商采购价
        metrics.setPriceComparison(performanceDataMapper.selectPriceComparison(tenantId, supplierId, scoreMonth));
        return metrics;
    }

    private void writeKeepPreviousScoreLog(Supplier supplier, String scoreMonth, String remark) {
        SupplierScoreLog scoreLog = buildBaseScoreLog(supplier, scoreMonth);
        scoreLog.setTotalScore(safeScore(supplier.getScore()));
        scoreLog.setGrade(safeGrade(supplier.getGrade()));
        scoreLog.setPrevGrade(safeGrade(supplier.getGrade()));
        scoreLog.setGradeChanged(0);
        scoreLog.setCalcRemark(remark);
        scoreLogMapper.insert(scoreLog);
    }

    private void insertScoreLog(Supplier supplier,
                                String scoreMonth,
                                SupplierPerformanceMetrics metrics,
                                SupplierMonthlyScore monthlyScore,
                                String prevGrade,
                                boolean gradeChanged) {
        SupplierScoreLog scoreLog = buildBaseScoreLog(supplier, scoreMonth);
        scoreLog.setTotalOrders(safeInteger(metrics.getTotalOrders()));
        scoreLog.setDeliveredOnTime(safeInteger(metrics.getDeliveredOnTime()));
        scoreLog.setQualityPassed(safeInteger(metrics.getQualityPassed()));
        scoreLog.setQualityTotal(safeInteger(metrics.getQualityTotal()));
        scoreLog.setResponseHoursAvg(metrics.getResponseHoursAvg());
        scoreLog.setPriceComparison(metrics.getPriceComparison());
        scoreLog.setDeliveryScore(monthlyScore.getDeliveryScore());
        scoreLog.setQualityScore(monthlyScore.getQualityScore());
        scoreLog.setResponseScore(monthlyScore.getResponseScore());
        scoreLog.setPriceScore(monthlyScore.getPriceScore());
        scoreLog.setTotalScore(monthlyScore.getTotalScore());
        scoreLog.setGrade(monthlyScore.getGrade());
        scoreLog.setPrevGrade(prevGrade);
        scoreLog.setGradeChanged(gradeChanged ? 1 : 0);
        scoreLog.setCalcRemark("月度绩效评分计算完成");
        scoreLogMapper.insert(scoreLog);
    }

    private SupplierScoreLog buildBaseScoreLog(Supplier supplier, String scoreMonth) {
        SupplierScoreLog scoreLog = new SupplierScoreLog();
        scoreLog.setTenantId(supplier.getTenantId());
        scoreLog.setSupplierId(supplier.getId());
        scoreLog.setScoreMonth(scoreMonth);
        scoreLog.setTotalOrders(0);
        scoreLog.setDeliveredOnTime(0);
        scoreLog.setQualityPassed(0);
        scoreLog.setQualityTotal(0);
        scoreLog.setDeliveryScore(BigDecimal.ZERO);
        scoreLog.setQualityScore(BigDecimal.ZERO);
        scoreLog.setResponseScore(BigDecimal.ZERO);
        scoreLog.setPriceScore(BigDecimal.ZERO);
        scoreLog.setTotalScore(BigDecimal.ZERO);
        scoreLog.setGrade("C");
        scoreLog.setGradeChanged(0);
        scoreLog.setCalcTime(LocalDateTime.now(clock));
        return scoreLog;
    }

    private void updateSupplierScore(Long supplierId, SupplierMonthlyScore monthlyScore, String scoreMonth) {
        Supplier update = new Supplier();
        update.setGrade(monthlyScore.getGrade());
        update.setScore(monthlyScore.getTotalScore());
        update.setLastScoreMonth(scoreMonth);
        supplierMapper.update(update, new LambdaUpdateWrapper<Supplier>()
                .eq(Supplier::getId, supplierId)
                .eq(Supplier::getIsDeleted, 0));
    }

    private void sendGradeChangedNotice(Supplier supplier, String prevGrade, String currentGrade, BigDecimal totalScore) {
        String title = "供应商评级变化通知";
        String content = "供应商【" + supplier.getSupplierName() + "】评级从 " + prevGrade
                + " 调整为 " + currentGrade + "，最新评分：" + totalScore;
        notificationService.send(NotificationCommand.builder()
                .tenantId(supplier.getTenantId())
                .receiverType(NotificationConstants.RECEIVER_TYPE_ROLE)
                .receiverKey(NotificationConstants.ROLE_PURCHASE_SPECIALIST)
                .title(title)
                .content(content)
                .bizType(NotificationConstants.BIZ_TYPE_SUPPLIER_SCORE)
                .bizId(String.valueOf(supplier.getId()))
                .priority(NotificationConstants.PRIORITY_HIGH)
                .mailTo(supplier.getContactEmail())
                .mailSubject(title)
                .mailContent(content)
                .build());
    }

    private String safeGrade(String grade) {
        return grade == null ? "C" : grade;
    }

    private BigDecimal safeScore(BigDecimal score) {
        return score == null ? BigDecimal.ZERO : score;
    }

    private Integer safeInteger(Integer value) {
        return value == null ? 0 : value;
    }
}
