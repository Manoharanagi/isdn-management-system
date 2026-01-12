-- =====================================================
-- GPS Setup Verification Script
-- =====================================================
-- Run this script to verify GPS tracking is working
-- =====================================================

USE isdn_db;

-- Show banner
SELECT '=====================================================';
SELECT 'GPS Tracking Setup Verification';
SELECT '=====================================================';
SELECT '';

-- Check 1: Verify columns exist
SELECT '✓ CHECK 1: Verify GPS columns exist' AS Check_Status;
SELECT
    COLUMN_NAME,
    DATA_TYPE,
    COLUMN_TYPE,
    IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'isdn_db'
  AND TABLE_NAME = 'deliveries'
  AND COLUMN_NAME IN (
      'current_latitude',
      'current_longitude',
      'destination_latitude',
      'destination_longitude'
  )
ORDER BY ORDINAL_POSITION;

SELECT '';

-- Check 2: Count deliveries with GPS data
SELECT '✓ CHECK 2: Count deliveries with GPS data' AS Check_Status;
SELECT
    COUNT(*) AS total_deliveries,
    SUM(CASE WHEN destination_latitude IS NOT NULL THEN 1 ELSE 0 END) AS has_destination_gps,
    SUM(CASE WHEN current_latitude IS NOT NULL THEN 1 ELSE 0 END) AS has_current_gps,
    SUM(CASE WHEN destination_latitude IS NOT NULL AND current_latitude IS NOT NULL THEN 1 ELSE 0 END) AS has_both_gps
FROM deliveries;

SELECT '';

-- Check 3: Show sample deliveries with GPS data
SELECT '✓ CHECK 3: Sample deliveries with GPS coordinates' AS Check_Status;
SELECT
    d.delivery_id,
    o.order_number,
    d.status,
    CONCAT(d.current_latitude, ', ', d.current_longitude) AS current_location,
    CONCAT(d.destination_latitude, ', ', d.destination_longitude) AS destination,
    d.estimated_distance_km AS distance_km,
    o.delivery_address
FROM deliveries d
INNER JOIN orders o ON d.order_id = o.order_id
WHERE d.destination_latitude IS NOT NULL
LIMIT 5;

SELECT '';

-- Check 4: Validate GPS coordinate ranges
SELECT '✓ CHECK 4: Validate GPS coordinate ranges (Sri Lanka bounds)' AS Check_Status;
SELECT
    delivery_id,
    current_latitude,
    current_longitude,
    destination_latitude,
    destination_longitude,
    CASE
        WHEN destination_latitude BETWEEN 5.9 AND 9.9 AND destination_longitude BETWEEN 79.5 AND 81.9 THEN 'Valid (Sri Lanka)'
        WHEN destination_latitude IS NULL THEN 'No GPS data'
        ELSE 'Out of Sri Lanka bounds'
    END AS validation_status
FROM deliveries
WHERE destination_latitude IS NOT NULL
LIMIT 10;

SELECT '';

-- Check 5: Summary report
SELECT '✓ CHECK 5: Summary Report' AS Check_Status;
SELECT
    'Total Deliveries' AS Metric,
    COUNT(*) AS Value
FROM deliveries

UNION ALL

SELECT
    'Deliveries with GPS',
    COUNT(*)
FROM deliveries
WHERE destination_latitude IS NOT NULL

UNION ALL

SELECT
    'In Transit with GPS',
    COUNT(*)
FROM deliveries
WHERE status = 'IN_TRANSIT' AND current_latitude IS NOT NULL

UNION ALL

SELECT
    'GPS Coverage %',
    ROUND((COUNT(CASE WHEN destination_latitude IS NOT NULL THEN 1 END) * 100.0 / COUNT(*)), 2)
FROM deliveries;

SELECT '';
SELECT '=====================================================';
SELECT 'Verification Complete!';
SELECT '=====================================================';
SELECT '';
SELECT 'Next step: Test the API with curl or 03_test_api.bat';
SELECT '';
