package com.lyf.supplychain.system.request;

import com.lyf.supplychain.common.api.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * System role page query.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysRolePageQuery extends PageQuery {

    private String keyword;

    private Integer status;
}
