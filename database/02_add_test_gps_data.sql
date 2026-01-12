-- =====================================================
-- STEP 2: Add Test GPS Data for Sri Lanka Deliveries
-- =====================================================
-- Database: isdn_db
-- Table: deliveries
-- Description: Inserts sample GPS coordinates for testing the map feature
-- =====================================================

USE isdn_db;

-- =====================================================
-- Important GPS Coordinates for Sri Lanka
-- =====================================================
-- Colombo City Center: 6.9271° N, 79.8612° E
-- Galle:               6.0535° N, 80.2210° E
-- Kandy:               7.2906° N, 80.6337° E
-- Jaffna:              9.6615° N, 80.0255° E
-- Negombo:             7.2088° N, 79.8358° E
-- Matara:              5.9549° N, 80.5550° E
-- Anuradhapura:        8.3114° N, 80.4037° E
-- =====================================================

-- First, let's see what deliveries exist
SELECT
    delivery_id,
    status,
    current_latitude,
    current_longitude,
    destination_latitude,
    destination_longitude
FROM deliveries
LIMIT 10;

-- =====================================================
-- Update Delivery #1: Colombo Delivery (IN_TRANSIT)
-- =====================================================
-- Destination: Colombo City Center
-- Current Location: En route (north of destination)
UPDATE deliveries
SET
    destination_latitude = 6.9271,
    destination_longitude = 79.8612,
    current_latitude = 6.9500,
    current_longitude = 79.8800,
    estimated_distance_km = 5.2
WHERE delivery_id = 1;

-- =====================================================
-- Update Delivery #2: Galle Delivery
-- =====================================================
-- Destination: Galle
-- Current Location: En route from Colombo
UPDATE deliveries
SET
    destination_latitude = 6.0535,
    destination_longitude = 80.2210,
    current_latitude = 6.5000,
    current_longitude = 80.0000,
    estimated_distance_km = 119.0
WHERE delivery_id = 2;

-- =====================================================
-- Update Delivery #3: Kandy Delivery
-- =====================================================
-- Destination: Kandy
-- Current Location: Halfway from Colombo
UPDATE deliveries
SET
    destination_latitude = 7.2906,
    destination_longitude = 80.6337,
    current_latitude = 7.1000,
    current_longitude = 80.3000,
    estimated_distance_km = 115.0
WHERE delivery_id = 3;

-- =====================================================
-- Update Delivery #4: Jaffna Delivery
-- =====================================================
-- Destination: Jaffna
-- Current Location: Anuradhapura (midway point)
UPDATE deliveries
SET
    destination_latitude = 9.6615,
    destination_longitude = 80.0255,
    current_latitude = 8.3114,
    current_longitude = 80.4037,
    estimated_distance_km = 396.0
WHERE delivery_id = 4;

-- =====================================================
-- Update Delivery #5: Negombo Delivery
-- =====================================================
-- Destination: Negombo
-- Current Location: Just left Colombo
UPDATE deliveries
SET
    destination_latitude = 7.2088,
    destination_longitude = 79.8358,
    current_latitude = 7.0000,
    current_longitude = 79.8500,
    estimated_distance_km = 37.0
WHERE delivery_id = 5;

-- =====================================================
-- Verify the updates
-- =====================================================
SELECT
    d.delivery_id,
    d.status,
    CONCAT(COALESCE(d.current_latitude, 0), ', ', COALESCE(d.current_longitude, 0)) AS current_location,
    CONCAT(COALESCE(d.destination_latitude, 0), ', ', COALESCE(d.destination_longitude, 0)) AS destination,
    d.estimated_distance_km AS distance_km,
    o.order_number,
    o.delivery_address
FROM deliveries d
INNER JOIN orders o ON d.order_id = o.order_id
WHERE d.delivery_id IN (1, 2, 3, 4, 5);

-- Success message
SELECT 'Test GPS data added successfully! Check the map for markers.' AS Status;
