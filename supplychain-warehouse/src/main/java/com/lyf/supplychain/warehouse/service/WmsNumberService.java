package com.lyf.supplychain.warehouse.service;

/**
 * WMS 单号生成服务。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
public interface WmsNumberService {

    /**
     * 生成 WMS 业务单号。
     *
     * @param prefix 单号前缀
     * @return 业务单号
     */
    String nextNo(String prefix);
}
