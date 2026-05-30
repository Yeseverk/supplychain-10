package com.lyf.supplychain.logistics.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 物流轨迹实体。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
@TableName("logistics_track")
public class LogisticsTrack {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long tenantId;
    private Long waybillId;
    private String trackingNo;
    private String trackCode;
    private Integer trackStage;
    private String rawStatus;
    private String statusDesc;
    private String location;
    private String locationCountry;
    private LocalDateTime trackTime;
    private LocalDateTime fetchTime;
    private Integer isException;
    private Integer exceptionType;
    private String exceptionDesc;
}
