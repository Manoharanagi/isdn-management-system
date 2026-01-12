# 🚀 GPS Tracking - Quick Start Guide

## ✅ Good News!

Your backend is **already running** on port 8080! Since you have `spring.jpa.hibernate.ddl-auto=update` enabled, **the GPS columns should already exist** in your database.

---

## 📋 Step-by-Step Setup

### Option A: Using MySQL Workbench (Easiest) ⭐

1. **Open MySQL Workbench**
2. **Connect to your database**: `localhost:3306` with username `root` (no password)
3. **Select database**: `isdn_db`
4. **Open and run these SQL files in order:**
   - `01_add_gps_columns.sql` (adds GPS columns if they don't exist)
   - `02_add_test_gps_data.sql` (adds test data for Sri Lankan cities)
   - `verify_setup.sql` (verifies everything worked)

### Option B: Using Command Line

If you have MySQL in your PATH:

```bash
cd C:\Users\manoh\Desktop\isdn-management-system\database
run_all_setup.bat
```

If MySQL is not in PATH, find your MySQL installation (usually `C:\Program Files\MySQL\MySQL Server 8.0\bin\`) and run:

```bash
cd C:\Users\manoh\Desktop\isdn-management-system\database

"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -h localhost isdn_db < 01_add_gps_columns.sql

"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -h localhost isdn_db < 02_add_test_gps_data.sql

"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -h localhost isdn_db < verify_setup.sql
```

### Option C: Manual SQL (Copy-Paste)

**Step 1: Add GPS Columns** (Open MySQL Workbench and run):

```sql
USE isdn_db;

-- Add GPS columns if they don't exist
ALTER TABLE deliveries
ADD COLUMN IF NOT EXISTS current_latitude DECIMAL(10, 8) NULL,
ADD COLUMN IF NOT EXISTS current_longitude DECIMAL(11, 8) NULL,
ADD COLUMN IF NOT EXISTS destination_latitude DECIMAL(10, 8) NULL,
ADD COLUMN IF NOT EXISTS destination_longitude DECIMAL(11, 8) NULL,
ADD COLUMN IF NOT EXISTS estimated_distance_km DECIMAL(10, 2) NULL,
ADD COLUMN IF NOT EXISTS actual_distance_km DECIMAL(10, 2) NULL;
```

**Step 2: Add Test Data** (Run this after Step 1):

```sql
USE isdn_db;

-- Delivery #1: Colombo (IN_TRANSIT)
UPDATE deliveries
SET
    destination_latitude = 6.9271,
    destination_longitude = 79.8612,
    current_latitude = 6.9500,
    current_longitude = 79.8800,
    estimated_distance_km = 5.2
WHERE delivery_id = 1;

-- Delivery #2: Galle
UPDATE deliveries
SET
    destination_latitude = 6.0535,
    destination_longitude = 80.2210,
    current_latitude = 6.5000,
    current_longitude = 80.0000,
    estimated_distance_km = 119.0
WHERE delivery_id = 2;

-- Delivery #3: Kandy
UPDATE deliveries
SET
    destination_latitude = 7.2906,
    destination_longitude = 80.6337,
    current_latitude = 7.1000,
    current_longitude = 80.3000,
    estimated_distance_km = 115.0
WHERE delivery_id = 3;

-- Delivery #4: Jaffna
UPDATE deliveries
SET
    destination_latitude = 9.6615,
    destination_longitude = 80.0255,
    current_latitude = 8.3114,
    current_longitude = 80.4037,
    estimated_distance_km = 396.0
WHERE delivery_id = 4;

-- Delivery #5: Negombo
UPDATE deliveries
SET
    destination_latitude = 7.2088,
    destination_longitude = 79.8358,
    current_latitude = 7.0000,
    current_longitude = 79.8500,
    estimated_distance_km = 37.0
WHERE delivery_id = 5;

-- Verify updates
SELECT
    delivery_id,
    status,
    current_latitude,
    current_longitude,
    destination_latitude,
    destination_longitude,
    estimated_distance_km
FROM deliveries
WHERE delivery_id <= 5;
```

---

## 🧪 Test the Setup

### Test 1: Check Database

Run this query in MySQL Workbench:

```sql
SELECT
    delivery_id,
    status,
    CONCAT(current_latitude, ', ', current_longitude) AS current_location,
    CONCAT(destination_latitude, ', ', destination_longitude) AS destination
FROM deliveries
WHERE delivery_id <= 5;
```

Expected: You should see 5 deliveries with GPS coordinates.

### Test 2: Check API Response

Open a browser or use cURL:

```
http://localhost:8080/api/deliveries/1
```

**With Authentication** (if required):

1. Login first to get token
2. Use token in header:
   ```bash
   curl -H "Authorization: Bearer YOUR_TOKEN" http://localhost:8080/api/deliveries/1
   ```

**Expected Response:**

```json
{
  "deliveryId": 1,
  "currentLatitude": 6.95,
  "currentLongitude": 79.88,
  "destinationLatitude": 6.9271,
  "destinationLongitude": 79.8612,
  "estimatedDistanceKm": 5.2,
  ...
}
```

### Test 3: Check Frontend Map

1. Open your React frontend
2. Navigate to the map component
3. **You should see markers** for all deliveries with GPS data!

---

## 📍 GPS Test Data Locations

After running the setup, you'll have deliveries at these locations:

| ID | City | Destination | Current Location | Status |
|----|------|-------------|------------------|--------|
| 1 | **Colombo** | 6.9271, 79.8612 | 6.9500, 79.8800 | IN_TRANSIT |
| 2 | **Galle** | 6.0535, 80.2210 | 6.5000, 80.0000 | ASSIGNED |
| 3 | **Kandy** | 7.2906, 80.6337 | 7.1000, 80.3000 | ASSIGNED |
| 4 | **Jaffna** | 9.6615, 80.0255 | 8.3114, 80.4037 | ASSIGNED |
| 5 | **Negombo** | 7.2088, 79.8358 | 7.0000, 79.8500 | ASSIGNED |

---

## ❓ Troubleshooting

### Problem: No deliveries exist in database

**Solution:** Create sample orders and deliveries first:

```sql
-- Check if deliveries exist
SELECT COUNT(*) FROM deliveries;

-- If count is 0, you need to create sample data
-- Run your data initialization scripts first
```

### Problem: "Column already exists" error

**Solution:** This is fine! The columns were already created by Hibernate. Skip Step 1 and go directly to Step 2 (adding test data).

### Problem: API returns 401 Unauthorized

**Solution:** Your API requires authentication. Login first to get a JWT token, then include it in your API requests.

### Problem: API returns empty array `[]`

**Solution:** No deliveries exist yet. Create sample orders and deliveries first.

---

## 🎯 Next Steps

1. ✅ **Database Setup** - Run the SQL scripts above
2. ✅ **Verify API** - Test http://localhost:8080/api/deliveries/1
3. ✅ **Open Frontend** - Check the map component
4. ✅ **See Markers** - Deliveries should appear on the map!

---

## 📝 Additional Notes

- **Backend is already running** on port 8080
- **Hibernate auto-update** should have created the columns
- **Just add the test GPS data** and you're done!
- **Map markers** will appear automatically once you add GPS coordinates

---

## 🆘 Need Help?

1. Check `README_GPS_SETUP.md` for detailed documentation
2. Run `verify_setup.sql` to verify database setup
3. Check browser console for frontend errors
4. Check backend logs for API errors

---

**You're almost there! Just run the SQL scripts and your map will come alive! 🗺️**
