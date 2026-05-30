# FlexChain Supply Chain Platform

FlexChain is a flexible cross-border supply chain SaaS platform. The backend is built as a Spring Cloud microservice system and covers seller-side business domains such as product management, supplier management, procurement, warehouse inventory, order fulfillment, logistics, finance settlement, system authorization, and business intelligence.

The project focuses on real business workflows and service collaboration. Core modules communicate through HTTP APIs, Feign clients, database persistence, and event-style callbacks to support end-to-end supply chain operations.

## Features

- Multi-tenant SaaS foundation with tenant context propagation.
- RBAC authorization with users, roles, menus, permission points, and message center.
- Product master data for SPU/SKU, category, pricing, and listing status.
- Supplier lifecycle management, including approval, enable/disable, and supplier collaboration.
- Procurement workflow for requisitions, inquiries, purchase orders, receipts, reconciliation, and settlement.
- Warehouse inventory workflow for inbound, outbound, stocktake, inventory logs, and stock adjustment.
- OMS workflow for platform orders, order audit, cancellation, exceptions, refunds, and fulfillment callbacks.
- TMS workflow for waybills, logistics channels, tracking refresh, exceptions, and logistics cost integration.
- FMS workflow for payables, payments, platform bills, profit analysis, and cash flow.
- BI APIs for dashboard metrics, replenishment suggestions, KPI data, and operational summaries.

## Architecture

```text
Frontend / Gateway
        |
        v
Spring Cloud Gateway
        |
        +-- supplychain-system      RBAC, tenant, auth, messages
        +-- supplychain-product     PIM, SPU/SKU, category, product status
        +-- supplychain-supplier    SRM, supplier profile and approval
        +-- supplychain-purchase    PMS, requisition/order/receipt
        +-- supplychain-warehouse   WMS, inbound/outbound/inventory
        +-- supplychain-order       OMS, order/refund/fulfillment
        +-- supplychain-logistics   TMS, waybill/channel/tracking
        +-- supplychain-finance     FMS, payable/bill/profit/cash flow
        +-- supplychain-common      shared core, security, Feign contracts
```

## Technology Stack

- Java 17
- Spring Boot
- Spring Cloud Alibaba
- Spring Cloud Gateway
- Nacos
- MyBatis-Plus
- Sa-Token
- OpenFeign
- MySQL
- Redis
- RocketMQ style event integration
- XXL-JOB
- Maven multi-module build

## Modules

| Module | Description |
| --- | --- |
| `supplychain-gateway` | Unified API gateway and tenant header propagation |
| `supplychain-system` | Authentication, users, roles, menus, permissions, tenants, messages |
| `supplychain-product` | Product SPU/SKU, product status, product query APIs |
| `supplychain-supplier` | Supplier profile, certification, approval and status management |
| `supplychain-purchase` | Purchase requisitions, inquiries, orders, receipts and purchase settlement callbacks |
| `supplychain-warehouse` | Warehouses, locations, inventory, inbound, outbound and stocktake |
| `supplychain-order` | Sales orders, refunds, platform sync and fulfillment callbacks |
| `supplychain-logistics` | Logistics channels, waybills, tracking and logistics fee processing |
| `supplychain-finance` | Payables, payment records, platform bills, profit and cash flow |
| `supplychain-common` | Shared models, Feign clients, security context, tenant and plan guards |

## Directory Structure

```text
.
├── nacos-config/                 # Nacos configuration examples
├── sql/                          # Database schema and seed scripts
├── supplychain-common/           # Shared libraries and contracts
├── supplychain-gateway/          # API gateway
├── supplychain-system/           # System and RBAC service
├── supplychain-product/          # Product service
├── supplychain-supplier/         # Supplier service
├── supplychain-purchase/         # Procurement service
├── supplychain-warehouse/        # Warehouse service
├── supplychain-order/            # Order service
├── supplychain-logistics/        # Logistics service
├── supplychain-finance/          # Finance and BI service
└── xxl-job-3.4.0/                # Job scheduler dependency/source package
```

## Getting Started

### Prerequisites

- JDK 17+
- Maven 3.8+
- MySQL 8+
- Redis
- Nacos

### Build

```powershell
mvn clean package -DskipTests
```

Compile selected service modules:

```powershell
mvn -pl supplychain-system -am -DskipTests compile
mvn -pl supplychain-purchase,supplychain-warehouse,supplychain-finance -am -DskipTests compile
```

### Database

SQL scripts are stored in `sql/`. For a fresh local or demo database, run `sql/00_full_schema.sql` first, then `sql/01_demo_seed.sql`. The older numbered SQL files are kept as source history and should not be mixed manually for new environments.

### Configuration

Nacos configuration samples are stored in `nacos-config/`.

Before starting services, check database, Redis, Nacos, service ports, tenant headers, and gateway routes in the corresponding YAML files.

## Development Notes

- Keep API status values as numeric codes or stable enum codes. Frontend display labels should not be sent directly to integer status fields.
- Do not use mock data to mask backend or integration errors in production-like environments.
- Cross-module workflows should be verified through real APIs and database state changes.
- Tenant context, permission checks, and plan limits are part of the service contract and should be preserved when adding new endpoints.

## Verification

Typical compile command:

```powershell
mvn -pl supplychain-finance -am -DskipTests compile
```

For full backend verification, run the relevant module compile or test commands according to the changed service scope.
