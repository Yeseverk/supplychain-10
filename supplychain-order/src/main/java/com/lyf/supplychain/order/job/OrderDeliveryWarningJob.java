package com.lyf.supplychain.order.job;

import com.lyf.supplychain.order.service.OrderMainService;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 订单超期发货预警任务。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Component
public class OrderDeliveryWarningJob {

    private static final Logger log = LoggerFactory.getLogger(OrderDeliveryWarningJob.class);

    private final OrderMainService orderService;

    public OrderDeliveryWarningJob(OrderMainService orderService) {
        this.orderService = orderService;
    }

    /**
     * 扫描即将超期的待发货订单。
     */
    @XxlJob("orderDeliveryWarningJob")
    public void orderDeliveryWarningJob() {
        int count = orderService.scanDeliveryWarnings();
        log.info("订单超期发货预警任务完成，warningCount={}", count);
    }
}
