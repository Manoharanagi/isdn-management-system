@echo off
REM =====================================================
REM STEP 3: Test API Response for GPS Coordinates
REM =====================================================
REM This script tests if the backend is returning GPS data
REM =====================================================

echo =====================================================
echo Testing Delivery API for GPS Coordinates
echo =====================================================
echo.

echo Testing: GET /api/deliveries/1
echo.
curl -X GET http://localhost:8080/api/deliveries/1 -H "Content-Type: application/json"
echo.
echo.

echo =====================================================
echo Expected Response Fields:
echo - currentLatitude: 6.9500
echo - currentLongitude: 79.8800
echo - destinationLatitude: 6.9271
echo - destinationLongitude: 79.8612
echo =====================================================
echo.

pause
