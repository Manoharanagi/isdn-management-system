package com.isdn.controller;

import com.isdn.dto.request.CreateDriverRequest;
import com.isdn.dto.request.UpdateDriverRequest;
import com.isdn.dto.request.UpdateLocationRequest;
import com.isdn.dto.response.ApiResponse;
import com.isdn.dto.response.DeliveryResponse;
import com.isdn.dto.response.DriverResponse;
import com.isdn.model.DriverStatus;
import com.isdn.model.User;
import com.isdn.service.DeliveryService;
import com.isdn.service.DriverService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drivers")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class DriverController {

    private final DriverService driverService;
    private final DeliveryService deliveryService;
    private final com.isdn.repository.UserRepository userRepository;

    /**
     * GET /api/drivers - Get all drivers
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('LOGISTICS_OFFICER', 'RDC_STAFF', 'HO_MANAGER', 'ADMIN')")
    public ResponseEntity<List<DriverResponse>> getAllDrivers() {
        log.info("GET /api/drivers - Fetch all drivers");
        List<DriverResponse> drivers = driverService.getAllDrivers();
        return ResponseEntity.ok(drivers);
    }

    /**
     * GET /api/drivers/me - Get current driver's profile (Driver Dashboard)
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<DriverResponse> getCurrentDriver(
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("GET /api/drivers/me - Fetch current driver profile");
        Long userId = getUserId(userDetails);
        DriverResponse driver = driverService.getCurrentDriver(userId);
        return ResponseEntity.ok(driver);
    }

    /**
     * GET /api/drivers/me/deliveries - Get current driver's deliveries
     */
    @GetMapping("/me/deliveries")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<List<DeliveryResponse>> getCurrentDriverDeliveries(
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("GET /api/drivers/me/deliveries - Fetch current driver's deliveries");
        Long userId = getUserId(userDetails);
        DriverResponse driver = driverService.getCurrentDriver(userId);
        List<DeliveryResponse> deliveries = deliveryService.getDeliveriesByDriver(driver.getDriverId());
        return ResponseEntity.ok(deliveries);
    }

    /**
     * GET /api/drivers/available/{rdcId} - Get available drivers
     */
    @GetMapping("/available/{rdcId}")
    @PreAuthorize("hasAnyRole('LOGISTICS_OFFICER', 'RDC_STAFF', 'HO_MANAGER', 'ADMIN')")
    public ResponseEntity<List<DriverResponse>> getAvailableDrivers(@PathVariable Long rdcId) {
        log.info("GET /api/drivers/available/{} - Fetch available drivers", rdcId);
        List<DriverResponse> drivers = driverService.getAvailableDrivers(rdcId);
        return ResponseEntity.ok(drivers);
    }

    /**
     * GET /api/drivers/{driverId} - Get driver by ID
     */
    @GetMapping("/{driverId}")
    @PreAuthorize("hasAnyRole('DRIVER', 'LOGISTICS_OFFICER', 'RDC_STAFF', 'HO_MANAGER', 'ADMIN')")
    public ResponseEntity<DriverResponse> getDriverById(@PathVariable Long driverId) {
        log.info("GET /api/drivers/{} - Fetch driver", driverId);
        DriverResponse driver = driverService.getDriverById(driverId);
        return ResponseEntity.ok(driver);
    }

    /**
     * PUT /api/drivers/{driverId}/location - Update driver location
     */
    @PutMapping("/{driverId}/location")
    @PreAuthorize("hasAnyRole('DRIVER', 'ADMIN')")
    public ResponseEntity<DriverResponse> updateLocation(
            @PathVariable Long driverId,
            @Valid @RequestBody UpdateLocationRequest request) {
        log.info("PUT /api/drivers/{}/location - Update location", driverId);
        DriverResponse driver = driverService.updateLocation(driverId, request);
        return ResponseEntity.ok(driver);
    }

    /**
     * PUT /api/drivers/{driverId}/status - Update driver status
     */
    @PutMapping("/{driverId}/status")
    @PreAuthorize("hasAnyRole('DRIVER', 'LOGISTICS_OFFICER', 'ADMIN')")
    public ResponseEntity<DriverResponse> updateStatus(
            @PathVariable Long driverId,
            @RequestParam DriverStatus status) {
        log.info("PUT /api/drivers/{}/status - Update status to {}", driverId, status);
        DriverResponse driver = driverService.updateStatus(driverId, status);
        return ResponseEntity.ok(driver);
    }

    /**
     * POST /api/drivers - Create new driver - Feature 5
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('HO_MANAGER', 'ADMIN')")
    public ResponseEntity<DriverResponse> createDriver(
            @Valid @RequestBody CreateDriverRequest request) {
        log.info("POST /api/drivers - Create driver");
        DriverResponse driver = driverService.createDriver(request);
        return new ResponseEntity<>(driver, HttpStatus.CREATED);
    }

    /**
     * PUT /api/drivers/{driverId} - Update driver - Feature 5
     */
    @PutMapping("/{driverId}")
    @PreAuthorize("hasAnyRole('HO_MANAGER', 'ADMIN')")
    public ResponseEntity<DriverResponse> updateDriver(
            @PathVariable Long driverId,
            @Valid @RequestBody UpdateDriverRequest request) {
        log.info("PUT /api/drivers/{} - Update driver", driverId);
        DriverResponse driver = driverService.updateDriver(driverId, request);
        return ResponseEntity.ok(driver);
    }

    /**
     * DELETE /api/drivers/{driverId} - Delete driver (soft delete) - Feature 5
     */
    @DeleteMapping("/{driverId}")
    @PreAuthorize("hasAnyRole('HO_MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse> deleteDriver(@PathVariable Long driverId) {
        log.info("DELETE /api/drivers/{} - Delete driver", driverId);
        driverService.deleteDriver(driverId);

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Driver deleted successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/drivers/rdc/{rdcId} - Get drivers by RDC - Feature 5
     */
    @GetMapping("/rdc/{rdcId}")
    @PreAuthorize("hasAnyRole('LOGISTICS_OFFICER', 'RDC_STAFF', 'HO_MANAGER', 'ADMIN')")
    public ResponseEntity<List<DriverResponse>> getDriversByRdc(@PathVariable Long rdcId) {
        log.info("GET /api/drivers/rdc/{} - Fetch drivers for RDC", rdcId);
        List<DriverResponse> drivers = driverService.getDriversByRdc(rdcId);
        return ResponseEntity.ok(drivers);
    }

    /**
     * Helper method to get user ID from UserDetails
     */
    private Long getUserId(UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getUserId();
    }
}
