# FlexChain SQL Usage

For a new local or demo database, use only these two consolidated entry files:

1. `00_full_schema.sql`
   - Exported from the current verified `supplychain_dev` database by `mysqldump --no-data`.
   - Recreates all current table structures from system, SRM, PMS, WMS, PIM, OMS, TMS, FMS, BI, SaaS/RBAC, and Seata support tables.
   - Contains `DROP TABLE IF EXISTS`, so use it for a fresh/demo database reset, not as an online migration script.

2. `01_demo_seed.sql`
   - Exported from the current verified `supplychain_dev` database by `mysqldump --no-create-info --complete-insert`.
   - Inserts tenant, menu, role, permission, dictionary, warehouse-specialist RBAC, TMS rate, and seller-workspace demo/business data.
   - Intended for interview/demo/local integration usage after the schema file has been executed.

Recommended execution order:

```sql
source sql/00_full_schema.sql;
source sql/01_demo_seed.sql;
```


XXL-JOB note:

The bundled XXL-JOB admin package keeps its own database script at `xxl-job-3.4.0/doc/db/tables_xxl_job.sql`. It creates the separate `xxl_job` database and should be executed only when you also deploy the XXL-JOB admin service. It is not mixed into the seller-workspace `supplychain_dev` schema.

The older numbered SQL files are retained as source history. New environments should start from the two consolidated files above.

To refresh these two files later, export again from the verified local MySQL database with `mysqldump`.
