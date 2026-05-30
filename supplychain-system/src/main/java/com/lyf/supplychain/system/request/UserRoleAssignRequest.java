package com.lyf.supplychain.system.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * Batch user role assignment request.
 */
@Data
public class UserRoleAssignRequest {

    @NotNull(message = "角色ID集合不能为空")
    private List<Long> roleIds;
}
