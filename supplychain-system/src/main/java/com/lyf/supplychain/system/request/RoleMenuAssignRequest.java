package com.lyf.supplychain.system.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 角色菜单批量授权请求。
 */
@Data
public class RoleMenuAssignRequest {

    @NotNull(message = "菜单ID集合不能为空")
    private List<Long> menuIds;
}
