package com.lyf.supplychain.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统可靠事件发件箱实体。
 *
 * @author liyunfei
 * @date 2026-05-21
 */
@Data
@TableName("sys_event_outbox")
public class SysEventOutbox {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long tenantId;

    private String eventId;

    private String eventType;

    private String sourceService;

    private String bizType;

    private String bizId;

    private String idempotentKey;

    private String payload;

    private Integer status;

    private Integer retryCount;

    private String errorMsg;

    private LocalDateTime occurredTime;

    private LocalDateTime dispatchTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}
