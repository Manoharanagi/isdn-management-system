# FEATURE 3: DELIVERY SCHEDULING & GPS TRACKING

---

## FEATURE OVERVIEW

### What We'll Build:
1. ✅ Driver management system
2. ✅ Assign orders to drivers
3. ✅ Delivery route planning
4. ✅ GPS location tracking (simulated)
5. ✅ Real-time delivery status updates
6. ✅ Delivery history and analytics
7. ✅ Customer delivery notifications

---

## BACKEND IMPLEMENTATION

### 1. Update Delivery Entity

**File: `src/main/java/com/isdn/model/Delivery.java`**
```java
package com.isdn.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "deliveries")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Delivery {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_id")
    private Long deliveryId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private Driver driver;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus status = DeliveryStatus.PENDING_ASSIGNMENT;
    
    @Column(name = "assigned_date")
    private LocalDateTime assignedDate;
    
    @Column(name = "pickup_time")
    private LocalDateTime pickupTime;
    
    @Column(name = "delivery_time")
    private LocalDateTime deliveryTime;
    
    @Column(name = "current_latitude", precision = 10, scale = 7)
    private BigDecimal currentLatitude;
    
    @Column(name = "current_longitude", precision = 10, scale = 7)
    private BigDecimal currentLongitude;
    
    @Column(name = "destination_latitude", precision = 10, scale = 7)
    private BigDecimal destinationLatitude;
    
    @Column(name = "destination_longitude", precision = 10, scale = 7)
    private BigDecimal destinationLongitude;
    
    @Column(name = "estimated_distance_km", precision = 10, scale = 2)
    private BigDecimal estimatedDistanceKm;
    
    @Column(name = "actual_distance_km", precision = 10, scale = 2)
    private BigDecimal actualDistanceKm;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "delivery_proof_url")
    private String deliveryProofUrl;
    
    @Column(name = "customer_signature_url")
    private String customerSignatureUrl;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

**File: `src/main/java/com/isdn/model/DeliveryStatus.java`**
```java
package com.isdn.model;

public enum DeliveryStatus {
    PENDING_ASSIGNMENT("Pending Assignment"),
    ASSIGNED("Assigned to Driver"),
    PICKED_UP("Picked Up from RDC"),
    IN_TRANSIT("In Transit"),
    ARRIVED("Arrived at Destination"),
    DELIVERED("Delivered"),
    FAILED("Failed Delivery"),
    RETURNED("Returned to RDC");
    
    private final String displayName;
    
    DeliveryStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
```

### 2. Update Driver Entity

**File: `src/main/java/com/isdn/model/Driver.java`**
```java
package com.isdn.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "drivers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Driver {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "driver_id")
    private Long driverId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rdc_id", nullable = false)
    private RDC rdc;
    
    @NotBlank(message = "License number is required")
    @Column(name = "license_number", nullable = false, unique = true)
    private String licenseNumber;
    
    @NotBlank(message = "Vehicle number is required")
    @Column(name = "vehicle_number", nullable = false)
    private String vehicleNumber;
    
    @Column(name = "vehicle_type")
    private String vehicleType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DriverStatus status = DriverStatus.AVAILABLE;
    
    @Column(name = "current_latitude", precision = 10, scale = 7)
    private BigDecimal currentLatitude;
    
    @Column(name = "current_longitude", precision = 10, scale = 7)
    private BigDecimal currentLongitude;
    
    @Column(name = "last_location_update")
    private LocalDateTime lastLocationUpdate;
    
