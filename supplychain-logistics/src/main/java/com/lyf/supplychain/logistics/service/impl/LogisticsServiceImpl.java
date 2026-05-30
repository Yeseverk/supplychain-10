package com.lyf.supplychain.logistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyf.supplychain.common.api.PageQuery;
import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.common.context.TenantContext;
import com.lyf.supplychain.common.exception.BusinessException;
import com.lyf.supplychain.common.feign.order.OrderFeignClient;
import com.lyf.supplychain.common.feign.order.OrderLogisticsCallbackRequest;
import com.lyf.supplychain.common.redis.CommonRedisKeys;
import com.lyf.supplychain.common.redis.RedisDistributedLockTemplate;
import com.lyf.supplychain.logistics.client.LogisticsCarrierApiClient;
import com.lyf.supplychain.logistics.client.LogisticsCarrierApiClientRegistry;
import com.lyf.supplychain.logistics.config.LogisticsProperties;
import com.lyf.supplychain.logistics.constant.LogisticsConstants;
import com.lyf.supplychain.logistics.entity.LogisticsCarrier;
import com.lyf.supplychain.logistics.entity.LogisticsChannel;
import com.lyf.supplychain.logistics.entity.LogisticsFeeRecord;
import com.lyf.supplychain.logistics.entity.LogisticsRate;
import com.lyf.supplychain.logistics.entity.LogisticsReturn;
import com.lyf.supplychain.logistics.entity.LogisticsTrack;
import com.lyf.supplychain.logistics.entity.LogisticsWaybill;
import com.lyf.supplychain.logistics.mapper.LogisticsCarrierMapper;
import com.lyf.supplychain.logistics.mapper.LogisticsChannelMapper;
import com.lyf.supplychain.logistics.mapper.LogisticsFeeRecordMapper;
import com.lyf.supplychain.logistics.mapper.LogisticsRateMapper;
import com.lyf.supplychain.logistics.mapper.LogisticsReturnMapper;
import com.lyf.supplychain.logistics.mapper.LogisticsTrackMapper;
import com.lyf.supplychain.logistics.mapper.LogisticsWaybillMapper;
import com.lyf.supplychain.logistics.model.CarrierTrackEvent;
import com.lyf.supplychain.logistics.model.CarrierTrackPullRequest;
import com.lyf.supplychain.logistics.model.CarrierWaybillCreateRequest;
import com.lyf.supplychain.logistics.model.CarrierWaybillCreateResult;
import com.lyf.supplychain.logistics.request.CarrierRequest;
import com.lyf.supplychain.logistics.request.ChannelRequest;
import com.lyf.supplychain.logistics.request.FeeEstimateRequest;
import com.lyf.supplychain.logistics.request.LogisticsPageQuery;
import com.lyf.supplychain.logistics.request.RateItemRequest;
import com.lyf.supplychain.logistics.request.RateRequest;
import com.lyf.supplychain.logistics.request.RecommendRequest;
import com.lyf.supplychain.logistics.request.ReturnRequest;
import com.lyf.supplychain.logistics.request.WaybillBatchRequest;
import com.lyf.supplychain.logistics.request.WaybillRequest;
import com.lyf.supplychain.logistics.request.WaybillStatusRequest;
import com.lyf.supplychain.logistics.request.WebhookTrackRequest;
import com.lyf.supplychain.logistics.service.LogisticsNumberService;
import com.lyf.supplychain.logistics.service.LogisticsService;
import io.seata.spring.annotation.GlobalTransactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * 物流管理业务服务实现。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Service
public class LogisticsServiceImpl implements LogisticsService {

    private static final Logger log = LoggerFactory.getLogger(LogisticsServiceImpl.class);

    private final LogisticsCarrierMapper carrierMapper;
    private final LogisticsChannelMapper channelMapper;
    private final LogisticsRateMapper rateMapper;
    private final LogisticsWaybillMapper waybillMapper;
    private final LogisticsTrackMapper trackMapper;
    private final LogisticsFeeRecordMapper feeRecordMapper;
    private final LogisticsReturnMapper returnMapper;
    private final LogisticsNumberService numberService;
    private final OrderFeignClient orderFeignClient;
    private final RedisDistributedLockTemplate lockTemplate;
    private final LogisticsCarrierApiClientRegistry carrierApiClientRegistry;
    private final LogisticsProperties logisticsProperties;
    private final ThreadPoolTaskExecutor logisticsTrackExecutor;

    public LogisticsServiceImpl(LogisticsCarrierMapper carrierMapper,
                                LogisticsChannelMapper channelMapper,
                                LogisticsRateMapper rateMapper,
                                LogisticsWaybillMapper waybillMapper,
                                LogisticsTrackMapper trackMapper,
                                LogisticsFeeRecordMapper feeRecordMapper,
                                LogisticsReturnMapper returnMapper,
                                LogisticsNumberService numberService,
                                OrderFeignClient orderFeignClient,
                                RedisDistributedLockTemplate lockTemplate,
                                LogisticsCarrierApiClientRegistry carrierApiClientRegistry,
                                LogisticsProperties logisticsProperties,
                                @Qualifier("logisticsTrackExecutor") ThreadPoolTaskExecutor logisticsTrackExecutor) {
        this.carrierMapper = carrierMapper;
        this.channelMapper = channelMapper;
        this.rateMapper = rateMapper;
        this.waybillMapper = waybillMapper;
        this.trackMapper = trackMapper;
        this.feeRecordMapper = feeRecordMapper;
        this.returnMapper = returnMapper;
        this.numberService = numberService;
        this.orderFeignClient = orderFeignClient;
        this.lockTemplate = lockTemplate;
        this.carrierApiClientRegistry = carrierApiClientRegistry;
        this.logisticsProperties = logisticsProperties;
        this.logisticsTrackExecutor = logisticsTrackExecutor;
    }

