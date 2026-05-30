package com.lyf.supplychain.logistics.service;

/**
 * 物流编号服务。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
public interface LogisticsNumberService {

    /**
     * 生成业务编号。
     *
     * @param prefix 编号前缀
     * @return 业务编号
     */
    String nextNo(String prefix);
}
