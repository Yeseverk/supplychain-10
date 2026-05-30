# FlexChain SQL 使用说明

本目录保留两类 SQL：

1. 最终入口 SQL：给新环境、演示环境、面试展示环境使用。
2. 历史编号 SQL：保留开发过程和问题追溯，不建议新环境手动混跑。

## 推荐使用的两个文件

### 1. `00_full_schema.sql`

作用：创建并重建 `supplychain_dev` 的完整表结构。

说明：

- 由当前已验证的本地 MySQL 数据库通过 `mysqldump --no-data` 导出。
- 包含 81 张当前项目使用的表。
- 覆盖系统、SRM、PMS、WMS、PIM、OMS、TMS、FMS、BI、SaaS/RBAC 和 Seata 支撑表。
- 文件中包含 `DROP TABLE IF EXISTS`，适合新库初始化或演示库重建，不适合生产在线迁移。

### 2. `01_demo_seed.sql`

作用：写入当前项目演示和联调所需的数据。

说明：

- 由当前已验证的本地 MySQL 数据库通过 `mysqldump --no-create-info --complete-insert` 导出。
- 只包含数据，不包含建表语句。
- 包含租户、用户、角色、菜单、权限、字典、消息、仓储专员、TMS 费率和完整业务演示数据。
- 适合求职展示、前后端联调、Agent 数据分析演示和本地业务链路验证。

## 食用 SQL 流程

在 MySQL 客户端中执行：

```sql
source sql/00_full_schema.sql;
source sql/01_demo_seed.sql;
```

完整流程建议：

1. 确认 MySQL 8+ 已启动。
2. 确认当前连接用户有建库、删表、建表、写入数据权限。
3. 先执行 `00_full_schema.sql`。
4. 再执行 `01_demo_seed.sql`。
5. 检查 `supplychain_dev` 是否有 81 张表。
6. 检查用户、角色、权限和业务演示数据是否存在。
7. 启动 Nacos、Redis、后端服务和前端项目。

## 注意事项

- 不要把旧的 `15_mvp_demo_data.sql`、`15_mvp_demo_data_clean.sql`、`15_mvp_demo_data_repair.sql` 混着执行。
- 旧文件是开发过程记录，不是新环境最佳入口。
- 如果后续数据库结构或演示数据再次变化，应该重新从已验证数据库导出这两个最终文件。
- 这两个文件面向 `supplychain_dev`。XXL-JOB 使用独立数据库 `xxl_job`。

## XXL-JOB

如果需要启动 XXL-JOB 调度中心，请单独执行官方脚本：

```text
xxl-job-3.4.0/doc/db/tables_xxl_job.sql
```

它会创建独立的 `xxl_job` 数据库，不混入 `supplychain_dev`。

## 鸣谢

感谢周顺方、刘付延强带来的 token 支持。
