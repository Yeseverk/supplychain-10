package com.lyf.supplychain.logistics.client;

import com.lyf.supplychain.logistics.model.CarrierTrackEvent;
import com.lyf.supplychain.logistics.model.CarrierTrackPullRequest;
import com.lyf.supplychain.logistics.model.CarrierWaybillCreateRequest;
import com.lyf.supplychain.logistics.model.CarrierWaybillCreateResult;

import java.util.List;

/**
 * 物流商 API 适配器。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
public interface LogisticsCarrierApiClient {

    /**
     * 判断当前客户端是否支持该物流商。
     *
     * @param carrierCode 物流商编码
     * @return 是否支持
     */
    boolean supports(String carrierCode);

    /**
     * 向物流商创建运单。
     *
     * @param request 创建运单请求
     * @return 物流商返回结果
     */
    CarrierWaybillCreateResult createWaybill(CarrierWaybillCreateRequest request);

    /**
     * 从物流商拉取轨迹。
     *
     * @param request 轨迹拉取请求
     * @return 标准化后的轨迹列表
     */
    List<CarrierTrackEvent> pullTracks(CarrierTrackPullRequest request);
}