    /**
     * 分页查询物流商。
     *
     * @param query 分页参数
     * @return 物流商分页结果
     */
    @Override
    public PageResult<LogisticsCarrier> pageCarriers(PageQuery query) {
        query.normalize();
        return PageResult.from(carrierMapper.selectPage(Page.of(query.getPageNum(), query.getPageSize()),
                new LambdaQueryWrapper<LogisticsCarrier>().orderByDesc(LogisticsCarrier::getCreateTime)));
    }

    /**
     * 创建物流商。
     *
     * @param request 物流商请求
     * @return 物流商ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCarrier(CarrierRequest request) {
        LogisticsCarrier carrier = new LogisticsCarrier();
        fillCarrier(carrier, request);
        carrier.setTenantId(TenantContext.getTenantId());
        carrierMapper.insert(carrier);
        return carrier.getId();
    }

    /**
     * 编辑物流商。
     *
     * @param id      物流商ID
     * @param request 物流商请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCarrier(Long id, CarrierRequest request) {
        LogisticsCarrier carrier = carrierMapper.selectById(id);
        if (carrier == null) {
            BusinessException.throwException(16001, "物流商不存在");
        }
        fillCarrier(carrier, request);
        carrierMapper.updateById(carrier);
    }

    /**
     * 分页查询渠道。
     *
     * @param query 分页参数
     * @return 渠道分页结果
     */
    @Override
    public PageResult<LogisticsChannel> pageChannels(LogisticsPageQuery query) {
        query.normalize();
        LambdaQueryWrapper<LogisticsChannel> wrapper = new LambdaQueryWrapper<LogisticsChannel>()
                .orderByAsc(LogisticsChannel::getSortOrder);
        if (query.getStatus() != null) {
            wrapper.eq(LogisticsChannel::getStatus, query.getStatus());
        }
        if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
            String keyword = query.getKeyword().trim();
            wrapper.and(item -> item.like(LogisticsChannel::getChannelCode, keyword)
                    .or().like(LogisticsChannel::getChannelName, keyword)
                    .or().like(LogisticsChannel::getCountryCodes, keyword)
                    .or().like(LogisticsChannel::getRemark, keyword));
        }
        return PageResult.from(channelMapper.selectPage(Page.of(query.getPageNum(), query.getPageSize()),
                wrapper));
    }

    /**
     * 创建渠道。
     *
     * @param request 渠道请求
     * @return 渠道ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createChannel(ChannelRequest request) {
        if (carrierMapper.selectById(request.getCarrierId()) == null) {
            BusinessException.throwException(16001, "物流商不存在");
        }
        LogisticsChannel channel = new LogisticsChannel();
        fillChannel(channel, request);
        channel.setTenantId(TenantContext.getTenantId());
        channel.setStatus(LogisticsConstants.STATUS_ENABLED);
        channelMapper.insert(channel);
        return channel.getId();
    }

    /**
     * 停用渠道。
     *
     * @param id 渠道ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disableChannel(Long id) {
        channelMapper.update(null, new LambdaUpdateWrapper<LogisticsChannel>()
                .eq(LogisticsChannel::getId, id)
                .set(LogisticsChannel::getStatus, LogisticsConstants.STATUS_DISABLED));
    }

    /**
     * 保存渠道费率。
     *
     * @param id      渠道ID
     * @param request 费率请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveRates(Long id, RateRequest request) {
        if (channelMapper.selectById(id) == null) {
            BusinessException.throwException(16002, "渠道不存在或已停用");
        }
        rateMapper.delete(new LambdaUpdateWrapper<LogisticsRate>().eq(LogisticsRate::getChannelId, id));
        for (RateItemRequest item : request.getRates()) {
            LogisticsRate rate = new LogisticsRate();
            rate.setTenantId(TenantContext.getTenantId());
            rate.setChannelId(id);
            rate.setCountryCode(item.getCountryCode());
            rate.setZone(item.getZone());
            rate.setCurrency(item.getCurrency());
            rate.setFirstWeightG(item.getFirstWeightG());
            rate.setFirstWeightPrice(item.getFirstWeightPrice());
            rate.setExtraWeightG(item.getExtraWeightG());
            rate.setExtraWeightPrice(item.getExtraWeightPrice());
            rate.setMinCharge(item.getMinCharge());
            rate.setFuelRate(item.getFuelRate());
            rate.setPeakRate(item.getPeakRate());
            rate.setRemoteAreaFee(item.getRemoteAreaFee());
            rate.setEffectiveDate(item.getEffectiveDate());
            rate.setExpireDate(item.getExpireDate());
            rateMapper.insert(rate);
        }
    }

    /**
     * 智能推荐物流渠道。
     *
     * @param request 推荐请求
     * @return 推荐结果
     */
    @Override
    public Map<String, Object> recommend(RecommendRequest request) {
        List<Map<String, Object>> recommendations = new ArrayList<>();
        for (LogisticsChannel channel : availableChannels(request)) {
            Map<String, Object> fee = estimate(toFeeRequest(channel.getId(), request));
            BigDecimal estimatedFee = (BigDecimal) fee.get("totalEstimatedFee");
            BigDecimal costScore = BigDecimal.valueOf(100).subtract(estimatedFee.min(BigDecimal.valueOf(100)));
            BigDecimal deliveryScore = BigDecimal.valueOf(100).subtract(channel.getMaxDays().multiply(BigDecimal.valueOf(3)));
            BigDecimal reliabilityScore = BigDecimal.valueOf(97);
            BigDecimal totalScore = costScore.multiply(BigDecimal.valueOf(0.4))
                    .add(deliveryScore.multiply(BigDecimal.valueOf(0.35)))
                    .add(reliabilityScore.multiply(BigDecimal.valueOf(0.25)));
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("channelId", channel.getId());
            row.put("channelCode", channel.getChannelCode());
            row.put("channelName", channel.getChannelName());
            row.put("minDays", channel.getMinDays());
            row.put("maxDays", channel.getMaxDays());
            row.put("estimatedFee", estimatedFee);
            row.put("currency", fee.get("currency"));
            row.put("totalScore", totalScore.setScale(2, RoundingMode.HALF_UP));
            row.put("tips", channel.getMaxDays().intValue() <= 7 ? "时效较快，适合紧急订单" : "价格更优，适合普通订单");
            recommendations.add(row);
        }
        recommendations.sort(Comparator.comparing(item -> (BigDecimal) item.get("totalScore"), Comparator.reverseOrder()));
        for (int i = 0; i < recommendations.size(); i++) {
            recommendations.get(i).put("rank", i + 1);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("chargeWeightG", request.getActualWeightG());
        result.put("recommendations", recommendations);
        return result;
    }

    /**
     * 预估运费。
     *
     * @param request 预估请求
     * @return 费用明细
     */
    @Override
    public Map<String, Object> estimate(FeeEstimateRequest request) {
        LogisticsChannel channel = validChannel(request.getChannelId());
        BigDecimal volumeWeight = calcVolumeWeight(request.getLengthMm(), request.getWidthMm(), request.getHeightMm(), channel.getVolumeFactor());
        BigDecimal chargeWeight = request.getActualWeightG().max(volumeWeight);
        LogisticsRate rate = findRate(request.getChannelId(), request.getCountryCode());
        BigDecimal baseFee = calcBaseFee(chargeWeight, rate);
        BigDecimal fuel = baseFee.multiply(rate.getFuelRate());
        BigDecimal peak = baseFee.multiply(rate.getPeakRate());
        BigDecimal insurance = request.getDeclaredValue().compareTo(BigDecimal.valueOf(100)) > 0
                ? request.getDeclaredValue().multiply(BigDecimal.valueOf(0.01)) : BigDecimal.ZERO;
        BigDecimal total = baseFee.add(fuel).add(peak).add(rate.getRemoteAreaFee()).add(insurance).max(rate.getMinCharge());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("chargeWeightG", chargeWeight.setScale(2, RoundingMode.HALF_UP));
        result.put("volumeWeightG", volumeWeight.setScale(2, RoundingMode.HALF_UP));
        result.put("baseFee", baseFee.setScale(2, RoundingMode.HALF_UP));
        result.put("fuelSurcharge", fuel.setScale(2, RoundingMode.HALF_UP));
        result.put("peakSurcharge", peak.setScale(2, RoundingMode.HALF_UP));
        result.put("remoteFee", rate.getRemoteAreaFee());
        result.put("insuranceFee", insurance.setScale(2, RoundingMode.HALF_UP));
        result.put("totalEstimatedFee", total.setScale(2, RoundingMode.HALF_UP));
        result.put("currency", rate.getCurrency());
        result.put("rateId", rate.getId());
        return result;
    }

    /**
     * 分页查询运单。
     *
     * @param query 分页参数
     * @return 运单分页结果
     */
    @Override
    public PageResult<LogisticsWaybill> pageWaybills(LogisticsPageQuery query) {
        query.normalize();
        LambdaQueryWrapper<LogisticsWaybill> wrapper = new LambdaQueryWrapper<LogisticsWaybill>()
                .orderByDesc(LogisticsWaybill::getCreateTime);
        if (query.getStatus() != null) {
            wrapper.eq(LogisticsWaybill::getStatus, query.getStatus());
        }
        if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
            String keyword = query.getKeyword().trim();
            wrapper.and(item -> item.like(LogisticsWaybill::getWaybillNo, keyword)
                    .or().like(LogisticsWaybill::getTrackingNo, keyword)
                    .or().like(LogisticsWaybill::getOrderNo, keyword)
                    .or().like(LogisticsWaybill::getCountryCode, keyword)
                    .or().like(LogisticsWaybill::getCity, keyword)
                    .or().like(LogisticsWaybill::getExceptionDesc, keyword));
        }
        return PageResult.from(waybillMapper.selectPage(Page.of(query.getPageNum(), query.getPageSize()),
                wrapper));
    }

    /**
     * 查询运单详情。
     *
     * @param id 运单ID
     * @return 运单
     */
    @Override
    public LogisticsWaybill detailWaybill(Long id) {
        LogisticsWaybill waybill = waybillMapper.selectById(id);
        if (waybill == null) {
            BusinessException.throwException(16009, "运单号不存在");
        }
        return waybill;
    }

    /**
     * 创建运单并调用物流商 API 返回面单。
     *
     * @param request 运单请求
     * @return 运单ID
     */
    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public Long createWaybill(WaybillRequest request) {
        String lockKey = CommonRedisKeys.lock("tms:waybill:create",
                TenantContext.getTenantId() + ":" + request.getOrderId());
        return lockTemplate.execute(lockKey, logisticsProperties.getWaybillLockTtl(), () -> doCreateWaybill(request));
    }

    private Long doCreateWaybill(WaybillRequest request) {
        LogisticsWaybill existing = findExistingWaybill(request.getOrderId());
        if (existing != null) {
            return existing.getId();
        }
        Long channelId = request.getChannelId();
        if (channelId == null) {
            List<LogisticsChannel> channels = availableChannels(toRecommendRequest(request));
            if (channels.isEmpty()) {
                BusinessException.throwException(16006, "无可用渠道，请检查包裹信息");
            }
            channelId = channels.get(0).getId();
        }
        LogisticsChannel channel = validChannel(channelId);
        LogisticsCarrier carrier = validCarrier(channel.getCarrierId());
        Map<String, Object> fee = estimate(toFeeRequest(channelId, request));
        LogisticsWaybill waybill = buildWaybill(request, channel, fee);
        fillCarrierWaybillResult(waybill, carrier, channel);
        waybillMapper.insert(waybill);
        saveFeeRecord(waybill, fee);
        callbackOms(waybill);
        return waybill.getId();
    }

    /**
     * 批量创建运单。
     *
     * @param request 批量请求
     * @return 运单ID列表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Long> batchCreateWaybills(WaybillBatchRequest request) {
        return request.getWaybills().stream().map(this::createWaybill).toList();
    }

    /**
     * 获取面单信息。
     *
     * @param id 运单ID
     * @return 面单信息
     */
    @Override
    public Map<String, Object> label(Long id) {
        LogisticsWaybill waybill = detailWaybill(id);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("waybillNo", waybill.getWaybillNo());
        result.put("trackingNo", waybill.getTrackingNo());
        result.put("labelUrl", waybill.getLabelUrl());
        result.put("labelFormat", waybill.getLabelFormat());
        return result;
    }

    /**
     * 批量获取面单信息。
     *
     * @param ids 运单ID列表
     * @return 面单列表
     */
    @Override
    public List<Map<String, Object>> batchLabels(List<Long> ids) {
        return ids.stream().map(this::label).toList();
    }

    /**
     * 手动更新运单状态。
     *
     * @param id      运单ID
     * @param request 状态请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWaybillStatus(Long id, WaybillStatusRequest request) {
        LogisticsWaybill waybill = detailWaybill(id);
        if (LogisticsConstants.WAYBILL_CANCELED == waybill.getStatus()) {
            BusinessException.throwException(16010, "运单已取消，不能操作");
        }
        waybill.setStatus(request.getStatus());
        waybill.setExceptionDesc(request.getRemark());
        waybillMapper.updateById(waybill);
        callbackOms(waybill);
    }

    /**
     * 取消运单。
     *
     * @param id 运单ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelWaybill(Long id) {
        LogisticsWaybill waybill = detailWaybill(id);
        if (waybill.getStatus() >= LogisticsConstants.WAYBILL_PICKED) {
            BusinessException.throwException(16011, "运单已揽收，不能取消");
        }
        waybill.setStatus(LogisticsConstants.WAYBILL_CANCELED);
        waybillMapper.updateById(waybill);
    }

    /**
     * 查询运单轨迹。
     *
     * @param id 运单ID
     * @return 轨迹列表
     */
    @Override
    public List<LogisticsTrack> tracks(Long id) {
        return trackMapper.selectList(new LambdaQueryWrapper<LogisticsTrack>()
                .eq(LogisticsTrack::getWaybillId, id)
                .orderByDesc(LogisticsTrack::getTrackTime));
    }

    /**
     * 查询异常运单。
     *
     * @return 异常运单列表
     */
    @Override
    public List<LogisticsWaybill> exceptions() {
        return waybillMapper.selectList(new LambdaQueryWrapper<LogisticsWaybill>()
                .eq(LogisticsWaybill::getStatus, LogisticsConstants.WAYBILL_EXCEPTION)
                .orderByDesc(LogisticsWaybill::getUpdateTime));
    }

    /**
     * 手动刷新轨迹。
     *
     * @param id 运单ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refreshTracks(Long id) {
        LogisticsWaybill waybill = detailWaybill(id);
        refreshTracks(waybill);
    }

    /**
     * 处理轨迹 Webhook。
     *
     * @param carrierCode 物流商编码
     * @param request     Webhook 请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void webhookTrack(String carrierCode, WebhookTrackRequest request) {
        if (request.getSignature() == null || request.getSignature().isBlank()) {
            BusinessException.throwException(16007, "Webhook 签名验证失败");
        }
        LogisticsWaybill waybill = waybillMapper.selectOne(new LambdaQueryWrapper<LogisticsWaybill>()
                .eq(LogisticsWaybill::getTrackingNo, request.getTrackingNo())
                .last("limit 1"));
        if (waybill == null) {
            BusinessException.throwException(16009, "运单号不存在");
        }
        LogisticsTrack track = normalizeTrack(waybill, request);
        trackMapper.insert(track);
        if (track.getIsException() == 1) {
            waybill.setStatus(LogisticsConstants.WAYBILL_EXCEPTION);
            waybill.setExceptionDesc(track.getExceptionDesc());
            waybillMapper.updateById(waybill);
        }
        log.info("收到物流轨迹Webhook，carrier={}, trackingNo={}, rawStatus={}", carrierCode, request.getTrackingNo(), request.getRawStatus());
    }

    /**
     * 分页查询退货单。
     *
     * @param query 分页参数
     * @return 退货分页结果
     */
    @Override
    public PageResult<LogisticsReturn> pageReturns(PageQuery query) {
        query.normalize();
        return PageResult.from(returnMapper.selectPage(Page.of(query.getPageNum(), query.getPageSize()),
                new LambdaQueryWrapper<LogisticsReturn>().orderByDesc(LogisticsReturn::getCreateTime)));
    }

    /**
     * 创建退货单。
     *
     * @param request 退货请求
     * @return 退货单ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createReturn(ReturnRequest request) {
        LogisticsReturn returns = new LogisticsReturn();
        returns.setTenantId(TenantContext.getTenantId());
        returns.setReturnNo(numberService.nextNo("RWB"));
        returns.setOriginalWaybillId(request.getOriginalWaybillId());
        returns.setOrderId(request.getOrderId());
        returns.setRefundId(request.getRefundId());
        returns.setReturnType(request.getReturnType());
        returns.setCarrierId(request.getCarrierId());
        returns.setReturnTrackingNo(request.getReturnTrackingNo());
        returns.setFromCountry(request.getFromCountry());
        returns.setToWarehouseId(request.getToWarehouseId());
        returns.setExpectedArriveDate(request.getExpectedArriveDate());
        returns.setStatus(0);
        returns.setLabelUrl("https://labels.example.com/return/" + returns.getReturnNo() + ".pdf");
        returns.setRemark(request.getRemark());
        returnMapper.insert(returns);
        return returns.getId();
    }

    /**
     * 确认退货到仓。
     *
     * @param id 退货单ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void arriveReturn(Long id) {
        returnMapper.update(null, new LambdaUpdateWrapper<LogisticsReturn>()
                .eq(LogisticsReturn::getId, id)
                .set(LogisticsReturn::getStatus, LogisticsConstants.RETURN_ARRIVED)
                .set(LogisticsReturn::getActualArriveDate, LocalDate.now()));
    }

    /**
     * 分页查询费用记录。
     *
     * @param query 分页参数
     * @return 费用分页结果
     */
    @Override
    public PageResult<LogisticsFeeRecord> pageFees(PageQuery query) {
        query.normalize();
        return PageResult.from(feeRecordMapper.selectPage(Page.of(query.getPageNum(), query.getPageSize()),
                new LambdaQueryWrapper<LogisticsFeeRecord>().orderByDesc(LogisticsFeeRecord::getCreateTime)));
    }

    /**
     * 渠道效率报表。
     *
     * @return 报表数据
     */
    @Override
    public Map<String, Object> channelEfficiency() {
        return Map.of("waybillCount", waybillMapper.selectCount(new LambdaQueryWrapper<>()),
                "signedCount", waybillMapper.selectCount(new LambdaQueryWrapper<LogisticsWaybill>().eq(LogisticsWaybill::getStatus, LogisticsConstants.WAYBILL_SIGNED)));
    }

    /**
     * 异常报表。
     *
     * @return 报表数据
     */
    @Override
    public Map<String, Object> exceptionReport() {
        return Map.of("exceptionCount", exceptions().size(), "items", exceptions());
    }

    /**
     * 运费统计报表。
     *
     * @return 报表数据
     */
    @Override
    public Map<String, Object> feeSummary() {
        return Map.of("feeRecordCount", feeRecordMapper.selectCount(new LambdaQueryWrapper<>()),
                "currency", "CNY");
    }

    /**
     * 定时拉取轨迹。
     *
     * @return 处理数量
     */
    @Override
    public int pullTracks() {
        return pullTracks(0, 1);
    }

    /**
     * 按 XXL-JOB 分片定时拉取轨迹。
     *
     * @param shardIndex 当前分片序号
     * @param shardTotal 分片总数
     * @return 处理数量
     */
    @Override
    public int pullTracks(int shardIndex, int shardTotal) {
        int safeShardTotal = Math.max(shardTotal, 1);
        int safeShardIndex = Math.floorMod(Math.max(shardIndex, 0), safeShardTotal);
        List<LogisticsWaybill> waybills = waybillMapper.selectList(new LambdaQueryWrapper<LogisticsWaybill>()
                .in(LogisticsWaybill::getStatus, List.of(0, 1, 2, 3, 4, 5, 6))
                .last("limit " + logisticsProperties.getTrackPull().getBatchSize()))
                .stream()
                .filter(item -> Math.floorMod(item.getId().hashCode(), safeShardTotal) == safeShardIndex)
                .toList();
        Map<Long, List<LogisticsWaybill>> carrierGroups = groupByCarrier(waybills);
        int count = 0;
        int carrierConcurrency = Math.max(logisticsProperties.getTrackPull().getCarrierConcurrency(), 1);
        for (Map.Entry<Long, List<LogisticsWaybill>> entry : carrierGroups.entrySet()) {
            List<Future<Integer>> futures = new ArrayList<>();
            for (LogisticsWaybill waybill : entry.getValue()) {
                futures.add(logisticsTrackExecutor.submit(() -> refreshTracksSafely(waybill)));
                if (futures.size() >= carrierConcurrency) {
                    count += waitTrackTasks(futures);
                    futures.clear();
                }
            }
            count += waitTrackTasks(futures);
        }
        return count;
    }

    private int waitTrackTasks(List<Future<Integer>> futures) {
        int count = 0;
        for (Future<Integer> future : futures) {
            try {
                count += future.get(logisticsProperties.getTrackPull().getTaskTimeout().toMillis(), TimeUnit.MILLISECONDS);
            } catch (Exception ex) {
                log.warn("物流轨迹并行拉取任务执行失败", ex);
                future.cancel(true);
            }
        }
        return count;
    }

    /**
     * 定时扫描异常运单。
     *
     * @return 异常数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int scanExceptions() {
        List<LogisticsWaybill> stuck = waybillMapper.selectList(new LambdaQueryWrapper<LogisticsWaybill>()
                .in(LogisticsWaybill::getStatus, List.of(1, 2, 3, 4, 5, 6))
                .lt(LogisticsWaybill::getCreateTime, LocalDateTime.now().minusDays(20)));
        stuck.forEach(item -> {
            item.setStatus(LogisticsConstants.WAYBILL_EXCEPTION);
            item.setExceptionDesc("物流停滞超过20天，请联系物流商跟进");
            waybillMapper.updateById(item);
            log.warn("物流异常预警，waybillNo={}, reason={}", item.getWaybillNo(), item.getExceptionDesc());
        });
        return stuck.size();
    }

    private void fillCarrier(LogisticsCarrier carrier, CarrierRequest request) {
        carrier.setCarrierCode(request.getCarrierCode());
        carrier.setCarrierName(request.getCarrierName());
        carrier.setCarrierNameEn(request.getCarrierNameEn());
        carrier.setCarrierType(request.getCarrierType());
        carrier.setLogoUrl(request.getLogoUrl());
        carrier.setApiBaseUrl(request.getApiBaseUrl());
        carrier.setApiKey(request.getApiKey());
        carrier.setApiSecret(request.getApiSecret());
        carrier.setApiAccount(request.getApiAccount());
        carrier.setApiVersion(request.getApiVersion());
        carrier.setTrackApiUrl(request.getTrackApiUrl());
        carrier.setSupportLabel(request.getSupportLabel());
        carrier.setSupportTrack(request.getSupportTrack());
        carrier.setStatus(request.getStatus());
        carrier.setRemark(request.getRemark());
    }

    private void fillChannel(LogisticsChannel channel, ChannelRequest request) {
        channel.setCarrierId(request.getCarrierId());
        channel.setChannelCode(request.getChannelCode());
        channel.setChannelName(request.getChannelName());
        channel.setChannelType(request.getChannelType());
        channel.setCountryCodes(request.getCountryCodes());
        channel.setMinWeightG(request.getMinWeightG());
        channel.setMaxWeightG(request.getMaxWeightG());
        channel.setMaxLengthMm(request.getMaxLengthMm());
        channel.setMaxGirthMm(request.getMaxGirthMm());
        channel.setAllowBattery(request.getAllowBattery());
        channel.setAllowLiquid(request.getAllowLiquid());
        channel.setAllowPowder(request.getAllowPowder());
        channel.setAllowFood(request.getAllowFood());
        channel.setMinDays(request.getMinDays());
        channel.setMaxDays(request.getMaxDays());
        channel.setVolumeFactor(request.getVolumeFactor());
        channel.setDeclaredValueLimit(request.getDeclaredValueLimit());
        channel.setSortOrder(request.getSortOrder());
        channel.setRemark(request.getRemark());
    }

    private LogisticsChannel validChannel(Long channelId) {
        LogisticsChannel channel = channelMapper.selectById(channelId);
        if (channel == null || channel.getStatus() == null || channel.getStatus() != LogisticsConstants.STATUS_ENABLED) {
            BusinessException.throwException(16002, "渠道不存在或已停用");
        }
        return channel;
    }

    private LogisticsCarrier validCarrier(Long carrierId) {
        LogisticsCarrier carrier = carrierMapper.selectById(carrierId);
        if (carrier == null || carrier.getStatus() == null || carrier.getStatus() != LogisticsConstants.STATUS_ENABLED) {
            BusinessException.throwException(16001, "物流商不存在或已停用");
        }
        return carrier;
    }

    private LogisticsWaybill findExistingWaybill(Long orderId) {
        return waybillMapper.selectOne(new LambdaQueryWrapper<LogisticsWaybill>()
                .eq(LogisticsWaybill::getTenantId, TenantContext.getTenantId())
                .eq(LogisticsWaybill::getOrderId, orderId)
                .last("limit 1"));
    }

    private LogisticsRate findRate(Long channelId, String countryCode) {
        LogisticsRate rate = rateMapper.selectOne(new LambdaQueryWrapper<LogisticsRate>()
                .eq(LogisticsRate::getChannelId, channelId)
                .and(wrapper -> wrapper.eq(LogisticsRate::getCountryCode, countryCode).or().eq(LogisticsRate::getCountryCode, "ALL"))
                .le(LogisticsRate::getEffectiveDate, LocalDate.now())
                .and(wrapper -> wrapper.isNull(LogisticsRate::getExpireDate).or().ge(LogisticsRate::getExpireDate, LocalDate.now()))
                .last("limit 1"));
        if (rate == null) {
            BusinessException.throwException(16012, "费率配置不存在，无法计算运费");
        }
        return rate;
    }

    private boolean hasEffectiveRate(Long channelId, String countryCode) {
        return rateMapper.selectCount(new LambdaQueryWrapper<LogisticsRate>()
                .eq(LogisticsRate::getChannelId, channelId)
                .and(wrapper -> wrapper.eq(LogisticsRate::getCountryCode, countryCode).or().eq(LogisticsRate::getCountryCode, "ALL"))
                .le(LogisticsRate::getEffectiveDate, LocalDate.now())
                .and(wrapper -> wrapper.isNull(LogisticsRate::getExpireDate).or().ge(LogisticsRate::getExpireDate, LocalDate.now()))) > 0;
    }

    private BigDecimal calcVolumeWeight(Integer lengthMm, Integer widthMm, Integer heightMm, Integer volumeFactor) {
        if (lengthMm == null || widthMm == null || heightMm == null) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf((long) lengthMm * widthMm * heightMm)
                .divide(BigDecimal.valueOf((long) volumeFactor * 1000), 4, RoundingMode.HALF_UP);
    }

    private BigDecimal calcBaseFee(BigDecimal chargeWeight, LogisticsRate rate) {
        if (chargeWeight.compareTo(rate.getFirstWeightG()) <= 0) {
            return rate.getFirstWeightPrice();
        }
        BigDecimal extraWeight = chargeWeight.subtract(rate.getFirstWeightG());
        BigDecimal units = extraWeight.divide(rate.getExtraWeightG(), 0, RoundingMode.CEILING);
        return rate.getFirstWeightPrice().add(units.multiply(rate.getExtraWeightPrice()));
    }

    private List<LogisticsChannel> availableChannels(RecommendRequest request) {
        List<LogisticsChannel> channels = channelMapper.selectList(new LambdaQueryWrapper<LogisticsChannel>()
                .eq(LogisticsChannel::getStatus, LogisticsConstants.STATUS_ENABLED)
                .orderByAsc(LogisticsChannel::getSortOrder));
        return channels.stream()
                .filter(channel -> channel.getCountryCodes().contains("\"ALL\"") || channel.getCountryCodes().contains("\"" + request.getCountryCode() + "\""))
                .filter(channel -> request.getActualWeightG().compareTo(channel.getMinWeightG()) >= 0 && request.getActualWeightG().compareTo(channel.getMaxWeightG()) <= 0)
                .filter(channel -> !Boolean.TRUE.equals(request.getHasBattery()) || channel.getAllowBattery() == 1)
                .filter(channel -> !Boolean.TRUE.equals(request.getHasLiquid()) || channel.getAllowLiquid() == 1)
                .filter(channel -> !Boolean.TRUE.equals(request.getHasPowder()) || channel.getAllowPowder() == 1)
                .filter(channel -> channel.getDeclaredValueLimit() == null || request.getDeclaredValue().compareTo(channel.getDeclaredValueLimit()) <= 0)
                .filter(channel -> request.getMaxDaysRequired() == null || channel.getMaxDays().compareTo(BigDecimal.valueOf(request.getMaxDaysRequired())) <= 0)
                .filter(channel -> hasEffectiveRate(channel.getId(), request.getCountryCode()))
                .toList();
    }

    private FeeEstimateRequest toFeeRequest(Long channelId, RecommendRequest request) {
        FeeEstimateRequest fee = new FeeEstimateRequest();
        fee.setChannelId(channelId);
        fee.setCountryCode(request.getCountryCode());
        fee.setActualWeightG(request.getActualWeightG());
        fee.setLengthMm(request.getLengthMm());
        fee.setWidthMm(request.getWidthMm());
        fee.setHeightMm(request.getHeightMm());
        fee.setDeclaredValue(request.getDeclaredValue());
        return fee;
    }

    private FeeEstimateRequest toFeeRequest(Long channelId, WaybillRequest request) {
        FeeEstimateRequest fee = new FeeEstimateRequest();
        fee.setChannelId(channelId);
        fee.setCountryCode(request.getCountryCode());
        fee.setActualWeightG(request.getActualWeightG());
        fee.setLengthMm(request.getLengthMm());
        fee.setWidthMm(request.getWidthMm());
        fee.setHeightMm(request.getHeightMm());
        fee.setDeclaredValue(request.getDeclaredValue());
        return fee;
    }

    private RecommendRequest toRecommendRequest(WaybillRequest request) {
        RecommendRequest recommend = new RecommendRequest();
        recommend.setOrderId(request.getOrderId());
        recommend.setCountryCode(request.getCountryCode());
        recommend.setActualWeightG(request.getActualWeightG());
        recommend.setLengthMm(request.getLengthMm());
        recommend.setWidthMm(request.getWidthMm());
        recommend.setHeightMm(request.getHeightMm());
        recommend.setHasBattery(request.getHasBattery());
        recommend.setHasLiquid(request.getHasLiquid());
        recommend.setHasPowder(request.getHasPowder());
        recommend.setDeclaredValue(request.getDeclaredValue());
        recommend.setDeclaredCurrency(request.getDeclaredCurrency());
        return recommend;
    }

    private LogisticsWaybill buildWaybill(WaybillRequest request, LogisticsChannel channel, Map<String, Object> fee) {
        LogisticsWaybill waybill = new LogisticsWaybill();
        waybill.setTenantId(TenantContext.getTenantId());
        waybill.setWaybillNo(numberService.nextNo("WB"));
        waybill.setCarrierId(channel.getCarrierId());
        waybill.setChannelId(channel.getId());
        waybill.setOrderId(request.getOrderId());
        waybill.setOrderNo(request.getOrderNo());
        waybill.setWarehouseId(request.getWarehouseId());
        waybill.setReceiverName(request.getReceiverName());
        waybill.setReceiverPhone(request.getReceiverPhone());
        waybill.setCountryCode(request.getCountryCode());
        waybill.setState(request.getState());
        waybill.setCity(request.getCity());
        waybill.setAddressLine1(request.getAddressLine1());
        waybill.setAddressLine2(request.getAddressLine2());
        waybill.setZipCode(request.getZipCode());
        waybill.setActualWeightG(request.getActualWeightG());
        waybill.setVolumeWeightG((BigDecimal) fee.get("volumeWeightG"));
        waybill.setChargeWeightG((BigDecimal) fee.get("chargeWeightG"));
        waybill.setLengthMm(request.getLengthMm());
        waybill.setWidthMm(request.getWidthMm());
        waybill.setHeightMm(request.getHeightMm());
        waybill.setPackageCount(1);
        waybill.setDeclaredValue(request.getDeclaredValue());
        waybill.setDeclaredCurrency(request.getDeclaredCurrency());
        waybill.setDeclaredNameEn(request.getDeclaredNameEn());
        waybill.setHsCode(request.getHsCode());
        waybill.setIsGift(request.getIsGift());
        waybill.setEstimatedFee((BigDecimal) fee.get("totalEstimatedFee"));
        waybill.setFeeCurrency((String) fee.get("currency"));
        waybill.setStatus(LogisticsConstants.WAYBILL_WAIT_PICKUP);
        waybill.setCreateWaybillTime(LocalDateTime.now());
        return waybill;
    }

    private void fillCarrierWaybillResult(LogisticsWaybill waybill, LogisticsCarrier carrier, LogisticsChannel channel) {
        CarrierWaybillCreateRequest request = new CarrierWaybillCreateRequest();
        request.setCarrier(carrier);
        request.setChannel(channel);
        request.setWaybill(waybill);
        LogisticsCarrierApiClient client = carrierApiClientRegistry.getClient(carrier.getCarrierCode());
        CarrierWaybillCreateResult result = client.createWaybill(request);
        if (result.getTrackingNo() == null || result.getTrackingNo().isBlank()) {
            BusinessException.throwException(16014, "物流商未返回运单号");
        }
        waybill.setTrackingNo(result.getTrackingNo());
        waybill.setLabelUrl(result.getLabelUrl());
        waybill.setLabelFormat(result.getLabelFormat());
    }

    private Map<Long, List<LogisticsWaybill>> groupByCarrier(List<LogisticsWaybill> waybills) {
        Map<Long, List<LogisticsWaybill>> groups = new HashMap<>();
        for (LogisticsWaybill waybill : waybills) {
            groups.computeIfAbsent(waybill.getCarrierId(), key -> new ArrayList<>()).add(waybill);
        }
        return groups;
    }

    private Integer refreshTracksSafely(LogisticsWaybill waybill) {
        Long oldTenantId = TenantContext.getTenantId();
        try {
            TenantContext.set(waybill.getTenantId(), null);
            refreshTracks(waybill);
            return 1;
        } catch (Exception ex) {
            log.warn("物流轨迹拉取失败，waybillNo={}, trackingNo={}", waybill.getWaybillNo(), waybill.getTrackingNo(), ex);
            return 0;
        } finally {
            if (oldTenantId == null) {
                TenantContext.clear();
            } else {
                TenantContext.set(oldTenantId, null);
            }
        }
    }

    private void refreshTracks(LogisticsWaybill waybill) {
        LogisticsCarrier carrier = validCarrier(waybill.getCarrierId());
        CarrierTrackPullRequest request = new CarrierTrackPullRequest();
        request.setCarrier(carrier);
        request.setWaybill(waybill);
        LogisticsCarrierApiClient client = carrierApiClientRegistry.getClient(carrier.getCarrierCode());
        List<CarrierTrackEvent> events = client.pullTracks(request);
        for (CarrierTrackEvent event : events) {
            LogisticsTrack track = toTrack(waybill, event);
            trackMapper.insert(track);
            advanceWaybillStatus(waybill, track);
        }
        callbackOms(waybill);
    }

    private LogisticsTrack toTrack(LogisticsWaybill waybill, CarrierTrackEvent event) {
        LogisticsTrack track = new LogisticsTrack();
        track.setTenantId(waybill.getTenantId());
        track.setWaybillId(waybill.getId());
        track.setTrackingNo(waybill.getTrackingNo());
        track.setTrackCode(event.getTrackCode());
        track.setTrackStage(event.getTrackStage());
        track.setRawStatus(event.getRawStatus());
        track.setStatusDesc(event.getStatusDesc());
        track.setLocation(event.getLocation());
        track.setLocationCountry(event.getLocationCountry());
        track.setTrackTime(event.getTrackTime() == null ? LocalDateTime.now() : event.getTrackTime());
        track.setFetchTime(LocalDateTime.now());
        track.setIsException(Boolean.TRUE.equals(event.getException()) ? 1 : 0);
        track.setExceptionType(event.getExceptionType());
        track.setExceptionDesc(event.getExceptionDesc());
        return track;
    }

    private void advanceWaybillStatus(LogisticsWaybill waybill, LogisticsTrack track) {
        Integer stage = track.getTrackStage();
        if (stage == null) {
            return;
        }
        if (track.getIsException() == 1) {
            waybill.setStatus(LogisticsConstants.WAYBILL_EXCEPTION);
            waybill.setExceptionDesc(track.getExceptionDesc());
        } else if (stage > waybill.getStatus() && stage < LogisticsConstants.WAYBILL_EXCEPTION) {
            waybill.setStatus(stage);
            if (stage == LogisticsConstants.WAYBILL_PICKED) {
                waybill.setPickupTime(track.getTrackTime());
            }
            if (stage == LogisticsConstants.WAYBILL_SIGNED) {
                waybill.setSignedTime(track.getTrackTime());
            }
        }
        waybillMapper.updateById(waybill);
    }

    private void saveFeeRecord(LogisticsWaybill waybill, Map<String, Object> fee) {
        LogisticsFeeRecord record = new LogisticsFeeRecord();
        record.setTenantId(waybill.getTenantId());
        record.setWaybillId(waybill.getId());
        record.setWaybillNo(waybill.getWaybillNo());
        record.setBaseFee((BigDecimal) fee.get("baseFee"));
        record.setFuelSurcharge((BigDecimal) fee.get("fuelSurcharge"));
        record.setPeakSurcharge((BigDecimal) fee.get("peakSurcharge"));
        record.setRemoteFee((BigDecimal) fee.get("remoteFee"));
        record.setInsuranceFee((BigDecimal) fee.get("insuranceFee"));
        record.setOversizeFee(BigDecimal.ZERO);
        record.setOtherFee(BigDecimal.ZERO);
        record.setEstimatedTotal((BigDecimal) fee.get("totalEstimatedFee"));
        record.setCurrency((String) fee.get("currency"));
        record.setBillingWeightG(waybill.getChargeWeightG());
        record.setRateId((Long) fee.get("rateId"));
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        feeRecordMapper.insert(record);
    }

    private LogisticsTrack normalizeTrack(LogisticsWaybill waybill, WebhookTrackRequest request) {
        LogisticsTrack track = new LogisticsTrack();
        track.setTenantId(TenantContext.getTenantId());
        track.setWaybillId(waybill.getId());
        track.setTrackingNo(waybill.getTrackingNo());
        track.setRawStatus(request.getRawStatus());
        track.setLocation(request.getLocation());
        track.setLocationCountry(request.getLocationCountry());
        track.setTrackTime(request.getTrackTime() == null ? LocalDateTime.now() : request.getTrackTime());
        track.setFetchTime(LocalDateTime.now());
        String raw = request.getRawStatus().toLowerCase();
        if (raw.contains("delivered") || raw.contains("signed")) {
            track.setTrackCode("SIGNED");
            track.setTrackStage(LogisticsConstants.WAYBILL_SIGNED);
            track.setStatusDesc("包裹已签收");
            track.setIsException(0);
        } else if (raw.contains("exception") || raw.contains("failed") || raw.contains("customs hold")) {
            track.setTrackCode("EXCEPTION");
            track.setTrackStage(LogisticsConstants.WAYBILL_EXCEPTION);
            track.setStatusDesc("物流异常");
            track.setIsException(1);
            track.setExceptionType(6);
            track.setExceptionDesc("物流商返回异常状态：" + request.getRawStatus());
        } else {
            track.setTrackCode("IN_TRANSIT");
            track.setTrackStage(LogisticsConstants.WAYBILL_IN_TRANSIT);
            track.setStatusDesc("运输中");
            track.setIsException(0);
        }
        return track;
    }

    private void callbackOms(LogisticsWaybill waybill) {
        OrderLogisticsCallbackRequest request = new OrderLogisticsCallbackRequest();
        request.setOrderNo(waybill.getOrderNo());
        request.setWaybillNo(waybill.getWaybillNo());
        request.setTrackingNo(waybill.getTrackingNo());
        request.setLogisticsStatus(waybill.getStatus());
        orderFeignClient.logisticsCallback(request);
    }
}
