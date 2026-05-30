package com.lyf.supplychain.common.security.constant;

/**
 * 系统权限标识常量。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
public final class PermissionCodes {

    public static final String SYS_USER_LIST = "sys:user:list";
    public static final String SYS_USER_ADD = "sys:user:add";
    public static final String SYS_USER_EDIT = "sys:user:edit";
    public static final String SYS_USER_DELETE = "sys:user:delete";
    public static final String SYS_ROLE_MANAGE = "sys:role:manage";
    public static final String SYS_MENU_MANAGE = "sys:menu:manage";
    public static final String SYS_TENANT_MANAGE = "sys:tenant:manage";
    public static final String SYS_MESSAGE_MANAGE = "sys:message:manage";
    public static final String SYS_AUDIT_LIST = "sys:audit:list";

    public static final String SAAS_TENANT_MANAGE = "saas:tenant:manage";
    public static final String SAAS_PLAN_MANAGE = "saas:plan:manage";

    public static final String SRM_SUPPLIER_LIST = "srm:supplier:list";
    public static final String SRM_SUPPLIER_ADD = "srm:supplier:add";
    public static final String SRM_SUPPLIER_EDIT = "srm:supplier:edit";
    public static final String SRM_SUPPLIER_DELETE = "srm:supplier:delete";
    public static final String SRM_SUPPLIER_AUDIT = "srm:supplier:audit";

    public static final String PMS_ORDER_LIST = "pms:order:list";
    public static final String PMS_ORDER_MANAGE = "pms:order:manage";
    public static final String PMS_RECEIPT_CONFIRM = "pms:receipt:confirm";
    public static final String PMS_REQUISITION_AUDIT = "pms:requisition:audit";

    public static final String WMS_WAREHOUSE_MANAGE = "wms:warehouse:manage";
    public static final String WMS_INVENTORY_LIST = "wms:inventory:list";
    public static final String WMS_INVENTORY_ADJUST = "wms:inventory:adjust";
    public static final String WMS_INBOUND_MANAGE = "wms:inbound:manage";
    public static final String WMS_OUTBOUND_MANAGE = "wms:outbound:manage";
    public static final String WMS_STOCKTAKE_AUDIT = "wms:stocktake:audit";

    public static final String PIM_PRODUCT_MANAGE = "pim:product:manage";
    public static final String OMS_ORDER_LIST = "oms:order:list";
    public static final String OMS_ORDER_MANAGE = "oms:order:manage";
    public static final String TMS_WAYBILL_ADD = "tms:waybill:add";
    public static final String TMS_LOGISTICS_MANAGE = "tms:logistics:manage";
    public static final String FMS_BILL_IMPORT = "fms:bill:import";
    public static final String FMS_PROFIT_VIEW = "fms:profit:view";
    public static final String BI_DASHBOARD_VIEW = "bi:dashboard:view";

    private PermissionCodes() {
    }
}