    @Column(nullable = false)
    private Boolean active = true;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

**File: `src/main/java/com/isdn/model/DriverStatus.java`**
```java
package com.isdn.model;

public enum DriverStatus {
    AVAILABLE("Available"),
    ON_DELIVERY("On Delivery"),
    OFF_DUTY("Off Duty"),
    ON_BREAK("On Break");
    
    private final String displayName;
    
    DriverStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
```

---

### 3. Repositories

**File: `src/main/java/com/isdn/repository/DeliveryRepository.java`**
```java
package com.isdn.repository;

import com.isdn.model.Delivery;
import com.isdn.model.DeliveryStatus;
import com.isdn.model.Driver;
import com.isdn.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    
    Optional<Delivery> findByOrder(Order order);
    
    Optional<Delivery> findByOrder_OrderId(Long orderId);
    
    List<Delivery> findByDriverOrderByCreatedAtDesc(Driver driver);
    
    List<Delivery> findByDriver_DriverIdOrderByCreatedAtDesc(Long driverId);
    
    List<Delivery> findByStatusOrderByCreatedAtDesc(DeliveryStatus status);
    
    @Query("SELECT d FROM Delivery d WHERE d.driver.driverId = :driverId AND d.status = :status")
    List<Delivery> findByDriverIdAndStatus(@Param("driverId") Long driverId, 
                                           @Param("status") DeliveryStatus status);
    
    @Query("SELECT d FROM Delivery d WHERE d.order.rdc.rdcId = :rdcId ORDER BY d.createdAt DESC")
    List<Delivery> findByRdcIdOrderByCreatedAtDesc(@Param("rdcId") Long rdcId);
    
    @Query("SELECT d FROM Delivery d WHERE d.status IN :statuses ORDER BY d.createdAt DESC")
    List<Delivery> findByStatusIn(@Param("statuses") List<DeliveryStatus> statuses);
    
    @Query("SELECT COUNT(d) FROM Delivery d WHERE d.driver.driverId = :driverId AND d.status = :status")
    Long countByDriverIdAndStatus(@Param("driverId") Long driverId, 
                                   @Param("status") DeliveryStatus status);
}
```

**File: `src/main/java/com/isdn/repository/DriverRepository.java`**
```java
package com.isdn.repository;

import com.isdn.model.Driver;
import com.isdn.model.DriverStatus;
import com.isdn.model.RDC;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {
    
    Optional<Driver> findByLicenseNumber(String licenseNumber);
    
    List<Driver> findByRdc(RDC rdc);
    
    List<Driver> findByRdc_RdcId(Long rdcId);
    
    List<Driver> findByStatus(DriverStatus status);
    
    List<Driver> findByActiveTrue();
    
    @Query("SELECT d FROM Driver d WHERE d.rdc.rdcId = :rdcId AND d.status = :status AND d.active = true")
    List<Driver> findAvailableDriversByRdc(@Param("rdcId") Long rdcId, 
                                           @Param("status") DriverStatus status);
    
    @Query("SELECT d FROM Driver d WHERE d.user.userId = :userId")
    Optional<Driver> findByUserId(@Param("userId") Long userId);
}
```

---

### 4. DTOs

**File: `src/main/java/com/isdn/dto/request/AssignDeliveryRequest.java`**
```java
package com.isdn.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignDeliveryRequest {
    
    @NotNull(message = "Order ID is required")
    private Long orderId;
    
    @NotNull(message = "Driver ID is required")
    private Long driverId;
    
    private String notes;
}
```

**File: `src/main/java/com/isdn/dto/request/UpdateLocationRequest.java`**
```java
package com.isdn.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLocationRequest {
    
    @NotNull(message = "Latitude is required")
    private BigDecimal latitude;
    
    @NotNull(message = "Longitude is required")
    private BigDecimal longitude;
}
```

**File: `src/main/java/com/isdn/dto/request/UpdateDeliveryStatusRequest.java`**
```java
package com.isdn.dto.request;

import com.isdn.model.DeliveryStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDeliveryStatusRequest {
    
    @NotNull(message = "Status is required")
    private DeliveryStatus status;
    
    private String notes;
}
```

**File: `src/main/java/com/isdn/dto/response/DeliveryResponse.java`**
```java
package com.isdn.dto.response;

import com.isdn.model.DeliveryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryResponse {
    private Long deliveryId;
    private Long orderId;
    private String orderNumber;
    private Long driverId;
    private String driverName;
    private String vehicleNumber;
    private DeliveryStatus status;
    private String deliveryAddress;
    private String contactNumber;
    private LocalDateTime assignedDate;
    private LocalDateTime pickupTime;
    private LocalDateTime deliveryTime;
    private BigDecimal currentLatitude;
    private BigDecimal currentLongitude;
    private BigDecimal destinationLatitude;
    private BigDecimal destinationLongitude;
    private BigDecimal estimatedDistanceKm;
    private String notes;
}
```

**File: `src/main/java/com/isdn/dto/response/DriverResponse.java`**
```java
package com.isdn.dto.response;

import com.isdn.model.DriverStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverResponse {
    private Long driverId;
    private String name;
    private String username;
    private String phoneNumber;
    private String licenseNumber;
    private String vehicleNumber;
    private String vehicleType;
    private DriverStatus status;
    private String rdcName;
    private BigDecimal currentLatitude;
    private BigDecimal currentLongitude;
    private LocalDateTime lastLocationUpdate;
    private Integer activeDeliveries;
}
```

---

### 5. Services

**File: `src/main/java/com/isdn/service/DriverService.java`**
```java
package com.isdn.service;

import com.isdn.dto.request.UpdateLocationRequest;
import com.isdn.dto.response.DriverResponse;
import com.isdn.exception.ResourceNotFoundException;
import com.isdn.model.Delivery;
import com.isdn.model.DeliveryStatus;
import com.isdn.model.Driver;
import com.isdn.model.DriverStatus;
import com.isdn.repository.DeliveryRepository;
import com.isdn.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DriverService {
    
    private final DriverRepository driverRepository;
    private final DeliveryRepository deliveryRepository;
    
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
     * Map Driver to DriverResponse
     */
    private DriverResponse mapToResponse(Driver driver) {
        // Count active deliveries
        Long activeDeliveries = deliveryRepository.countByDriverIdAndStatus(
                driver.getDriverId(), DeliveryStatus.IN_TRANSIT);
        
        return DriverResponse.builder()
                .driverId(driver.getDriverId())
                .name(driver.getUser().getUsername())
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
```

**File: `src/main/java/com/isdn/service/DeliveryService.java`**
```java
package com.isdn.service;

import com.isdn.dto.request.AssignDeliveryRequest;
import com.isdn.dto.request.UpdateDeliveryStatusRequest;
import com.isdn.dto.response.DeliveryResponse;
import com.isdn.exception.BadRequestException;
import com.isdn.exception.ResourceNotFoundException;
import com.isdn.model.*;
import com.isdn.repository.DeliveryRepository;
import com.isdn.repository.DriverRepository;
import com.isdn.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryService {
    
    private final DeliveryRepository deliveryRepository;
    private final OrderRepository orderRepository;
    private final DriverRepository driverRepository;
    
    /**
     * Assign delivery to driver
     */
    @Transactional
    public DeliveryResponse assignDelivery(AssignDeliveryRequest request) {
        log.info("Assigning delivery for order: {} to driver: {}", 
                 request.getOrderId(), request.getDriverId());
        
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        
        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw new BadRequestException("Order must be CONFIRMED before assignment");
        }
        
        Driver driver = driverRepository.findById(request.getDriverId())
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));
        
        if (driver.getStatus() != DriverStatus.AVAILABLE) {
            throw new BadRequestException("Driver is not available");
        }
        
        // Check if delivery already exists
        Delivery delivery = deliveryRepository.findByOrder(order)
                .orElseGet(() -> Delivery.builder()
                        .order(order)
                        .status(DeliveryStatus.PENDING_ASSIGNMENT)
                        .build());
        
        // Set destination coordinates (simplified - in real app, use geocoding)
        delivery.setDestinationLatitude(new BigDecimal("6.9271")); // Colombo
        delivery.setDestinationLongitude(new BigDecimal("79.8612"));
        delivery.setEstimatedDistanceKm(new BigDecimal("15.5"));
        
        // Assign driver
        delivery.setDriver(driver);
        delivery.setStatus(DeliveryStatus.ASSIGNED);
        delivery.setAssignedDate(LocalDateTime.now());
        delivery.setNotes(request.getNotes());
        
        deliveryRepository.save(delivery);
        
        // Update driver status
        driver.setStatus(DriverStatus.ON_DELIVERY);
        driverRepository.save(driver);
        
        // Update order status
        order.setStatus(OrderStatus.READY_FOR_DELIVERY);
        orderRepository.save(order);
        
        log.info("Delivery assigned successfully");
        return mapToResponse(delivery);
    }
    
    /**
     * Update delivery status
     */
    @Transactional
    public DeliveryResponse updateDeliveryStatus(Long deliveryId, UpdateDeliveryStatusRequest request) {
        log.info("Updating delivery status: {} to {}", deliveryId, request.getStatus());
        
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found"));
        
        DeliveryStatus oldStatus = delivery.getStatus();
        delivery.setStatus(request.getStatus());
        
        // Update timestamps based on status
        switch (request.getStatus()) {
            case PICKED_UP -> {
                delivery.setPickupTime(LocalDateTime.now());
                delivery.getOrder().setStatus(OrderStatus.OUT_FOR_DELIVERY);
            }
            case IN_TRANSIT -> {
                delivery.getOrder().setStatus(OrderStatus.OUT_FOR_DELIVERY);
            }
            case DELIVERED -> {
                delivery.setDeliveryTime(LocalDateTime.now());
                delivery.getOrder().setStatus(OrderStatus.DELIVERED);
                delivery.getOrder().setActualDeliveryDate(LocalDateTime.now().toLocalDate());
                
                // Free up driver
                if (delivery.getDriver() != null) {
                    delivery.getDriver().setStatus(DriverStatus.AVAILABLE);
                    driverRepository.save(delivery.getDriver());
                }
            }
            case FAILED -> {
                delivery.getOrder().setStatus(OrderStatus.FAILED_DELIVERY);
                
                // Free up driver
                if (delivery.getDriver() != null) {
                    delivery.getDriver().setStatus(DriverStatus.AVAILABLE);
                    driverRepository.save(delivery.getDriver());
                }
            }
        }
        
        if (request.getNotes() != null) {
            delivery.setNotes(request.getNotes());
        }
        
        deliveryRepository.save(delivery);
        orderRepository.save(delivery.getOrder());
        
        log.info("Delivery status updated from {} to {}", oldStatus, request.getStatus());
        return mapToResponse(delivery);
    }
    
    /**
     * Get all deliveries for an RDC
     */
    @Transactional(readOnly = true)
    public List<DeliveryResponse> getDeliveriesByRdc(Long rdcId) {
        log.info("Fetching deliveries for RDC: {}", rdcId);
        List<Delivery> deliveries = deliveryRepository.findByRdcIdOrderByCreatedAtDesc(rdcId);
        return deliveries.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get deliveries by driver
     */
    @Transactional(readOnly = true)
    public List<DeliveryResponse> getDeliveriesByDriver(Long driverId) {
        log.info("Fetching deliveries for driver: {}", driverId);
        List<Delivery> deliveries = deliveryRepository.findByDriver_DriverIdOrderByCreatedAtDesc(driverId);
        return deliveries.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get active deliveries (in transit)
     */
    @Transactional(readOnly = true)
    public List<DeliveryResponse> getActiveDeliveries() {
        log.info("Fetching active deliveries");
        List<DeliveryStatus> activeStatuses = List.of(
                DeliveryStatus.ASSIGNED,
                DeliveryStatus.PICKED_UP,
                DeliveryStatus.IN_TRANSIT,
                DeliveryStatus.ARRIVED
        );
        List<Delivery> deliveries = deliveryRepository.findByStatusIn(activeStatuses);
        return deliveries.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get delivery by ID
     */
    @Transactional(readOnly = true)
    public DeliveryResponse getDeliveryById(Long deliveryId) {
        log.info("Fetching delivery: {}", deliveryId);
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found"));
        return mapToResponse(delivery);
    }
    
    /**
     * Get delivery by order ID
     */
    @Transactional(readOnly = true)
    public DeliveryResponse getDeliveryByOrderId(Long orderId) {
        log.info("Fetching delivery for order: {}", orderId);
        Delivery delivery = deliveryRepository.findByOrder_OrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found for this order"));
        return mapToResponse(delivery);
    }
    
    /**
     * Map Delivery to DeliveryResponse
     */
    private DeliveryResponse mapToResponse(Delivery delivery) {
        return DeliveryResponse.builder()
                .deliveryId(delivery.getDeliveryId())
                .orderId(delivery.getOrder().getOrderId())
                .orderNumber(delivery.getOrder().getOrderNumber())
                .driverId(delivery.getDriver() != null ? delivery.getDriver().getDriverId() : null)
                .driverName(delivery.getDriver() != null ? delivery.getDriver().getUser().getUsername() : null)
                .vehicleNumber(delivery.getDriver() != null ? delivery.getDriver().getVehicleNumber() : null)
                .status(delivery.getStatus())
                .deliveryAddress(delivery.getOrder().getDeliveryAddress())
                .contactNumber(delivery.getOrder().getContactNumber())
                .assignedDate(delivery.getAssignedDate())
                .pickupTime(delivery.getPickupTime())
                .deliveryTime(delivery.getDeliveryTime())
                .currentLatitude(delivery.getCurrentLatitude())
                .currentLongitude(delivery.getCurrentLongitude())
                .destinationLatitude(delivery.getDestinationLatitude())
                .destinationLongitude(delivery.getDestinationLongitude())
                .estimatedDistanceKm(delivery.getEstimatedDistanceKm())
                .notes(delivery.getNotes())
                .build();
    }
}
```

---

### 6. Controllers

**File: `src/main/java/com/isdn/controller/DriverController.java`**
```java
package com.isdn.controller;

import com.isdn.dto.request.UpdateLocationRequest;
import com.isdn.dto.response.DriverResponse;
import com.isdn.model.DriverStatus;
import com.isdn.service.DriverService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drivers")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class DriverController {
    
    private final DriverService driverService;
    
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
}
```

**File: `src/main/java/com/isdn/controller/DeliveryController.java`**
```java
package com.isdn.controller;

import com.isdn.dto.request.AssignDeliveryRequest;
import com.isdn.dto.request.UpdateDeliveryStatusRequest;
import com.isdn.dto.response.DeliveryResponse;
import com.isdn.service.DeliveryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/deliveries")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class DeliveryController {
    
    private final DeliveryService deliveryService;
    
    /**
     * POST /api/deliveries/assign - Assign delivery to driver
     */
    @PostMapping("/assign")
    @PreAuthorize("hasAnyRole('LOGISTICS_OFFICER', 'RDC_STAFF', 'HO_MANAGER', 'ADMIN')")
    public ResponseEntity<DeliveryResponse> assignDelivery(
            @Valid @RequestBody AssignDeliveryRequest request) {
        log.info("POST /api/deliveries/assign - Assign delivery");
        DeliveryResponse delivery = deliveryService.assignDelivery(request);
        return new ResponseEntity<>(delivery, HttpStatus.CREATED);
    }
    
    /**
     * PUT /api/deliveries/{deliveryId}/status - Update delivery status
     */
    @PutMapping("/{deliveryId}/status")
    @PreAuthorize("hasAnyRole('DRIVER', 'LOGISTICS_OFFICER', 'RDC_STAFF', 'ADMIN')")
    public ResponseEntity<DeliveryResponse> updateDeliveryStatus(
            @PathVariable Long deliveryId,
            @Valid @RequestBody UpdateDeliveryStatusRequest request) {
        log.info("PUT /api/deliveries/{}/status - Update status", deliveryId);
        DeliveryResponse delivery = deliveryService.updateDeliveryStatus(deliveryId, request);
        return ResponseEntity.ok(delivery);
    }
    
    /**
     * GET /api/deliveries/rdc/{rdcId} - Get deliveries by RDC
     */
    @GetMapping("/rdc/{rdcId}")
    @PreAuthorize("hasAnyRole('LOGISTICS_OFFICER', 'RDC_STAFF', 'HO_MANAGER', 'ADMIN')")
    public ResponseEntity<List<DeliveryResponse>> getDeliveriesByRdc(@PathVariable Long rdcId) {
        log.info("GET /api/deliveries/rdc/{} - Fetch deliveries", rdcId);
        List<DeliveryResponse> deliveries = deliveryService.getDeliveriesByRdc(rdcId);
        return ResponseEntity.ok(deliveries);
    }
    
    /**
     * GET /api/deliveries/driver/{driverId} - Get deliveries by driver
     */
    @GetMapping("/driver/{driverId}")
    @PreAuthorize("hasAnyRole('DRIVER', 'LOGISTICS_OFFICER', 'ADMIN')")
    public ResponseEntity<List<DeliveryResponse>> getDeliveriesByDriver(@PathVariable Long driverId) {
        log.info("GET /api/deliveries/driver/{} - Fetch deliveries", driverId);
        List<DeliveryResponse> deliveries = deliveryService.getDeliveriesByDriver(driverId);
        return ResponseEntity.ok(deliveries);
    }
    
    /**
     * GET /api/deliveries/active - Get active deliveries
     */
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('LOGISTICS_OFFICER', 'RDC_STAFF', 'HO_MANAGER', 'ADMIN')")
    public ResponseEntity<List<DeliveryResponse>> getActiveDeliveries() {
        log.info("GET /api/deliveries/active - Fetch active deliveries");
        List<DeliveryResponse> deliveries = deliveryService.getActiveDeliveries();
        return ResponseEntity.ok(deliveries);
    }
    
    /**
     * GET /api/deliveries/{deliveryId} - Get delivery by ID
     */
    @GetMapping("/{deliveryId}")
    @PreAuthorize("hasAnyRole('DRIVER', 'LOGISTICS_OFFICER', 'RDC_STAFF', 'CUSTOMER', 'ADMIN')")
    public ResponseEntity<DeliveryResponse> getDeliveryById(@PathVariable Long deliveryId) {
        log.info("GET /api/deliveries/{} - Fetch delivery", deliveryId);
        DeliveryResponse delivery = deliveryService.getDeliveryById(deliveryId);
        return ResponseEntity.ok(delivery);
    }
    
    /**
     * GET /api/deliveries/order/{orderId} - Get delivery by order ID
     */
    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'DRIVER', 'LOGISTICS_OFFICER', 'RDC_STAFF', 'ADMIN')")
    public ResponseEntity<DeliveryResponse> getDeliveryByOrderId(@PathVariable Long orderId) {
        log.info("GET /api/deliveries/order/{} - Fetch delivery for order", orderId);
        DeliveryResponse delivery = deliveryService.getDeliveryByOrderId(orderId);
        return ResponseEntity.ok(delivery);
    }
}
```

---

## FRONTEND IMPLEMENTATION

### 1. Delivery Service

**File: `src/services/deliveryService.js`**
```javascript
import api from './api';

/**
 * Assign delivery to driver
 */
export const assignDelivery = async (assignData) => {
    const response = await api.post('/deliveries/assign', assignData);
    return response.data;
};

/**
 * Update delivery status
 */
export const updateDeliveryStatus = async (deliveryId, statusData) => {
    const response = await api.put(`/deliveries/${deliveryId}/status`, statusData);
    return response.data;
};

/**
 * Get deliveries by RDC
 */
export const getDeliveriesByRdc = async (rdcId) => {
    const response = await api.get(`/deliveries/rdc/${rdcId}`);
    return response.data;
};

/**
 * Get deliveries by driver
 */
export const getDeliveriesByDriver = async (driverId) => {
    const response = await api.get(`/deliveries/driver/${driverId}`);
    return response.data;
};

/**
 * Get active deliveries
 */
export const getActiveDeliveries = async () => {
    const response = await api.get('/deliveries/active');
    return response.data;
};

/**
 * Get delivery by ID
 */
export const getDeliveryById = async (deliveryId) => {
    const response = await api.get(`/deliveries/${deliveryId}`);
    return response.data;
};

/**
 * Get delivery by order ID
 */
export const getDeliveryByOrderId = async (orderId) => {
    const response = await api.get(`/deliveries/order/${orderId}`);
    return response.data;
};
```

**File: `src/services/driverService.js`**
```javascript
import api from './api';

/**
 * Get all drivers
 */
export const getAllDrivers = async () => {
    const response = await api.get('/drivers');
    return response.data;
};

/**
 * Get available drivers for RDC
 */
export const getAvailableDrivers = async (rdcId) => {
    const response = await api.get(`/drivers/available/${rdcId}`);
    return response.data;
};

/**
 * Get driver by ID
 */
export const getDriverById = async (driverId) => {
    const response = await api.get(`/drivers/${driverId}`);
    return response.data;
};

/**
 * Update driver location
 */
export const updateDriverLocation = async (driverId, locationData) => {
    const response = await api.put(`/drivers/${driverId}/location`, locationData);
    return response.data;
};

/**
 * Update driver status
 */
export const updateDriverStatus = async (driverId, status) => {
    const response = await api.put(`/drivers/${driverId}/status`, null, {
        params: { status }
    });
    return response.data;
};
```

---

### 2. Delivery Components

**File: `src/components/delivery/DeliveryDashboard.jsx`**
```javascript
import React, { useState, useEffect } from 'react';
import * as deliveryService from '../../services/deliveryService';
import AssignDeliveryModal from './AssignDeliveryModal';
import DeliveryMap from './DeliveryMap';
import Loader from '../common/Loader';

export default function DeliveryDashboard() {
    const [activeDeliveries, setActiveDeliveries] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [selectedDelivery, setSelectedDelivery] = useState(null);
    const [showAssignModal, setShowAssignModal] = useState(false);

    // Hardcoded RDC ID - In real app, get from user context
    const rdcId = 4; // Western RDC

    useEffect(() => {
        loadActiveDeliveries();
        
        // Poll for updates every 30 seconds
        const interval = setInterval(loadActiveDeliveries, 30000);
        return () => clearInterval(interval);
    }, []);

    const loadActiveDeliveries = async () => {
        try {
            setLoading(true);
            const data = await deliveryService.getActiveDeliveries();
            setActiveDeliveries(data);
            setError(null);
        } catch (err) {
            setError('Failed to load deliveries');
            console.error('Error loading deliveries:', err);
        } finally {
            setLoading(false);
        }
    };

    const getStatusColor = (status) => {
        const colors = {
            PENDING_ASSIGNMENT: 'bg-gray-100 text-gray-800',
            ASSIGNED: 'bg-blue-100 text-blue-800',
            PICKED_UP: 'bg-purple-100 text-purple-800',
            IN_TRANSIT: 'bg-orange-100 text-orange-800',
            ARRIVED: 'bg-indigo-100 text-indigo-800',
            DELIVERED: 'bg-green-100 text-green-800',
            FAILED: 'bg-red-100 text-red-800'
        };
        return colors[status] || 'bg-gray-100 text-gray-800';
    };

    if (loading) return <Loader />;

    return (
        <div className="container mx-auto px-4 py-8">
            <div className="flex items-center justify-between mb-8">
                <div>
                    <h1 className="text-3xl font-bold text-gray-900">Delivery Dashboard</h1>
                    <p className="text-gray-600">Real-time delivery tracking</p>
                </div>
                <button
                    onClick={loadActiveDeliveries}
                    className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition"
                >
                    Refresh
                </button>
            </div>

            {error && (
                <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-6">
                    {error}
                </div>
            )}

            {/* Map View */}
            <div className="bg-white rounded-lg shadow-md p-6 mb-6">
                <h2 className="text-xl font-bold mb-4">Live Tracking Map</h2>
                <DeliveryMap deliveries={activeDeliveries} />
            </div>

            {/* Active Deliveries List */}
            <div className="bg-white rounded-lg shadow-md overflow-hidden">
                <div className="px-6 py-4 border-b border-gray-200">
                    <h2 className="text-xl font-bold">Active Deliveries ({activeDeliveries.length})</h2>
                </div>

                {activeDeliveries.length === 0 ? (
                    <div className="text-center py-12 text-gray-500">
                        No active deliveries at the moment
                    </div>
                ) : (
                    <div className="divide-y divide-gray-200">
                        {activeDeliveries.map(delivery => (
                            <div key={delivery.deliveryId} className="p-6 hover:bg-gray-50">
                                <div className="flex items-start justify-between">
                                    <div className="flex-1">
                                        <div className="flex items-center gap-3 mb-2">
                                            <h3 className="text-lg font-semibold text-gray-900">
                                                Order #{delivery.orderNumber}
                                            </h3>
                                            <span className={`px-3 py-1 rounded-full text-sm font-medium ${getStatusColor(delivery.status)}`}>
                                                {delivery.status.replace('_', ' ')}
                                            </span>
                                        </div>

                                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm text-gray-600">
                                            <div>
                                                <p className="font-medium text-gray-900">Driver</p>
                                                <p>{delivery.driverName || 'Not assigned'}</p>
                                                {delivery.vehicleNumber && (
                                                    <p className="text-xs text-gray-500">{delivery.vehicleNumber}</p>
                                                )}
                                            </div>

                                            <div>
                                                <p className="font-medium text-gray-900">Delivery Address</p>
                                                <p className="line-clamp-2">{delivery.deliveryAddress}</p>
                                            </div>

                                            <div>
                                                <p className="font-medium text-gray-900">Contact</p>
                                                <p>{delivery.contactNumber}</p>
                                            </div>

                                            <div>
                                                <p className="font-medium text-gray-900">Distance</p>
                                                <p>{delivery.estimatedDistanceKm} km</p>
                                            </div>
                                        </div>

                                        {delivery.pickupTime && (
                                            <div className="mt-3 text-sm text-gray-600">
                                                <p>Picked up: {new Date(delivery.pickupTime).toLocaleString()}</p>
                                            </div>
                                        )}
                                    </div>

                                    <button
                                        onClick={() => setSelectedDelivery(delivery)}
                                        className="ml-4 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition"
                                    >
                                        View Details
                                    </button>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>

            {showAssignModal && (
                <AssignDeliveryModal
                    onClose={() => setShowAssignModal(false)}
                    onSuccess={loadActiveDeliveries}
                />
            )}
        </div>
    );
}
```

**File: `src/components/delivery/DeliveryMap.jsx`**
```javascript
import React from 'react';

export default function DeliveryMap({ deliveries }) {
    // This is a placeholder - In production, use Google Maps or Leaflet
    
    return (
        <div className="relative h-96 bg-gray-200 rounded-lg overflow-hidden">
            {/* Map Placeholder */}
            <div className="absolute inset-0 flex items-center justify-center">
                <div className="text-center">
                    <svg className="mx-auto h-16 w-16 text-gray-400 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
                    </svg>
                    <p className="text-gray-600 font-medium">Interactive Map</p>
                    <p className="text-sm text-gray-500 mt-2">
                        {deliveries.length} active {deliveries.length === 1 ? 'delivery' : 'deliveries'}
                    </p>
                    <p className="text-xs text-gray-400 mt-4">
                        Integrate with Google Maps API or Leaflet for real GPS tracking
                    </p>
                </div>
            </div>

            {/* Delivery Markers (Simplified) */}
            <div className="absolute top-4 left-4 bg-white rounded-lg shadow-lg p-3 max-w-xs">
                <h3 className="font-semibold text-sm mb-2">Active Deliveries</h3>
                <div className="space-y-2 max-h-60 overflow-y-auto">
                    {deliveries.slice(0, 5).map((delivery, index) => (
                        <div key={delivery.deliveryId} className="flex items-center gap-2 text-xs">
                            <div className={`w-3 h-3 rounded-full ${
                                delivery.status === 'IN_TRANSIT' ? 'bg-orange-500' :
                                delivery.status === 'PICKED_UP' ? 'bg-purple-500' :
                                'bg-blue-500'
                            }`}></div>
                            <span className="truncate">
                                {delivery.orderNumber} - {delivery.driverName}
                            </span>
                        </div>
                    ))}
                    {deliveries.length > 5 && (
                        <p className="text-gray-500 text-xs">+{deliveries.length - 5} more</p>
                    )}
                </div>
            </div>
        </div>
    );
}
```

**File: `src/components/delivery/AssignDeliveryModal.jsx`**
```javascript
import React, { useState, useEffect } from 'react';
import * as deliveryService from '../../services/deliveryService';
import * as driverService from '../../services/driverService';
import * as orderService from '../../services/orderService';

export default function AssignDeliveryModal({ orderId, onClose, onSuccess }) {
    const [orders, setOrders] = useState([]);
    const [drivers, setDrivers] = useState([]);
    const [formData, setFormData] = useState({
        orderId: orderId || '',
        driverId: '',
        notes: ''
    });
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    const rdcId = 4; // Western RDC

    useEffect(() => {
        loadData();
    }, []);

    const loadData = async () => {
        try {
            const [ordersData, driversData] = await Promise.all([
                orderService.getUserOrders(),
                driverService.getAvailableDrivers(rdcId)
            ]);

            // Filter confirmed orders without delivery
            const confirmedOrders = ordersData.filter(o => o.status === 'CONFIRMED');
            setOrders(confirmedOrders);
            setDrivers(driversData);
        } catch (err) {
            setError('Failed to load data');
            console.error('Error:', err);
        }
    };

    const handleChange = (e) => {
        setFormData({
            ...formData,
            [e.target.name]: e.target.value
        });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        try {
            await deliveryService.assignDelivery({
                ...formData,
                orderId: parseInt(formData.orderId),
                driverId: parseInt(formData.driverId)
            });
            onSuccess();
            onClose();
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to assign delivery');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white rounded-lg shadow-xl p-6 max-w-md w-full mx-4">
                <div className="flex items-center justify-between mb-4">
                    <h2 className="text-xl font-bold text-gray-900">Assign Delivery</h2>
                    <button onClick={onClose} className="text-gray-400 hover:text-gray-600">
                        <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                        </svg>
                    </button>
                </div>

                {error && (
                    <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
                        {error}
                    </div>
                )}

                <form onSubmit={handleSubmit} className="space-y-4">
                    {!orderId && (
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                Order
                            </label>
                            <select
                                name="orderId"
                                required
                                value={formData.orderId}
                                onChange={handleChange}
                                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500"
                            >
                                <option value="">Select Order</option>
                                {orders.map(order => (
                                    <option key={order.orderId} value={order.orderId}>
                                        {order.orderNumber} - Rs. {order.totalAmount.toFixed(2)}
                                    </option>
                                ))}
                            </select>
                        </div>
                    )}

                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            Driver
                        </label>
                        <select
                            name="driverId"
                            required
                            value={formData.driverId}
                            onChange={handleChange}
                            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500"
                        >
                            <option value="">Select Driver</option>
                            {drivers.map(driver => (
                                <option key={driver.driverId} value={driver.driverId}>
                                    {driver.name} - {driver.vehicleNumber} ({driver.vehicleType})
                                </option>
                            ))}
                        </select>
                        {drivers.length === 0 && (
                            <p className="text-sm text-red-600 mt-1">No available drivers</p>
                        )}
                    </div>

                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            Notes (Optional)
                        </label>
                        <textarea
                            name="notes"
                            rows={3}
                            value={formData.notes}
                            onChange={handleChange}
                            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500"
                            placeholder="Special instructions for driver"
                        />
                    </div>

                    <div className="flex gap-3 pt-4">
                        <button
                            type="button"
                            onClick={onClose}
                            className="flex-1 px-4 py-2 border border-gray-300 text-gray-700 rounded-md hover:bg-gray-50"
                        >
                            Cancel
                        </button>
                        <button
                            type="submit"
                            disabled={loading || drivers.length === 0}
                            className="flex-1 px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:bg-gray-400"
                        >
                            {loading ? 'Assigning...' : 'Assign Delivery'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}
```

---

### 3. Add Routes to App.jsx

**Update: `src/App.jsx`**
Add import:
```javascript
import DeliveryDashboard from './components/delivery/DeliveryDashboard';
```

Add route:
```javascript
<Route path="/deliveries" element={
    <ProtectedRoute>
        <DeliveryDashboard />
    </ProtectedRoute>
} />
```

---

### 4. Update Navbar

**Update: `src/components/common/Navbar.jsx`**
Add delivery link:
```javascript
{(user?.role === 'LOGISTICS_OFFICER' || user?.role === 'DRIVER' || user?.role === 'HO_MANAGER' || user?.role === 'ADMIN') && (
    <Link to="/deliveries" className="hover:text-blue-200 transition">
        Deliveries
    </Link>
)}
```

---

## TESTING FEATURE 3

### Test Users
```
Username: driver1
Password: password123
Role: DRIVER

Username: logistics_officer
Password: password123
Role: LOGISTICS_OFFICER
```

### Test Steps
1. Login as logistics officer
2. Navigate to Deliveries
3. View active deliveries
4. Assign a CONFIRMED order to a driver
5. View delivery on map (placeholder)
6. Update delivery status
7. Mark as delivered

---

## NEXT ENHANCEMENTS

### Google Maps Integration (Optional)
```bash
npm install @react-google-maps/api
```

Then replace DeliveryMap.jsx with real Google Maps implementation.

---

END OF FEATURE 3 IMPLEMENTATION