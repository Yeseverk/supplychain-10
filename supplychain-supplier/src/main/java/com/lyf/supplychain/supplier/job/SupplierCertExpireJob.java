package com.lyf.supplychain.supplier.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.lyf.supplychain.supplier.constant.NotificationConstants;
import com.lyf.supplychain.supplier.entity.Supplier;
import com.lyf.supplychain.supplier.entity.SupplierCert;
import com.lyf.supplychain.supplier.mapper.SupplierCertMapper;
import com.lyf.supplychain.supplier.mapper.SupplierMapper;
import com.lyf.supplychain.supplier.model.NotificationCommand;
import com.lyf.supplychain.supplier.service.NotificationService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

/**
 * 供应商资质到期提醒 XXL-JOB 任务。
 *
 * @author liyunfei
 * @date 2026-05-18
 */
@Slf4j
@Component
public class SupplierCertExpireJob {

    private static final Set<Long> NOTICE_DAYS = Set.of(30L, 7L, 1L);

    private final SupplierCertMapper certMapper;

    private final SupplierMapper supplierMapper;

    private final NotificationService notificationService;

    private final ThreadPoolTaskExecutor certExpireExecutor;

    private final Clock clock;

    public SupplierCertExpireJob(SupplierCertMapper certMapper,
                                 SupplierMapper supplierMapper,
                                 NotificationService notificationService,
                                 @Qualifier("supplierCertExpireExecutor") ThreadPoolTaskExecutor certExpireExecutor,
                                 Clock clock) {
        this.certMapper = certMapper;
        this.supplierMapper = supplierMapper;
        this.notificationService = notificationService;
        this.certExpireExecutor = certExpireExecutor;
        this.clock = clock;
    }

    /**
     * XXL-JOB 入口，按调度中心传入的分片参数扫描资质到期数据。
     */
    @XxlJob("supplierCertExpireJob")
    public void execute() {
        // 如果任务的路由策略是 分片广播 那么这边就可以获取总的分片数量 和 当前的分片的索引号
        // 当前分片的索引号
        int shardIndex = XxlJobHelper.getShardIndex();
        // 总的分片数量
        int shardTotal = XxlJobHelper.getShardTotal();
        // 扫描
        SupplierCertExpireJobResult result = scanCertExpire(shardIndex, shardTotal);
        String message = "供应商资质到期扫描完成，分片=" + shardIndex + "/" + shardTotal
                + "，处理数量=" + result.getScannedCount()
                + "，过期数量=" + result.getExpiredCount()
                + "，通知数量=" + result.getNoticeCount()
                + "，失败数量=" + result.getFailedCount();
        XxlJobHelper.log(message);
        XxlJobHelper.handleSuccess(message);
    }

    /**
     * 使用传统线程池按分片扫描并处理供应商资质到期数据。
     *
     * @param shardIndex 分片序号
     * @param shardTotal 分片总数
     * @return 任务执行结果
     */
    public SupplierCertExpireJobResult scanCertExpire(int shardIndex, int shardTotal) {
        // 根据分片数量和当前的分片索引号 查询所有资质
        // 按照分片的数量 进行查询资质文件
        List<SupplierCert> certs = queryUnexpiredCerts(shardIndex, shardTotal);
        // 扫描的结果封装Bean
        SupplierCertExpireJobResult result = new SupplierCertExpireJobResult();
        List<Future<?>> futures = new ArrayList<>(certs.size());
        for (SupplierCert cert : certs) {
            try {
                // submit 是有返回值的 拿到异常 和 结果
                // get 阻塞式
                futures.add(certExpireExecutor.submit(() -> processCertSafely(cert, result)));
            } catch (RejectedExecutionException exception) {
                result.incrementFailed();
                log.error("供应商资质到期任务提交线程池失败，certId={}", cert.getId(), exception);
            }
        }
        // 当所有的线程都执行完成后
        // 怎么判断 所有的线程都已经执行完毕了 ??
        // submit 可以通过 future.get(); 来阻塞主线程的执行 等待子任务执行完成
        waitForTraditionalTasks(futures, result);
        logScanResult(shardIndex, shardTotal, result);
        return result;
    }

    /**
     * 使用 CompletableFuture 按分片扫描并处理供应商资质到期数据。
     *
     * @param shardIndex 分片序号
     * @param shardTotal 分片总数
     * @return 任务执行结果
     */
    public SupplierCertExpireJobResult scanCertExpireWithCompletableFuture(int shardIndex, int shardTotal) {
        // 根据分片数量和当前的分片索引号 查询所有资质
        // 按照分片的数量 进行查询资质文件
        List<SupplierCert> certs = queryUnexpiredCerts(shardIndex, shardTotal);
        // 扫描的结果封装Bean
        SupplierCertExpireJobResult result = new SupplierCertExpireJobResult();
        List<CompletableFuture<Void>> futures = certs.stream()
                // CompletableFuture.runAsync 异步处理 原子性更新 也就是说多个线程同时处理扫描到的数据 也不会出现 result 的并发问题
                .map(cert -> CompletableFuture.runAsync(() -> processCert(cert, result), certExpireExecutor)
                        // 如果出现了异常
                        .exceptionally(exception -> {
                            // 记录了异常的失败次数
                            // 如果失败了 需要重新 processCert 吗 ??
                            // 不需要 当天失败了 明天还会再处理一次 1天 处理失败了 明天就过期了
                            result.incrementFailed();
                            log.error("供应商资质到期处理失败，certId={}", cert.getId(), exception);
                            return null;
                        }))
                .toList();
        // 阻塞操作 等待所有的任务都执行完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        // 如果要处理失败 有两种做法
        // 第一种是 exception 中直接使用 try catch 在 catch 中进行处理 重试 重新判断
        // 第二种是 在所有的线程都执行完成后 判断失败的总的次数 > 0 如果是 就根据失败的资质的ID 针对性的去判断
        // 第三种是 不处理  等着下一次 定时任务的触发 到时候再重新处理
        // 如果我们要实现增量 或者 需要记录每天的任务结果 就需要创建一个MySQL的表 来存储下面的数据
        // log 可以直接写入到文件中 也算是持久化了
        logScanResult(shardIndex, shardTotal, result);
        return result;
    }

