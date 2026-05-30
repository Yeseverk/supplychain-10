package com.lyf.supplychain.logistics.service;

import com.lyf.supplychain.common.api.PageQuery;
import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.logistics.entity.LogisticsCarrier;
import com.lyf.supplychain.logistics.entity.LogisticsChannel;
import com.lyf.supplychain.logistics.entity.LogisticsFeeRecord;
import com.lyf.supplychain.logistics.entity.LogisticsReturn;
import com.lyf.supplychain.logistics.entity.LogisticsTrack;
import com.lyf.supplychain.logistics.entity.LogisticsWaybill;
import com.lyf.supplychain.logistics.request.CarrierRequest;
import com.lyf.supplychain.logistics.request.ChannelRequest;
import com.lyf.supplychain.logistics.request.FeeEstimateRequest;
import com.lyf.supplychain.logistics.request.LogisticsPageQuery;
import com.lyf.supplychain.logistics.request.RateRequest;
import com.lyf.supplychain.logistics.request.RecommendRequest;
import com.lyf.supplychain.logistics.request.ReturnRequest;
import com.lyf.supplychain.logistics.request.WaybillBatchRequest;
import com.lyf.supplychain.logistics.request.WaybillRequest;
import com.lyf.supplychain.logistics.request.WaybillStatusRequest;
import com.lyf.supplychain.logistics.request.WebhookTrackRequest;

import java.util.List;
import java.util.Map;

/**
 * 物流管理业务服务。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
public interface LogisticsService {

    /**
     * 分页查询物流商。
     *
     * @param query 分页参数
     * @return 物流商分页结果
     */
    PageResult<LogisticsCarrier> pageCarriers(PageQuery query);

    /**
     * 创建物流商。
     *
     * @param request 物流商请求
     * @return 物流商ID
     */
    Long createCarrier(CarrierRequest request);

    /**
     * 编辑物流商。
     *
     * @param id      物流商ID
     * @param request 物流商请求
     */
    void updateCarrier(Long id, CarrierRequest request);

    /**
     * 分页查询渠道。
     *
     * @param query 分页参数
     * @return 渠道分页结果
     */
    PageResult<LogisticsChannel> pageChannels(LogisticsPageQuery query);

    /**
     * 创建渠道。
     *
     * @param request 渠道请求
     * @return 渠道ID
     */
    Long createChannel(ChannelRequest request);

    /**
     * 停用渠道。
     *
     * @param id 渠道ID
     */
    void disableChannel(Long id);

    /**
     * 保存渠道费率。
     *
     * @param id      渠道ID
     * @param request 费率请求
     */
    void saveRates(Long id, RateRequest request);

    /**
     * 智能推荐物流渠道。
     *
     * @param request 推荐请求
     * @return 推荐结果
     */
    Map<String, Object> recommend(RecommendRequest request);

    /**
     * 预估运费。
     *
     * @param request 预估请求
     * @return 费用明细
     */
    Map<String, Object> estimate(FeeEstimateRequest request);

    /**
     * 分页查询运单。
     *
     * @param query 分页参数
     * @return 运单分页结果
     */
    PageResult<LogisticsWaybill> pageWaybills(LogisticsPageQuery query);

    /**
     * 查询运单详情。
     *
     * @param id 运单ID
     * @return 运单
     */
    LogisticsWaybill detailWaybill(Long id);

    /**
     * 创建运单并调用物流商 API 返回面单。
     *
     * @param request 运单请求
     * @return 运单ID
     */
    Long createWaybill(WaybillRequest request);

    /**
     * 批量创建运单。
     *
     * @param request 批量请求
     * @return 运单ID列表
     */
    List<Long> batchCreateWaybills(WaybillBatchRequest request);

    /**
     * 获取面单信息。
     *
     * @param id 运单ID
     * @return 面单信息
     */
    Map<String, Object> label(Long id);

    /**
     * 批量获取面单信息。
     *
     * @param ids 运单ID列表
     * @return 面单列表
     */
    List<Map<String, Object>> batchLabels(List<Long> ids);

    /**
     * 手动更新运单状态。
     *
     * @param id      运单ID
     * @param request 状态请求
     */
    void updateWaybillStatus(Long id, WaybillStatusRequest request);

    /**
     * 取消运单。
     *
     * @param id 运单ID
     */
    void cancelWaybill(Long id);

    /**
     * 查询运单轨迹。
     *
     * @param id 运单ID
     * @return 轨迹列表
     */
    List<LogisticsTrack> tracks(Long id);

    /**
     * 查询异常运单。
     *
     * @return 异常运单列表
     */
    List<LogisticsWaybill> exceptions();

    /**
     * 手动刷新轨迹。
     *
     * @param id 运单ID
     */
    void refreshTracks(Long id);

    /**
     * 处理轨迹 Webhook。
     *
     * @param carrierCode 物流商编码
     * @param request     Webhook 请求
     */
    void webhookTrack(String carrierCode, WebhookTrackRequest request);

    /**
     * 分页查询退货单。
     *
     * @param query 分页参数
     * @return 退货分页结果
     */
    PageResult<LogisticsReturn> pageReturns(PageQuery query);

    /**
     * 创建退货单。
     *
     * @param request 退货请求
     * @return 退货单ID
     */
    Long createReturn(ReturnRequest request);

    /**
     * 确认退货到仓。
     *
     * @param id 退货单ID
     */
    void arriveReturn(Long id);

    /**
     * 分页查询费用记录。
     *
     * @param query 分页参数
     * @return 费用分页结果
     */
    PageResult<LogisticsFeeRecord> pageFees(PageQuery query);

    /**
     * 渠道效率报表。
     *
     * @return 报表数据
     */
    Map<String, Object> channelEfficiency();

    /**
     * 异常报表。
     *
     * @return 报表数据
     */
    Map<String, Object> exceptionReport();

    /**
     * 运费统计报表。
     *
     * @return 报表数据
     */
    Map<String, Object> feeSummary();

    /**
     * 定时拉取轨迹。
     *
     * @return 处理数量
     */
    int pullTracks();

    /**
     * 按 XXL-JOB 分片拉取轨迹。
     *
     * @param shardIndex 当前分片序号
     * @param shardTotal 分片总数
     * @return 处理数量
     */
    int pullTracks(int shardIndex, int shardTotal);

    /**
     * 定时扫描异常运单。
     *
     * @return 异常数量
     */
    int scanExceptions();
}
