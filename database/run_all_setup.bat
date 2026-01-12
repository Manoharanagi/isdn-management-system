@echo off
REM =====================================================
REM GPS Tracking Setup - Master Script
REM =====================================================
REM This script sets up GPS tracking for the ISDN system
REM Author: Claude AI
REM Date: 2026-01-08
REM =====================================================

echo =====================================================
echo ISDN Management System - GPS Tracking Setup
echo =====================================================
echo.
echo This script will:
echo   1. Add GPS coordinate columns to deliveries table
echo   2. Insert test GPS data for Sri Lankan locations
echo   3. Verify the database changes
echo.
echo Database: isdn_db
echo MySQL Server: localhost:3306
echo Username: root
echo Password: (empty)
echo.

pause

echo.
echo =====================================================
echo STEP 1: Adding GPS columns to deliveries table...
echo =====================================================
mysql -u root -h localhost isdn_db < 01_add_gps_columns.sql
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Failed to add GPS columns!
    pause
    exit /b 1
)
echo SUCCESS: GPS columns added!
echo.

echo =====================================================
echo STEP 2: Adding test GPS data...
echo =====================================================
mysql -u root -h localhost isdn_db < 02_add_test_gps_data.sql
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Failed to add test GPS data!
    pause
    exit /b 1
)
echo SUCCESS: Test GPS data added!
echo.

echo =====================================================
echo STEP 3: Verifying database changes...
echo =====================================================
mysql -u root -h localhost isdn_db -e "SELECT delivery_id, status, current_latitude, current_longitude, destination_latitude, destination_longitude FROM deliveries WHERE delivery_id <= 5;"
echo.

echo =====================================================
echo Setup Complete!
echo =====================================================
echo.
echo Next steps:
echo   1. Start your Spring Boot backend (if not running)
echo   2. Run: 03_test_api.bat to test the API response
echo   3. Open your React frontend map component
echo   4. You should see delivery markers on the map!
echo.
echo GPS Coordinates Added:
echo   - Delivery #1: Colombo (6.9271, 79.8612)
echo   - Delivery #2: Galle (6.0535, 80.2210)
echo   - Delivery #3: Kandy (7.2906, 80.6337)
echo   - Delivery #4: Jaffna (9.6615, 80.0255)
echo   - Delivery #5: Negombo (7.2088, 79.8358)
echo.
pause
