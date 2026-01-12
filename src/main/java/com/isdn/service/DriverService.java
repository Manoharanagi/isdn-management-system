package com.isdn.service;

import com.isdn.dto.request.CreateDriverRequest;
import com.isdn.dto.request.UpdateDriverRequest;
import com.isdn.dto.request.UpdateLocationRequest;
import com.isdn.dto.response.DriverResponse;
import com.isdn.exception.BadRequestException;
import com.isdn.exception.ResourceNotFoundException;
import com.isdn.model.*;
import com.isdn.repository.DeliveryRepository;
import com.isdn.repository.DriverRepository;
import com.isdn.repository.RDCRepository;
import com.isdn.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DriverService {

    private final DriverRepository driverRepository;
    private final DeliveryRepository deliveryRepository;
    private final UserRepository userRepository;
    private final RDCRepository rdcRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Get all active drivers
     */
    @Transactional(readOnly = true)
    public List<DriverResponse> getAllDrivers() {
        log.info("Fetching all active drivers");
        List<Driver> drivers = driverRepository.findByActiveTrue();
        return drivers.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get available drivers by RDC
     */
    @Transactional(readOnly = true)
    public List<DriverResponse> getAvailableDrivers(Long rdcId) {
        log.info("Fetching available drivers for RDC: {}", rdcId);
        List<Driver> drivers = driverRepository.findAvailableDriversByRdc(rdcId, DriverStatus.AVAILABLE);
        return drivers.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get driver by ID
     */
    @Transactional(readOnly = true)
    public DriverResponse getDriverById(Long driverId) {
        log.info("Fetching driver: {}", driverId);
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));
        return mapToResponse(driver);
    }

    /**
     * Update driver location
     */
    @Transactional
    public DriverResponse updateLocation(Long driverId, UpdateLocationRequest request) {
        log.info("Updating location for driver: {}", driverId);

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));

        driver.setCurrentLatitude(request.getLatitude());
        driver.setCurrentLongitude(request.getLongitude());
        driver.setLastLocationUpdate(LocalDateTime.now());

        driverRepository.save(driver);

        // Also update current delivery location if driver is on delivery
        List<Delivery> activeDeliveries = deliveryRepository.findByDriverIdAndStatus(
                driverId, DeliveryStatus.IN_TRANSIT);

        for (Delivery delivery : activeDeliveries) {
            delivery.setCurrentLatitude(request.getLatitude());
            delivery.setCurrentLongitude(request.getLongitude());
            deliveryRepository.save(delivery);
        }

        return mapToResponse(driver);
    }

    /**
     * Update driver status
     */
    @Transactional
    public DriverResponse updateStatus(Long driverId, DriverStatus status) {
        log.info("Updating status for driver: {} to {}", driverId, status);

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));

        driver.setStatus(status);
        driverRepository.save(driver);

        return mapToResponse(driver);
    }

    /**
     * Create new driver - Feature 5
     */
    @Transactional
    public DriverResponse createDriver(CreateDriverRequest request) {
        log.info("Creating new driver: {}", request.getUsername());

        // Check if username already exists
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new BadRequestException("Username already exists");
        }

        // Check if email already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BadRequestException("Email already exists");
        }

        // Check if license number already exists
        if (driverRepository.findByLicenseNumber(request.getLicenseNumber()).isPresent()) {
            throw new BadRequestException("License number already exists");
        }

        // Get RDC
        RDC rdc = rdcRepository.findById(request.getRdcId())
                .orElseThrow(() -> new ResourceNotFoundException("RDC not found"));

        // Create user account for driver
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .contactPerson(request.getFullName())
                .role(Role.DRIVER)
                .active(true)
                .build();

        userRepository.save(user);

        // Create driver record
        Driver driver = Driver.builder()
                .user(user)
                .rdc(rdc)
                .licenseNumber(request.getLicenseNumber())
                .vehicleNumber(request.getVehicleNumber())
                .vehicleType(request.getVehicleType())
                .status(DriverStatus.AVAILABLE)
                .active(true)
                .build();

        driverRepository.save(driver);

        log.info("Driver created successfully: {}", driver.getDriverId());
        return mapToResponse(driver);
    }

    /**
     * Update driver information - Feature 5
     */
    @Transactional
    public DriverResponse updateDriver(Long driverId, UpdateDriverRequest request) {
        log.info("Updating driver: {}", driverId);

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));

        // Update phone number
        if (request.getPhoneNumber() != null) {
            driver.getUser().setPhoneNumber(request.getPhoneNumber());
        }

        // Update license number
        if (request.getLicenseNumber() != null) {
            // Check if new license number is already taken
            Optional<Driver> existingDriver = driverRepository.findByLicenseNumber(request.getLicenseNumber());
            if (existingDriver.isPresent() && !existingDriver.get().getDriverId().equals(driverId)) {
                throw new BadRequestException("License number already exists");
            }
            driver.setLicenseNumber(request.getLicenseNumber());
        }

        // Update vehicle details
        if (request.getVehicleNumber() != null) {
            driver.setVehicleNumber(request.getVehicleNumber());
        }

        if (request.getVehicleType() != null) {
            driver.setVehicleType(request.getVehicleType());
        }

        // Update status
        if (request.getStatus() != null) {
            driver.setStatus(request.getStatus());
        }

        // Update RDC assignment
        if (request.getRdcId() != null) {
            RDC rdc = rdcRepository.findById(request.getRdcId())
                    .orElseThrow(() -> new ResourceNotFoundException("RDC not found"));
            driver.setRdc(rdc);
        }

        // Update active status
        if (request.getActive() != null) {
            driver.setActive(request.getActive());
            driver.getUser().setActive(request.getActive());
        }

        driverRepository.save(driver);
        userRepository.save(driver.getUser());

        log.info("Driver updated successfully");
        return mapToResponse(driver);
    }

    /**
     * Delete driver (soft delete) - Feature 5
     */
    @Transactional
    public void deleteDriver(Long driverId) {
        log.info("Deleting driver: {}", driverId);

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));

        // Check if driver has active deliveries
        Long activeDeliveries = deliveryRepository.countByDriverIdAndStatus(
                driverId, DeliveryStatus.IN_TRANSIT);

        if (activeDeliveries > 0) {
            throw new BadRequestException("Cannot delete driver with active deliveries");
        }

        // Soft delete
        driver.setActive(false);
        driver.getUser().setActive(false);

        driverRepository.save(driver);
        userRepository.save(driver.getUser());

        log.info("Driver deleted successfully");
    }

    /**
     * Get drivers by RDC - Feature 5
     */
    @Transactional(readOnly = true)
    public List<DriverResponse> getDriversByRdc(Long rdcId) {
        log.info("Fetching drivers for RDC: {}", rdcId);

        List<Driver> drivers = driverRepository.findByRdc_RdcId(rdcId);

        return drivers.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get current driver's profile (for /me endpoint)
     */
    @Transactional(readOnly = true)
    public DriverResponse getCurrentDriver(Long userId) {
        log.info("Fetching current driver profile for user: {}", userId);

        Driver driver = driverRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver profile not found for this user"));

        return mapToResponse(driver);
    }

    /**
     * Map Driver to DriverResponse
     */
    private DriverResponse mapToResponse(Driver driver) {
        // Count active deliveries
        Long activeDeliveries = deliveryRepository.countByDriverIdAndStatus(
                driver.getDriverId(), DeliveryStatus.IN_TRANSIT);

        return DriverResponse.builder()
                .driverId(driver.getDriverId())
                .name(driver.getUser().getContactPerson() != null ?
                      driver.getUser().getContactPerson() : driver.getUser().getUsername())
                .username(driver.getUser().getUsername())
                .phoneNumber(driver.getUser().getPhoneNumber())
                .licenseNumber(driver.getLicenseNumber())
                .vehicleNumber(driver.getVehicleNumber())
                .vehicleType(driver.getVehicleType())
                .status(driver.getStatus())
                .rdcName(driver.getRdc().getName())
                .currentLatitude(driver.getCurrentLatitude())
                .currentLongitude(driver.getCurrentLongitude())
                .lastLocationUpdate(driver.getLastLocationUpdate())
                .activeDeliveries(activeDeliveries.intValue())
                .build();
    }
}
