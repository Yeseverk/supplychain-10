package com.lyf.supplychain.common.security.model;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 当前登录用户快照。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser implements Serializable {

    private Long userId;

    private Long tenantId;

    private String tenantCode;

    private String username;

    private String realName;

    private Integer userType;

    private Integer planType;

    private Integer dataScope;

    private Long supplierId;

    @Builder.Default
    private List<Long> storeIds = Collections.emptyList();

    @Builder.Default
    private List<Long> warehouseIds = Collections.emptyList();

    @Builder.Default
    private List<String> roles = Collections.emptyList();

    @Builder.Default
    private List<String> permissions = Collections.emptyList();
}