    private void processCertSafely(SupplierCert cert, SupplierCertExpireJobResult result) {
        try {
            processCert(cert, result);
        } catch (Exception exception) {
            result.incrementFailed();
            log.error("供应商资质到期处理失败，certId={}", cert.getId(), exception);
        }
    }

    private void waitForTraditionalTasks(List<Future<?>> futures, SupplierCertExpireJobResult result) {
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                result.incrementFailed();
                log.error("供应商资质到期线程等待被中断", exception);
                return;
            } catch (ExecutionException exception) {
                result.incrementFailed();
                log.error("供应商资质到期线程执行异常", exception);
            }
        }
        // 这个循环走完 也就意味着 所有的子线程都已经执行完毕了
    }

    private void logScanResult(int shardIndex, int shardTotal, SupplierCertExpireJobResult result) {
        log.info("供应商资质到期扫描完成，shardIndex={}, shardTotal={}, scanned={}, expired={}, notice={}, failed={}",
                shardIndex, shardTotal, result.getScannedCount(), result.getExpiredCount(),
                result.getNoticeCount(), result.getFailedCount());
    }

    private List<SupplierCert> queryUnexpiredCerts(int shardIndex, int shardTotal) {
        LambdaQueryWrapper<SupplierCert> wrapper = new LambdaQueryWrapper<SupplierCert>()
                .eq(SupplierCert::getIsExpired, 0) // 过期状态为0 代表未过期 或者是 已过期但是还没有修改这个状态
                .isNotNull(SupplierCert::getExpireDate) // 过期时间不为空
                .eq(SupplierCert::getIsDeleted, 0); // 未被逻辑删除的
        if (shardTotal > 1) { // 判断我们的服务器数量 是否是集群 如果是单机 就不走分片广播
            // 如果是集群 至少两台服务器 才会走这个条件
            // id % shardTotal = shardIndex;
            wrapper.apply("MOD(id, {0}) = {1}", shardTotal, shardIndex);
        }
        return certMapper.selectList(wrapper);
    }

    private void processCert(SupplierCert cert, SupplierCertExpireJobResult result) {
        // 扫描的结果集数量 使用CAS更新 原子类更新
        result.incrementScanned();
        // 获取当前日期
        LocalDate today = LocalDate.now(clock);
        // 比较两个日期
        long daysLeft = ChronoUnit.DAYS.between(today, cert.getExpireDate());
        if (daysLeft < 0) {
            // 如果资质的过期时间 < 当前日期 说明已经过期
            markExpired(cert);
            // 把已过期的数量 CAS 更新
            result.incrementExpired();
            return;
        }
        // 30L, 7L, 1L
        // 判断当前资质的过期时间 是否属于 上面的三个值
        // 如果是 就需要触发 通知
        if (NOTICE_DAYS.contains(daysLeft)) {
            // 发送通知
            sendNotice(cert, daysLeft);
            // 把需要发送通知的数量 做CAS更新
            result.incrementNotice();
        }
    }

    private void markExpired(SupplierCert cert) {
        SupplierCert update = new SupplierCert();
        update.setIsExpired(1); // 1 代表是资质已经过期了
        certMapper.update(update, new LambdaUpdateWrapper<SupplierCert>()
                .eq(SupplierCert::getId, cert.getId())
                .eq(SupplierCert::getIsExpired, 0));
    }

    private void sendNotice(SupplierCert cert, long daysLeft) {
        String target = switch ((int) daysLeft) {
            case 30 -> "采购专员";
            case 7 -> "采购专员和采购负责人";
            case 1 -> "采购专员、采购负责人和租户管理员";
            default -> "采购专员";
        };
        Supplier supplier = supplierMapper.selectById(cert.getSupplierId());
        String title = "供应商资质到期提醒";
        String content = "供应商资质【" + cert.getCertName() + "】将在 " + daysLeft
                + " 天后到期，到期日期：" + cert.getExpireDate() + "，通知对象：" + target;
        notificationService.send(NotificationCommand.builder()
                .tenantId(cert.getTenantId())
                .receiverType(NotificationConstants.RECEIVER_TYPE_ROLE)
                .receiverKey(resolveReceiverRole(daysLeft))
                .title(title)
                .content(content)
                .bizType(NotificationConstants.BIZ_TYPE_SUPPLIER_CERT)
                .bizId(String.valueOf(cert.getId()))
                .priority(resolvePriority(daysLeft))
                .mailTo(supplier == null ? null : supplier.getContactEmail())
                .mailSubject(title)
                .mailContent(content)
                .build());
    }

    private String resolveReceiverRole(long daysLeft) {
        if (daysLeft == 1L) {
            return NotificationConstants.ROLE_TENANT_ADMIN;
        }
        if (daysLeft == 7L) {
            return NotificationConstants.ROLE_PURCHASE_MANAGER;
        }
        return NotificationConstants.ROLE_PURCHASE_SPECIALIST;
    }

    private String resolvePriority(long daysLeft) {
        if (daysLeft == 1L) {
            return NotificationConstants.PRIORITY_URGENT;
        }
        if (daysLeft == 7L) {
            return NotificationConstants.PRIORITY_HIGH;
        }
        return NotificationConstants.PRIORITY_NORMAL;
    }
}
