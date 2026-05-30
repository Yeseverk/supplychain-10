package com.lyf.supplychain.finance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lyf.supplychain.common.event.EventConstants;
import com.lyf.supplychain.common.notification.ReliableNotificationCommand;
import com.lyf.supplychain.common.notification.ReliableNotificationEventPublisher;
import com.lyf.supplychain.finance.config.FinancePayableReminderProperties;
import com.lyf.supplychain.finance.entity.FinancePayable;
import com.lyf.supplychain.finance.mapper.FinancePayableMapper;
import com.lyf.supplychain.finance.model.FinancePayableDueReminderResult;
import com.lyf.supplychain.finance.service.FinancePayableReminderService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 应付账款到期提醒服务实现。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Service
public class FinancePayableReminderServiceImpl implements FinancePayableReminderService {

    private final FinancePayableMapper payableMapper;

    private final FinancePayableReminderProperties properties;

    private final ReliableNotificationEventPublisher notificationEventPublisher;

    public FinancePayableReminderServiceImpl(FinancePayableMapper payableMapper,
                                             FinancePayableReminderProperties properties,
                                             ReliableNotificationEventPublisher notificationEventPublisher) {
        this.payableMapper = payableMapper;
        this.properties = properties;
        this.notificationEventPublisher = notificationEventPublisher;
    }

    /**
     * 扫描即将到期或已逾期的应付账款并发送提醒。
     *
     * @return 执行结果
     */
    @Override
    public FinancePayableDueReminderResult scanDuePayables() {
        FinancePayableDueReminderResult result = new FinancePayableDueReminderResult();
        LocalDate today = LocalDate.now();
        LocalDate warningDate = today.plusDays(properties.getPayableWarningDays());
        List<FinancePayable> payables = payableMapper.selectList(new LambdaQueryWrapper<FinancePayable>()
                .in(FinancePayable::getStatus, 0, 1, 2)
                .le(FinancePayable::getDueDate, warningDate)
                .orderByAsc(FinancePayable::getDueDate));
        for (FinancePayable payable : payables) {
            result.incrementScanned();
            long daysLeft = ChronoUnit.DAYS.between(today, payable.getDueDate());
            if (daysLeft < 0) {
                payable.setOverdueDays((int) Math.abs(daysLeft));
                payableMapper.updateById(payable);
                result.incrementOverdueUpdated();
            }
            publishReminder(payable, daysLeft);
            result.incrementNotified();
        }
        return result;
    }

    private void publishReminder(FinancePayable payable, long daysLeft) {
        String title = daysLeft < 0 ? "应付账款已逾期" : "应付账款即将到期";
        String content = "采购单【" + payable.getPoNo() + "】应付账款"
                + (daysLeft < 0 ? "已逾期" + Math.abs(daysLeft) + "天" : "将在" + daysLeft + "天后到期")
                + "，金额：" + payable.getPayableAmount() + " " + payable.getCurrency()
                + "，请及时安排付款。";
        notificationEventPublisher.publish(ReliableNotificationCommand.builder()
                .tenantId(payable.getTenantId())
                .receiverType(EventConstants.Notification.RECEIVER_TYPE_ROLE)
                .receiverKey(EventConstants.ReceiverRole.FINANCE_MANAGER)
                .title(title)
                .content(content)
                .bizType(EventConstants.BizType.FMS_PAYABLE_DUE_WARNING)
                .bizId(String.valueOf(payable.getId()))
                .priority(EventConstants.Notification.PRIORITY_HIGH)
                .sourceService(EventConstants.SourceService.FINANCE)
                .build());
    }
}
