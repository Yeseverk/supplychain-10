package com.lyf.supplychain.system.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * User status update request.
 */
@Data
public class UserStatusUpdateRequest {

    @NotNull(message = "用户状态不能为空")
    private Integer status;
}
