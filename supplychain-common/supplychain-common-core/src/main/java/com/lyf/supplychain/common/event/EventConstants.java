package com.lyf.supplychain.common.event;

/**
 * 领域事件公共常量。
 *
 * @author liyunfei
 * @date 2026-05-21
 */
public final class EventConstants {

    private EventConstants() {
    }

    /**
     * 事件类型。
     *
     * @author liyunfei
     * @date 2026-05-21
     */
    public static final class EventType {

        public static final String SYSTEM_NOTIFICATION = "SYSTEM_NOTIFICATION";

        public static final String WMS_OUTBOUND_COMPLETED = "WMS_OUTBOUND_COMPLETED";

        private EventType() {
        }
    }

    /**
     * 事件来源服务。
     *
     * @author liyunfei
     * @date 2026-05-21
     */
    public static final class SourceService {

        public static final String SUPPLIER = "supplychain-supplier";

        public static final String WAREHOUSE = "supplychain-warehouse";

        public static final String LOGISTICS = "supplychain-logistics";

        public static final String FINANCE = "supplychain-finance";

        private SourceService() {
        }
    }

    /**
     * 通知业务类型。
     *
     * @author liyunfei
     * @date 2026-05-21
     */
    public static final class BizType {

        public static final String WMS_INVENTORY_WARNING = "WMS_INVENTORY_WARNING";

        public static final String TMS_LOGISTICS_EXCEPTION = "TMS_LOGISTICS_EXCEPTION";

        public static final String FMS_LOSS_WARNING = "FMS_LOSS_WARNING";

        public static final String FMS_PAYABLE_DUE_WARNING = "FMS_PAYABLE_DUE_WARNING";

        private BizType() {
        }
    }

    /**
     * 通知接收角色。
     *
     * @author liyunfei
     * @date 2026-05-21
     */
    public static final class ReceiverRole {

        public static final String WAREHOUSE_MANAGER = "ROLE_WAREHOUSE_MANAGER";

        public static final String LOGISTICS_MANAGER = "ROLE_LOGISTICS_MANAGER";

        public static final String FINANCE_MANAGER = "ROLE_FINANCE_MANAGER";

        private ReceiverRole() {
        }
    }

    /**
     * 通知通用值。
     *
     * @author liyunfei
     * @date 2026-05-21
     */
    public static final class Notification {

        public static final String RECEIVER_TYPE_ROLE = "ROLE";

        public static final String PRIORITY_HIGH = "HIGH";

        private Notification() {
        }
    }

    /**
     * 事件处理状态。
     *
     * @author liyunfei
     * @date 2026-05-21
     */
    public static final class Status {

        public static final Integer PENDING = 0;

        public static final Integer DISPATCHED = 1;

        public static final Integer FAILED = 2;

        public static final Integer IGNORED = 3;

        private Status() {
        }
    }
}
