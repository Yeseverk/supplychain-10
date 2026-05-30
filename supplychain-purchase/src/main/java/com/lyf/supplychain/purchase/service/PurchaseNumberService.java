package com.lyf.supplychain.purchase.service;

/**
 * 采购单据编号服务。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
public interface PurchaseNumberService {

    /**
     * 生成采购模块业务单号。
     *
     * @param prefix 单号前缀
     * @return 业务单号
     */
    String nextNo(String prefix);
}
