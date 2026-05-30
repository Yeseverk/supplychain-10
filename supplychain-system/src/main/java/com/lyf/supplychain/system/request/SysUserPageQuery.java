package com.lyf.supplychain.system.request;

import com.lyf.supplychain.common.api.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * System user page query.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysUserPageQuery extends PageQuery {

    private String keyword;

    private Integer status;
}
