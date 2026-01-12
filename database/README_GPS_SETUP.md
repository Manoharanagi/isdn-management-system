# GPS Tracking Database Setup

This folder contains SQL scripts to set up GPS tracking for the ISDN Management System.

## 📁 Files Overview

| File | Description |
|------|-------------|
| `01_add_gps_columns.sql` | Adds GPS coordinate columns to the deliveries table |
| `02_add_test_gps_data.sql` | Inserts sample GPS data for Sri Lankan locations |
| `03_test_api.bat` | Tests the API to verify GPS data is being returned |
| `run_all_setup.bat` | Master script that runs all setup steps |

## 🚀 Quick Start (Recommended)

### Option 1: Automatic Setup (Windows)

1. **Open Command Prompt** in the `database` folder
2. **Run the master script:**
   ```bash
   run_all_setup.bat
   ```
3. **Follow the prompts**

That's it! The script will:
- ✅ Add GPS columns to deliveries table
- ✅ Insert test GPS data for 5 deliveries
- ✅ Verify the changes

---

## 🔧 Manual Setup (Step by Step)

### Prerequisites

- MySQL server running on `localhost:3306`
- Database: `isdn_db` exists
- MySQL CLI installed and in PATH

### Step 1: Add GPS Columns

Run this command in the `database` folder:

```bash
mysql -u root -h localhost isdn_db < 01_add_gps_columns.sql
```

**What it does:**
- Adds `current_latitude` and `current_longitude` columns (where driver is now)
- Adds `destination_latitude` and `destination_longitude` columns (delivery destination)
- Adds `estimated_distance_km` and `actual_distance_km` columns

### Step 2: Add Test GPS Data

```bash
mysql -u root -h localhost isdn_db < 02_add_test_gps_data.sql
```

**What it does:**
- Updates 5 deliveries with real Sri Lankan GPS coordinates:
  - **Delivery #1**: Colombo City Center
  - **Delivery #2**: Galle
  - **Delivery #3**: Kandy
  - **Delivery #4**: Jaffna
  - **Delivery #5**: Negombo

### Step 3: Verify Database Changes

```bash
mysql -u root -h localhost isdn_db -e "SELECT delivery_id, status, current_latitude, current_longitude, destination_latitude, destination_longitude FROM deliveries LIMIT 5;"
```

Expected output:
```
+-------------+--------------+------------------+-------------------+----------------------+-----------------------+
| delivery_id | status       | current_latitude | current_longitude | destination_latitude | destination_longitude |
+-------------+--------------+------------------+-------------------+----------------------+-----------------------+
|           1 | IN_TRANSIT   |         6.950000 |         79.880000 |             6.927100 |             79.861200 |
|           2 | ASSIGNED     |         6.500000 |         80.000000 |             6.053500 |             80.221000 |
|           3 | ASSIGNED     |         7.100000 |         80.300000 |             7.290600 |             80.633700 |
...
```

---

## 🧪 Test the API

### Option 1: Using the Batch Script

```bash
03_test_api.bat
```

### Option 2: Manual cURL Test

```bash
curl -X GET http://localhost:8080/api/deliveries/1 -H "Content-Type: application/json"
```

### Expected API Response:

```json
{
  "deliveryId": 1,
  "orderId": 1,
  "orderNumber": "ORD-20250108-001",
  "driverId": 1,
  "driverName": "John Driver",
  "vehicleNumber": "CAB-1234",
  "status": "IN_TRANSIT",
  "currentLatitude": 6.9500,
  "currentLongitude": 79.8800,
  "destinationLatitude": 6.9271,
  "destinationLongitude": 79.8612,
  "estimatedDistanceKm": 5.2,
  ...
}
```

✅ **Success!** Your backend is now returning GPS coordinates!

---

## 📍 GPS Coordinates Reference

### Major Sri Lankan Cities

| City | Latitude | Longitude | Notes |
|------|----------|-----------|-------|
| **Colombo** | 6.9271 | 79.8612 | Capital city, commercial hub |
| **Galle** | 6.0535 | 80.2210 | Southern coastal city |
| **Kandy** | 7.2906 | 80.6337 | Cultural capital, central highlands |
| **Jaffna** | 9.6615 | 80.0255 | Northern peninsula |
| **Negombo** | 7.2088 | 79.8358 | Beach resort town |
| **Matara** | 5.9549 | 80.5550 | Southern coast |
| **Anuradhapura** | 8.3114 | 80.4037 | Ancient city, north-central |

### Using Custom Coordinates

To add your own GPS coordinates:

```sql
UPDATE deliveries
SET
    destination_latitude = YOUR_LAT,
    destination_longitude = YOUR_LNG,
    current_latitude = DRIVER_LAT,
    current_longitude = DRIVER_LNG
WHERE delivery_id = X;
```

---

## 🗺️ Frontend Integration

Once the database is set up, your React frontend map will automatically display delivery markers!

**Map Component**: The frontend map component reads from:
```javascript
GET /api/deliveries/active
```

Each delivery object includes:
- `currentLatitude` / `currentLongitude` - Current driver location (blue marker)
- `destinationLatitude` / `destinationLongitude` - Delivery destination (red marker)

---

## ❌ Troubleshooting

### Error: "Unknown database 'isdn_db'"

**Solution**: Create the database first:
```sql
CREATE DATABASE IF NOT EXISTS isdn_db;
```

### Error: "Access denied for user 'root'@'localhost'"

**Solution**: Update your MySQL root password in the commands:
```bash
mysql -u root -pYOUR_PASSWORD -h localhost isdn_db < 01_add_gps_columns.sql
```

### Error: "Table 'deliveries' doesn't exist"

**Solution**: Make sure you've run your Spring Boot application at least once with `spring.jpa.hibernate.ddl-auto=update`. This creates all tables automatically.

### Columns already exist

If you get "Duplicate column name" error, it means Hibernate already created the columns. You can skip Step 1 and go directly to Step 2 (adding test data).

---

## 🔄 Reset GPS Data

To reset all GPS coordinates:

```sql
UPDATE deliveries
SET
    current_latitude = NULL,
    current_longitude = NULL,
    destination_latitude = NULL,
    destination_longitude = NULL,
    estimated_distance_km = NULL;
```

---

## 📝 Notes

- **Data Type**: Using `DECIMAL(10, 8)` for latitude and `DECIMAL(11, 8)` for longitude provides high precision GPS coordinates
- **Nullable**: All GPS columns are nullable since not all deliveries may have coordinates initially
- **Performance**: Consider adding indexes if you'll be querying by coordinates frequently:
  ```sql
  CREATE INDEX idx_deliveries_current_location ON deliveries(current_latitude, current_longitude);
  CREATE INDEX idx_deliveries_destination ON deliveries(destination_latitude, destination_longitude);
  ```

---

## ✅ Checklist

After running the setup, verify:

- [ ] GPS columns exist in deliveries table
- [ ] At least 5 deliveries have GPS coordinates
- [ ] API endpoint `/api/deliveries/1` returns latitude/longitude fields
- [ ] Frontend map displays delivery markers
- [ ] Clicking markers shows delivery details

---

## 🎯 Next Steps

1. ✅ Database setup complete
2. 🚀 Start Spring Boot backend
3. 🗺️ Open frontend map component
4. 👀 See markers appear on the map!
5. 📱 Test real-time GPS updates (if implemented)

---

**Need help?** Check the main project README or contact the development team.

**Happy Mapping! 🗺️**
