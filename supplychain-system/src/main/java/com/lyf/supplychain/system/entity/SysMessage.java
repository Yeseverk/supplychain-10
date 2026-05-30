package com.lyf.supplychain.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统站内信消息实体。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
@TableName("sys_message")
public class SysMessage {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long tenantId;

    private Long receiverId;

    private String receiverType;

    private String receiverKey;

    private String title;

    private String content;

    private String bizType;

    private String bizId;

    private String priority;

    private Integer readStatus;

    private LocalDateTime readTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}
