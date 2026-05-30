package com.lyf.supplychain.logistics.model;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 标准化物流轨迹事件。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Data
public class CarrierTrackEvent {

    private String trackCode;
    private Integer trackStage;
    private String rawStatus;
    private String statusDesc;
    private String location;
    private String locationCountry;
    private LocalDateTime trackTime;
    private Boolean exception;
    private Integer exceptionType;
    private String exceptionDesc;
}
