-- =====================================================
-- STEP 1: Add GPS Coordinate Columns to Deliveries Table
-- =====================================================
-- Database: isdn_db
-- Table: deliveries
-- Description: Adds GPS tracking columns for delivery locations
-- =====================================================

USE isdn_db;

-- Check if columns exist before adding (MySQL syntax)
-- If columns already exist, these will be skipped

-- Add current location columns (where the delivery driver is now)
ALTER TABLE deliveries
ADD COLUMN IF NOT EXISTS current_latitude DECIMAL(10, 8) NULL,
ADD COLUMN IF NOT EXISTS current_longitude DECIMAL(11, 8) NULL;

-- Add destination location columns (where the delivery is going)
ALTER TABLE deliveries
ADD COLUMN IF NOT EXISTS destination_latitude DECIMAL(10, 8) NULL,
ADD COLUMN IF NOT EXISTS destination_longitude DECIMAL(11, 8) NULL;

-- Add additional columns for tracking
ALTER TABLE deliveries
ADD COLUMN IF NOT EXISTS estimated_distance_km DECIMAL(10, 2) NULL,
ADD COLUMN IF NOT EXISTS actual_distance_km DECIMAL(10, 2) NULL;

-- Verify columns were added
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
      'destination_longitude',
      'estimated_distance_km',
      'actual_distance_km'
  )
ORDER BY ORDINAL_POSITION;

-- Success message
SELECT 'GPS columns added successfully!' AS Status;
