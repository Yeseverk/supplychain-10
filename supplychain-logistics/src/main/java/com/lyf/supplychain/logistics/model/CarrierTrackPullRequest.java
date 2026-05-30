package com.lyf.supplychain.logistics.model;

import com.lyf.supplychain.logistics.entity.LogisticsCarrier;
import com.lyf.supplychain.logistics.entity.LogisticsWaybill;
import lombok.Data;

/**
 * 物流商轨迹拉取请求。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Data
public class CarrierTrackPullRequest {

    private LogisticsCarrier carrier;
    private LogisticsWaybill waybill;
}
