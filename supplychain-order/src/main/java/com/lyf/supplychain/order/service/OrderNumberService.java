package com.lyf.supplychain.order.service;

/**
 * 订单编号服务。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
public interface OrderNumberService {

    /**
     * 生成业务编号。
     *
     * @param prefix 编号前缀
     * @return 业务编号
     */
    String nextNo(String prefix);
}
