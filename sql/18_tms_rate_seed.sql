-- TMS usable rate seed for the merchant tenant demo chain.
-- Keeps waybill creation on real logistics pricing instead of mock fallback.

INSERT INTO logistics_rate (
    id,
    tenant_id,
    channel_id,
    country_code,
    zone,
    currency,
    first_weight_g,
    first_weight_price,
    extra_weight_g,
    extra_weight_price,
    min_charge,
    fuel_rate,
    peak_rate,
    remote_area_fee,
    effective_date,
    expire_date,
    create_by,
    update_by,
    is_deleted,
    version
)
SELECT
    910000000000001211,
    2059984036520636418,
    910000000000001111,
    'US',
    'US',
    'CNY',
    500.00,
    42.0000,
    500.00,
    18.0000,
    42.0000,
    0.0800,
    0.0000,
    0.0000,
    '2026-01-01',
    NULL,
    2059984037695041538,
    2059984037695041538,
    0,
    0
WHERE NOT EXISTS (
    SELECT 1
    FROM logistics_rate
    WHERE tenant_id = 2059984036520636418
      AND channel_id = 910000000000001111
      AND country_code = 'US'
      AND is_deleted = 0
);
