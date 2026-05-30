package com.lyf.supplychain.warehouse.service;

import java.util.Map;

/**
 * WMS 报表服务。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
public interface WmsReportService {

    /**
     * 查询库存总览。
     *
     * @return 总览指标
     */
    Map<String, Object> overview();

    /**
     * 查询库存健康度。
     *
     * @return 健康度指标
     */
    Map<String, Object> health();

    /**
     * 查询出入库趋势。
     *
     * @return 趋势指标
     */
    Map<String, Object> trend();
}
