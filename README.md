# FlexChain 柔性供应链 SaaS 后端

FlexChain 是一个面向跨境卖家的柔性供应链 SaaS 平台后端项目，采用 Spring Cloud 微服务架构，覆盖卖家工作台的核心业务域：商品、供应商、采购、仓储、订单、物流、财务、系统权限和 BI 数据分析。

项目目标不是简单 CRUD 演示，而是尽量打通真实业务链路：前端通过网关访问后端接口，后端通过数据库、Feign、租户上下文、RBAC 权限和业务状态机协同完成端到端操作。

## 页面预览

前端仓库为 `supplychain-10-web`，下面是当前卖家工作台的页面效果。

### 首页 Dashboard

![首页 Dashboard](sql/images/dashboard.png)

### 供应商列表

![供应商列表](sql/images/supplier-list.png)

## 核心能力

- 多租户基础：租户上下文、租户隔离和租户级业务数据。
- RBAC 权限：用户、角色、菜单、权限点、消息中心和仓储专员示例角色。
- PIM 商品中心：SPU/SKU、类目、价格、上下架状态和商品主档。
- SRM 供应商管理：供应商档案、资质、联系人、审核、启停和评分。
- PMS 采购管理：采购申请、询价、采购订单、收货和采购结算回调。
- WMS 仓储库存：仓库、库位、库存、入库、出库、盘点和库存流水。
- OMS 订单履约：销售订单、退款、审核、取消、异常和履约回写。
- TMS 物流管理：物流渠道、运单、轨迹、费用和物流推荐数据。
- FMS 财务管理：应付账款、付款记录、平台账单、利润和现金流。
- BI 数据分析：看板指标、补货建议、KPI 阈值和经营摘要接口。

## 技术栈

Java 17、Spring Boot、Spring Cloud Alibaba、Spring Cloud Gateway、Nacos、MyBatis-Plus、Sa-Token、OpenFeign、MySQL 8、Redis、Maven 多模块、XXL-JOB。

## 模块说明

| 模块 | 说明 |
| --- | --- |
| `supplychain-gateway` | 统一 API 网关、路由和租户请求头透传 |
| `supplychain-system` | 登录、用户、角色、菜单、权限、租户、消息中心 |
| `supplychain-product` | 商品 SPU/SKU、类目、价格和上下架状态 |
| `supplychain-supplier` | 供应商档案、资质、审核和状态管理 |
| `supplychain-purchase` | 采购申请、询价、采购订单、收货和结算回调 |
| `supplychain-warehouse` | 仓库、库位、库存、入库、出库和盘点 |
| `supplychain-order` | 销售订单、退款、平台同步和履约回调 |
| `supplychain-logistics` | 物流渠道、运单、轨迹和物流费用 |
| `supplychain-finance` | 应付、付款、平台账单、利润和现金流 |
| `supplychain-common` | 公共模型、Feign 契约、安全上下文、租户和套餐守卫 |

## 本地启动准备

环境要求：JDK 17+、Maven 3.8+、MySQL 8+、Redis、Nacos。

构建：

```powershell
mvn clean package -DskipTests
```

按模块编译示例：

```powershell
mvn -pl supplychain-system -am -DskipTests compile
mvn -pl supplychain-purchase,supplychain-warehouse,supplychain-finance -am -DskipTests compile
```

## SQL 使用流程

新环境、演示环境或面试展示环境，优先只使用 `sql` 目录下这两个最终入口文件：

```sql
source sql/00_full_schema.sql;
source sql/01_demo_seed.sql;
```

执行说明：

1. `sql/00_full_schema.sql`：从当前已验证的 `supplychain_dev` 数据库导出，包含 81 张表结构，会创建并重建 `supplychain_dev`。
2. `sql/01_demo_seed.sql`：从当前已验证的 `supplychain_dev` 数据库导出，只包含数据，包含租户、用户、角色、权限、菜单、字典、仓储专员、TMS 费率和完整演示业务数据。

推荐流程：

1. 创建或清空本地 MySQL 环境。
2. 执行 `sql/00_full_schema.sql`。
3. 执行 `sql/01_demo_seed.sql`。
4. 检查 Nacos 中 MySQL、Redis、端口和网关配置。
5. 启动后端服务。
6. 启动前端项目并访问卖家工作台。

旧的编号 SQL 文件仍保留在仓库中，作为开发历史和问题追溯使用。新同事搭环境时不要手动混跑 `15_mvp_demo_data.sql`、`15_mvp_demo_data_clean.sql`、`15_mvp_demo_data_repair.sql` 这类过程文件。

XXL-JOB 使用独立数据库 `xxl_job`。如果需要启动 XXL-JOB 调度中心，请单独执行：

```text
xxl-job-3.4.0/doc/db/tables_xxl_job.sql
```

## 开发约定

- 后端状态字段保持数字或稳定枚举 code，前端只负责中文展示。
- 禁止把中文状态直接传给后端 Integer 状态字段。
- 不用 mock 数据掩盖接口或链路错误。
- 关键业务操作要有状态机约束、错误提示和必要的测试覆盖。
- 跨模块链路要以真实 API 和数据库状态变化为准。
- 租户隔离、权限校验和套餐限制属于接口契约。

## 鸣谢

感谢周顺方、刘付延强带来的 token 支持。
