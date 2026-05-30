package com.lyf.supplychain.system.request;

import com.lyf.supplychain.common.api.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Message center page query.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MessagePageQuery extends PageQuery {

    private String keyword;

    private Integer readStatus;

    private String type;
}
