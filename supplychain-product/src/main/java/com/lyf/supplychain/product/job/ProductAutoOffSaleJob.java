package com.lyf.supplychain.product.job;

import com.lyf.supplychain.product.service.ProductSpuService;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 商品自动下架定时任务。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Component
public class ProductAutoOffSaleJob {

    private static final Logger log = LoggerFactory.getLogger(ProductAutoOffSaleJob.class);

    private final ProductSpuService spuService;

    public ProductAutoOffSaleJob(ProductSpuService spuService) {
        this.spuService = spuService;
    }

    /**
     * 扫描定时下架商品并执行状态流转。
     */
    @XxlJob("productAutoOffSaleJob")
    public void productAutoOffSaleJob() {
        int count = spuService.autoOffSaleExpired();
        log.info("商品自动下架任务完成，offSaleCount={}", count);
    }
}
