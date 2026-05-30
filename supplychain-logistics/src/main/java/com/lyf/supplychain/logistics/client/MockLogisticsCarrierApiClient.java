package com.lyf.supplychain.logistics.client;

import com.lyf.supplychain.logistics.constant.LogisticsConstants;
import com.lyf.supplychain.logistics.model.CarrierTrackEvent;
import com.lyf.supplychain.logistics.model.CarrierTrackPullRequest;
import com.lyf.supplychain.logistics.model.CarrierWaybillCreateRequest;
import com.lyf.supplychain.logistics.model.CarrierWaybillCreateResult;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 默认物流商 API 模拟适配器。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Component
public class MockLogisticsCarrierApiClient implements LogisticsCarrierApiClient {

    /**
     * 默认适配所有暂未接入真实 SDK 的物流商。
     *
     * @param carrierCode 物流商编码
     * @return 是否支持
     */
    @Override
    public boolean supports(String carrierCode) {
        return true;
    }

    /**
     * 模拟物流商创建运单并返回面单。
     *
     * @param request 创建运单请求
     * @return 物流商返回结果
     */
    @Override
    public CarrierWaybillCreateResult createWaybill(CarrierWaybillCreateRequest request) {
        String trackingNo = "TMS" + request.getWaybillNo();
        CarrierWaybillCreateResult result = new CarrierWaybillCreateResult();
        result.setTrackingNo(trackingNo);
        result.setLabelUrl("https://labels.example.com/" + request.getCarrierCode().toLowerCase() + "/" + trackingNo + ".pdf");
        result.setLabelFormat("PDF");
        return result;
    }

    /**
     * 模拟物流商返回最新轨迹。
     *
     * @param request 轨迹拉取请求
     * @return 标准化轨迹
     */
    @Override
    public List<CarrierTrackEvent> pullTracks(CarrierTrackPullRequest request) {
        CarrierTrackEvent event = new CarrierTrackEvent();
        event.setTrackCode("IN_TRANSIT");
        event.setTrackStage(LogisticsConstants.WAYBILL_IN_TRANSIT);
        event.setRawStatus("In transit");
        event.setStatusDesc("运输中");
        event.setLocation("Carrier Hub");
        event.setLocationCountry("CN");
        event.setTrackTime(LocalDateTime.now());
        event.setException(false);
        return List.of(event);
    }
}
