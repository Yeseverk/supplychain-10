package com.lyf.supplychain.order.service;

import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.order.entity.OrderLog;
import com.lyf.supplychain.order.entity.OrderMain;
import com.lyf.supplychain.order.request.OrderCancelRequest;
import com.lyf.supplychain.order.request.OrderCreateRequest;
import com.lyf.supplychain.order.request.OrderFlagRequest;
import com.lyf.supplychain.order.request.OrderMergeRequest;
import com.lyf.supplychain.order.request.OrderPageQuery;
import com.lyf.supplychain.order.request.OrderSplitRequest;
import com.lyf.supplychain.order.request.WebhookRequest;

import java.util.List;
import java.util.Map;

/**
 * 订单主流程业务服务。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
public interface OrderMainService {

    /**
     * 分页查询订单。
     *
     * @param query 分页参数
     * @return 订单分页结果
     */
    PageResult<OrderMain> page(OrderPageQuery query);

    /**
     * 查询订单详情。
     *
     * @param id 订单ID
     * @return 订单
     */
    OrderMain detail(Long id);

    /**
     * 手动创建订单并执行幂等与 Redis 防超卖。
     *
     * @param request 创建请求
     * @return 订单ID
     */
    Long create(OrderCreateRequest request);

    /**
     * 取消订单。
     *
     * @param id      订单ID
     * @param request 取消请求
     */
    void cancel(Long id, OrderCancelRequest request);

    /**
     * 风控审核通过。
     *
     * @param id 订单ID
     */
    void approve(Long id);

    /**
     * 风控审核拒绝。
     *
     * @param id 订单ID
     */
    void reject(Long id);

    /**
     * 标记异常订单。
     *
     * @param id      订单ID
     * @param request 异常请求
     */
    void flag(Long id, OrderFlagRequest request);

    /**
     * 同步平台状态。
     *
     * @param id 订单ID
     */
    void sync(Long id);

    /**
     * 查询订单操作日志。
     *
     * @param id 订单ID
     * @return 日志列表
     */
    List<OrderLog> logs(Long id);

    /**
     * 拆分订单。
     *
     * @param id      订单ID
     * @param request 拆单请求
     * @return 新订单ID
     */
    Long split(Long id, OrderSplitRequest request);

    /**
     * 合并订单。
     *
     * @param request 合单请求
     * @return 主订单ID
     */
    Long merge(OrderMergeRequest request);

    /**
     * 接收平台 Webhook 并保存原始报文。
     *
     * @param platform 平台
     * @param request  Webhook 请求
     */
    void webhook(String platform, WebhookRequest request);

    /**
     * 导入平台原始订单报文并标准化为 OMS 订单。
     *
     * @param platform 平台
     * @param rawData  原始报文
     * @return OMS 订单ID
     */
    Long importPlatformOrder(String platform, String rawData);

    /**
     * WMS 出库完成回调推进订单状态。
     *
     * @param orderNo    订单号
     * @param outboundNo 出库单号
     */
    void outboundCallback(String orderNo, String outboundNo);

    /**
     * TMS 物流状态回调推进订单物流状态。
     *
     * @param orderNo         订单号
     * @param waybillNo       TMS内部运单号
     * @param trackingNo      物流商追踪号
     * @param logisticsStatus TMS物流状态
     */
    void logisticsCallback(String orderNo, String waybillNo, String trackingNo, Integer logisticsStatus);

    /**
     * 订单概览报表。
     *
     * @return 概览数据
     */
    Map<String, Object> overview();

    /**
     * 今日订单报表。
     *
     * @return 今日数据
     */
    Map<String, Object> today();

    /**
     * 平台同步日志。
     *
     * @return 同步日志
     */
    List<Map<String, Object>> syncLogs();

    /**
     * 扫描超期发货风险订单。
     *
     * @return 风险订单数
     */
    int scanDeliveryWarnings();
}
