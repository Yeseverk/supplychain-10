package com.lyf.supplychain.logistics.model;

import com.lyf.supplychain.logistics.entity.LogisticsCarrier;
import com.lyf.supplychain.logistics.entity.LogisticsChannel;
import com.lyf.supplychain.logistics.entity.LogisticsWaybill;
import lombok.Data;

/**
 * 物流商创建运单请求。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Data
public class CarrierWaybillCreateRequest {

    private LogisticsCarrier carrier;
    private LogisticsChannel channel;
    private LogisticsWaybill waybill;

    /**
     * 获取物流商编码。
     *
     * @return 物流商编码
     */
    public String getCarrierCode() {
        return carrier == null ? "DEFAULT" : carrier.getCarrierCode();
    }

    /**
     * 获取内部运单号。
     *
     * @return 内部运单号
     */
    public String getWaybillNo() {
        return waybill == null ? "UNKNOWN" : waybill.getWaybillNo();
    }
}
