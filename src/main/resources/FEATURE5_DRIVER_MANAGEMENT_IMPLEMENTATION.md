# FEATURE 5: DRIVER & VEHICLE MANAGEMENT

---

## FEATURE OVERVIEW

### What We'll Build:
1. ✅ Driver registration and management
2. ✅ Vehicle assignment to drivers
3. ✅ Driver availability management (AVAILABLE, ON_DELIVERY, OFF_DUTY, ON_BREAK)
4. ✅ View all drivers by RDC
5. ✅ Update driver status
6. ✅ Assign drivers to deliveries (already implemented in Feature 3)
7. ✅ Driver profile management
8. ✅ Role-based permissions for driver management

---

## WHY THIS FEATURE IS NEEDED

### Current Problem:
- ❌ No way to create drivers in the system
- ❌ No driver management interface
- ❌ Cannot assign vehicles to drivers
- ❌ Cannot manage driver availability
- ❌ Logistics Officer sees "No available drivers" when trying to assign deliveries

### After Implementation:
- ✅ HO_MANAGER/ADMIN can create and manage drivers
- ✅ Drivers can be assigned to specific RDCs
- ✅ Vehicles are linked to drivers
- ✅ Driver status can be updated (AVAILABLE, ON_DELIVERY, OFF_DUTY, ON_BREAK)
- ✅ Logistics Officer can see available drivers for delivery assignment

---

## BACKEND IMPLEMENTATION

### 1. Driver & Vehicle Entities (Already Defined in Feature 3)

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

### 2. DTOs for Driver Management

**File: `src/main/java/com/isdn/dto/request/CreateDriverRequest.java`**
```java
package com.isdn.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateDriverRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotNull(message = "RDC ID is required")
    private Long rdcId;

    @NotBlank(message = "License number is required")
    private String licenseNumber;

    @NotBlank(message = "Vehicle number is required")
    private String vehicleNumber;

    @NotBlank(message = "Vehicle type is required")
    private String vehicleType; // e.g., "Van", "Truck", "Motorcycle"
}
```

**File: `src/main/java/com/isdn/dto/request/UpdateDriverRequest.java`**
```java
package com.isdn.dto.request;

import com.isdn.model.DriverStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDriverRequest {

    private String phoneNumber;

    private String licenseNumber;

    private String vehicleNumber;

    private String vehicleType;

    private DriverStatus status;

    private Long rdcId;

    private Boolean active;
}
```

---

### 3. Repository (Already Defined in Feature 3)

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

### 4. Extended DriverService

**File: `src/main/java/com/isdn/service/DriverService.java`**

Add these new methods to the existing DriverService from Feature 3:

```java
/**
 * Create new driver
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
            .fullName(request.getFullName())
            .role(UserRole.DRIVER)
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
 * Update driver information
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
 * Delete driver (soft delete)
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
 * Get drivers by RDC
 */
@Transactional(readOnly = true)
public List<DriverResponse> getDriversByRdc(Long rdcId) {
    log.info("Fetching drivers for RDC: {}", rdcId);

    List<Driver> drivers = driverRepository.findByRdc_RdcId(rdcId);

    return drivers.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
}
```

---

### 5. Extended DriverController

**File: `src/main/java/com/isdn/controller/DriverController.java`**

Add these endpoints to the existing DriverController from Feature 3:

```java
/**
 * POST /api/drivers - Create new driver
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
 * PUT /api/drivers/{driverId} - Update driver
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
 * DELETE /api/drivers/{driverId} - Delete driver (soft delete)
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
 * GET /api/drivers/rdc/{rdcId} - Get drivers by RDC
 */
@GetMapping("/rdc/{rdcId}")
@PreAuthorize("hasAnyRole('LOGISTICS_OFFICER', 'RDC_STAFF', 'HO_MANAGER', 'ADMIN')")
public ResponseEntity<List<DriverResponse>> getDriversByRdc(@PathVariable Long rdcId) {
    log.info("GET /api/drivers/rdc/{} - Fetch drivers for RDC", rdcId);
    List<DriverResponse> drivers = driverService.getDriversByRdc(rdcId);
    return ResponseEntity.ok(drivers);
}
```

---

### 6. Database Schema

**SQL for creating drivers table:**
```sql
CREATE TABLE drivers (
    driver_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    rdc_id BIGINT NOT NULL,
    license_number VARCHAR(50) NOT NULL UNIQUE,
    vehicle_number VARCHAR(50) NOT NULL,
    vehicle_type VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    current_latitude DECIMAL(10, 7),
    current_longitude DECIMAL(10, 7),
    last_location_update TIMESTAMP,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (rdc_id) REFERENCES rdcs(rdc_id),
    INDEX idx_rdc_status (rdc_id, status, active),
    INDEX idx_user (user_id),
    INDEX idx_license (license_number)
);
```

---

## FRONTEND IMPLEMENTATION

### 1. Extended Driver Service

**File: `src/services/driverService.js`**

Add these functions to the existing driverService from Feature 3:

```javascript
/**
 * Create new driver
 */
export const createDriver = async (driverData) => {
    const response = await api.post('/drivers', driverData);
    return response.data;
};

/**
 * Update driver
 */
export const updateDriver = async (driverId, driverData) => {
    const response = await api.put(`/drivers/${driverId}`, driverData);
    return response.data;
};

/**
 * Delete driver
 */
export const deleteDriver = async (driverId) => {
    const response = await api.delete(`/drivers/${driverId}`);
    return response.data;
};

/**
 * Get drivers by RDC
 */
export const getDriversByRdc = async (rdcId) => {
    const response = await api.get(`/drivers/rdc/${rdcId}`);
    return response.data;
};
```

---

### 2. Driver Management Component

**File: `src/components/drivers/DriverManagement.jsx`**

```javascript
import React, { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import * as driverService from '../../services/driverService';
import CreateDriverModal from './CreateDriverModal';
import UpdateDriverModal from './UpdateDriverModal';
import Loader from '../common/Loader';

export default function DriverManagement() {
    const [drivers, setDrivers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [selectedDriver, setSelectedDriver] = useState(null);
    const [showCreateModal, setShowCreateModal] = useState(false);
    const [showUpdateModal, setShowUpdateModal] = useState(false);

    useEffect(() => {
        loadDrivers();
    }, []);

    const loadDrivers = async () => {
        try {
            setLoading(true);
            const data = await driverService.getAllDrivers();
            setDrivers(data);
            setError(null);
        } catch (err) {
            setError('Failed to load drivers');
            toast.error('Failed to load drivers');
            console.error('Error loading drivers:', err);
        } finally {
            setLoading(false);
        }
    };

    const handleDelete = async (driverId, driverName) => {
        if (!window.confirm(`Are you sure you want to delete driver "${driverName}"?`)) {
            return;
        }

        try {
            await driverService.deleteDriver(driverId);
            toast.success('Driver deleted successfully!');
            loadDrivers();
        } catch (err) {
            toast.error(err.response?.data?.message || 'Failed to delete driver');
        }
    };

    const getStatusColor = (status) => {
        const colors = {
            AVAILABLE: 'bg-green-100 text-green-800',
            ON_DELIVERY: 'bg-orange-100 text-orange-800',
            OFF_DUTY: 'bg-gray-100 text-gray-800',
            ON_BREAK: 'bg-yellow-100 text-yellow-800'
        };
        return colors[status] || 'bg-gray-100 text-gray-800';
    };

    if (loading) return <Loader />;

    return (
        <div className="container mx-auto px-4 py-8">
            <div className="flex items-center justify-between mb-8">
                <div>
                    <h1 className="text-3xl font-bold text-gray-900">Driver Management</h1>
                    <p className="text-gray-600">Manage drivers and vehicles</p>
                </div>
                <div className="flex gap-3">
                    <button
                        onClick={() => setShowCreateModal(true)}
                        className="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition"
                    >
                        Add Driver
                    </button>
                    <button
                        onClick={loadDrivers}
                        className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition"
                    >
                        Refresh
                    </button>
                </div>
            </div>

            {error && (
                <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-6">
                    {error}
                </div>
            )}

            <div className="bg-white rounded-lg shadow-md overflow-hidden">
                <div className="px-6 py-4 border-b border-gray-200">
                    <h2 className="text-xl font-bold">All Drivers ({drivers.length})</h2>
                </div>

                {drivers.length === 0 ? (
                    <div className="text-center py-12 text-gray-500">
                        No drivers found. Click "Add Driver" to create one.
                    </div>
                ) : (
                    <div className="overflow-x-auto">
                        <table className="min-w-full divide-y divide-gray-200">
                            <thead className="bg-gray-50">
                                <tr>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        Driver Name
                                    </th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        License Number
                                    </th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        Vehicle
                                    </th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        RDC
                                    </th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        Status
                                    </th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        Active Deliveries
                                    </th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        Actions
                                    </th>
                                </tr>
                            </thead>
                            <tbody className="bg-white divide-y divide-gray-200">
                                {drivers.map(driver => (
                                    <tr key={driver.driverId} className="hover:bg-gray-50">
                                        <td className="px-6 py-4 whitespace-nowrap">
                                            <div className="text-sm font-medium text-gray-900">
                                                {driver.name}
                                            </div>
                                            <div className="text-sm text-gray-500">
                                                {driver.phoneNumber}
                                            </div>
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                            {driver.licenseNumber}
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap">
                                            <div className="text-sm text-gray-900">
                                                {driver.vehicleNumber}
                                            </div>
                                            <div className="text-sm text-gray-500">
                                                {driver.vehicleType}
                                            </div>
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                            {driver.rdcName}
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap">
                                            <span className={`px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full ${getStatusColor(driver.status)}`}>
                                                {driver.status.replace('_', ' ')}
                                            </span>
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500 text-center">
                                            {driver.activeDeliveries || 0}
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap text-sm font-medium space-x-2">
                                            <button
                                                onClick={() => {
                                                    setSelectedDriver(driver);
                                                    setShowUpdateModal(true);
                                                }}
                                                className="text-blue-600 hover:text-blue-900"
                                            >
                                                Edit
                                            </button>
                                            <button
                                                onClick={() => handleDelete(driver.driverId, driver.name)}
                                                className="text-red-600 hover:text-red-900"
                                            >
                                                Delete
                                            </button>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>

            {showCreateModal && (
                <CreateDriverModal
                    onClose={() => setShowCreateModal(false)}
                    onSuccess={() => {
                        loadDrivers();
                        toast.success('Driver created successfully!');
                    }}
                />
            )}

            {showUpdateModal && selectedDriver && (
                <UpdateDriverModal
                    driver={selectedDriver}
                    onClose={() => {
                        setShowUpdateModal(false);
                        setSelectedDriver(null);
                    }}
                    onSuccess={() => {
                        loadDrivers();
                        toast.success('Driver updated successfully!');
                    }}
                />
            )}
        </div>
    );
}
```

---

### 3. Create Driver Modal

**File: `src/components/drivers/CreateDriverModal.jsx`**

```javascript
import React, { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import * as driverService from '../../services/driverService';

export default function CreateDriverModal({ onClose, onSuccess }) {
    const [rdcs, setRdcs] = useState([]);
    const [formData, setFormData] = useState({
        username: '',
        password: '',
        email: '',
        phoneNumber: '',
        fullName: '',
        rdcId: '',
        licenseNumber: '',
        vehicleNumber: '',
        vehicleType: ''
    });
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    useEffect(() => {
        loadRdcs();
    }, []);

    const loadRdcs = async () => {
        try {
            // Assuming you have an RDC service
            // const data = await rdcService.getAllRdcs();
            // setRdcs(data);

            // For now, hardcoded RDCs
            setRdcs([
                { rdcId: 1, name: 'Northern RDC' },
                { rdcId: 2, name: 'Southern RDC' },
                { rdcId: 3, name: 'Eastern RDC' },
                { rdcId: 4, name: 'Western RDC' }
            ]);
        } catch (err) {
            console.error('Error loading RDCs:', err);
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
            await driverService.createDriver({
                ...formData,
                rdcId: parseInt(formData.rdcId)
            });
            onSuccess();
            onClose();
        } catch (err) {
            const errorMsg = err.response?.data?.message || 'Failed to create driver';
            setError(errorMsg);
            toast.error(errorMsg);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 overflow-y-auto">
            <div className="bg-white rounded-lg shadow-xl p-6 max-w-2xl w-full mx-4 my-8">
                <div className="flex items-center justify-between mb-4">
                    <h2 className="text-xl font-bold text-gray-900">Create New Driver</h2>
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
                    {/* User Account Details */}
                    <div className="border-b pb-4">
                        <h3 className="font-semibold text-gray-900 mb-3">Account Details</h3>
                        <div className="grid grid-cols-2 gap-4">
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">
                                    Full Name *
                                </label>
                                <input
                                    type="text"
                                    name="fullName"
                                    required
                                    value={formData.fullName}
                                    onChange={handleChange}
                                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                                />
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">
                                    Username *
                                </label>
                                <input
                                    type="text"
                                    name="username"
                                    required
                                    value={formData.username}
                                    onChange={handleChange}
                                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                                />
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">
                                    Password *
                                </label>
                                <input
                                    type="password"
                                    name="password"
                                    required
                                    value={formData.password}
                                    onChange={handleChange}
                                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                                />
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">
                                    Email *
                                </label>
                                <input
                                    type="email"
                                    name="email"
                                    required
                                    value={formData.email}
                                    onChange={handleChange}
                                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                                />
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">
                                    Phone Number *
                                </label>
                                <input
                                    type="tel"
                                    name="phoneNumber"
                                    required
                                    value={formData.phoneNumber}
                                    onChange={handleChange}
                                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                                />
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">
                                    Assigned RDC *
                                </label>
                                <select
                                    name="rdcId"
                                    required
                                    value={formData.rdcId}
                                    onChange={handleChange}
                                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                                >
                                    <option value="">Select RDC</option>
                                    {rdcs.map(rdc => (
                                        <option key={rdc.rdcId} value={rdc.rdcId}>
                                            {rdc.name}
                                        </option>
                                    ))}
                                </select>
                            </div>
                        </div>
                    </div>

                    {/* Driver & Vehicle Details */}
                    <div>
                        <h3 className="font-semibold text-gray-900 mb-3">Driver & Vehicle Details</h3>
                        <div className="grid grid-cols-2 gap-4">
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">
                                    License Number *
                                </label>
                                <input
                                    type="text"
                                    name="licenseNumber"
                                    required
                                    value={formData.licenseNumber}
                                    onChange={handleChange}
                                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                                    placeholder="DL12345"
                                />
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">
                                    Vehicle Number *
                                </label>
                                <input
                                    type="text"
                                    name="vehicleNumber"
                                    required
                                    value={formData.vehicleNumber}
                                    onChange={handleChange}
                                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                                    placeholder="CAB-1234"
                                />
                            </div>

                            <div className="col-span-2">
                                <label className="block text-sm font-medium text-gray-700 mb-1">
                                    Vehicle Type *
                                </label>
                                <select
                                    name="vehicleType"
                                    required
                                    value={formData.vehicleType}
                                    onChange={handleChange}
                                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                                >
                                    <option value="">Select Vehicle Type</option>
                                    <option value="Motorcycle">Motorcycle</option>
                                    <option value="Three-Wheeler">Three-Wheeler</option>
                                    <option value="Van">Van</option>
                                    <option value="Truck">Truck</option>
                                    <option value="Lorry">Lorry</option>
                                </select>
                            </div>
                        </div>
                    </div>

                    <div className="flex gap-3 pt-4">
                        <button
                            type="button"
                            onClick={onClose}
                            className="flex-1 px-4 py-2 border border-gray-300 text-gray-700 rounded-md hover:bg-gray-50 transition"
                        >
                            Cancel
                        </button>
                        <button
                            type="submit"
                            disabled={loading}
                            className="flex-1 px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 transition disabled:bg-gray-400 disabled:cursor-not-allowed"
                        >
                            {loading ? 'Creating...' : 'Create Driver'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}
```

---

### 4. Update Driver Modal

**File: `src/components/drivers/UpdateDriverModal.jsx`**

```javascript
import React, { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import * as driverService from '../../services/driverService';

export default function UpdateDriverModal({ driver, onClose, onSuccess }) {
    const [rdcs, setRdcs] = useState([]);
    const [formData, setFormData] = useState({
        phoneNumber: driver.phoneNumber || '',
        licenseNumber: driver.licenseNumber || '',
        vehicleNumber: driver.vehicleNumber || '',
        vehicleType: driver.vehicleType || '',
        status: driver.status || 'AVAILABLE',
        rdcId: driver.rdcId || '',
        active: driver.active !== undefined ? driver.active : true
    });
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    useEffect(() => {
        loadRdcs();
    }, []);

    const loadRdcs = async () => {
        try {
            // Hardcoded RDCs for now
            setRdcs([
                { rdcId: 1, name: 'Northern RDC' },
                { rdcId: 2, name: 'Southern RDC' },
                { rdcId: 3, name: 'Eastern RDC' },
                { rdcId: 4, name: 'Western RDC' }
            ]);
        } catch (err) {
            console.error('Error loading RDCs:', err);
        }
    };

    const handleChange = (e) => {
        const value = e.target.type === 'checkbox' ? e.target.checked : e.target.value;
        setFormData({
            ...formData,
            [e.target.name]: value
        });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        try {
            await driverService.updateDriver(driver.driverId, {
                ...formData,
                rdcId: formData.rdcId ? parseInt(formData.rdcId) : undefined
            });
            onSuccess();
            onClose();
        } catch (err) {
            const errorMsg = err.response?.data?.message || 'Failed to update driver';
            setError(errorMsg);
            toast.error(errorMsg);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white rounded-lg shadow-xl p-6 max-w-2xl w-full mx-4">
                <div className="flex items-center justify-between mb-4">
                    <h2 className="text-xl font-bold text-gray-900">Update Driver</h2>
                    <button onClick={onClose} className="text-gray-400 hover:text-gray-600">
                        <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                        </svg>
                    </button>
                </div>

                <div className="mb-4 p-4 bg-gray-50 rounded">
                    <p className="text-sm text-gray-600">Driver Name</p>
                    <p className="font-semibold text-gray-900">{driver.name}</p>
                </div>

                {error && (
                    <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
                        {error}
                    </div>
                )}

                <form onSubmit={handleSubmit} className="space-y-4">
                    <div className="grid grid-cols-2 gap-4">
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                Phone Number
                            </label>
                            <input
                                type="tel"
                                name="phoneNumber"
                                value={formData.phoneNumber}
                                onChange={handleChange}
                                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                            />
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                License Number
                            </label>
                            <input
                                type="text"
                                name="licenseNumber"
                                value={formData.licenseNumber}
                                onChange={handleChange}
                                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                            />
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                Vehicle Number
                            </label>
                            <input
                                type="text"
                                name="vehicleNumber"
                                value={formData.vehicleNumber}
                                onChange={handleChange}
                                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                            />
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                Vehicle Type
                            </label>
                            <select
                                name="vehicleType"
                                value={formData.vehicleType}
                                onChange={handleChange}
                                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                            >
                                <option value="Motorcycle">Motorcycle</option>
                                <option value="Three-Wheeler">Three-Wheeler</option>
                                <option value="Van">Van</option>
                                <option value="Truck">Truck</option>
                                <option value="Lorry">Lorry</option>
                            </select>
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                Status
                            </label>
                            <select
                                name="status"
                                value={formData.status}
                                onChange={handleChange}
                                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                            >
                                <option value="AVAILABLE">Available</option>
                                <option value="ON_DELIVERY">On Delivery</option>
                                <option value="OFF_DUTY">Off Duty</option>
                                <option value="ON_BREAK">On Break</option>
                            </select>
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                Assigned RDC
                            </label>
                            <select
                                name="rdcId"
                                value={formData.rdcId}
                                onChange={handleChange}
                                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                            >
                                {rdcs.map(rdc => (
                                    <option key={rdc.rdcId} value={rdc.rdcId}>
                                        {rdc.name}
                                    </option>
                                ))}
                            </select>
                        </div>
                    </div>

                    <div className="flex items-center">
                        <input
                            type="checkbox"
                            name="active"
                            checked={formData.active}
                            onChange={handleChange}
                            className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                        />
                        <label className="ml-2 block text-sm text-gray-900">
                            Active Driver
                        </label>
                    </div>

                    <div className="flex gap-3 pt-4">
                        <button
                            type="button"
                            onClick={onClose}
                            className="flex-1 px-4 py-2 border border-gray-300 text-gray-700 rounded-md hover:bg-gray-50 transition"
                        >
                            Cancel
                        </button>
                        <button
                            type="submit"
                            disabled={loading}
                            className="flex-1 px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition disabled:bg-gray-400 disabled:cursor-not-allowed"
                        >
                            {loading ? 'Updating...' : 'Update Driver'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}
```

---

### 5. Add Routes and Navigation

**Update: `src/App.jsx`**

Add import:
```javascript
import DriverManagement from './components/drivers/DriverManagement';
```

Add route:
```javascript
<Route path="/driver-management" element={
    <ProtectedRoute>
        <DriverManagement />
    </ProtectedRoute>
} />
```

**Update: `src/components/common/Navbar.jsx`**

Add link:
```javascript
{(user?.role === 'HO_MANAGER' || user?.role === 'ADMIN') && (
    <Link to="/driver-management" className="hover:text-blue-200 transition">
        Driver Management
    </Link>
)}
```

---

## PERMISSIONS MATRIX

| Role | View Drivers | Create Driver | Update Driver | Delete Driver | Change Availability |
|------|--------------|---------------|---------------|---------------|---------------------|
| CUSTOMER | ❌ | ❌ | ❌ | ❌ | ❌ |
| DRIVER | ✅ (Self only) | ❌ | ❌ | ❌ | ✅ (Self only) |
| RDC_STAFF | ✅ | ❌ | ❌ | ❌ | ❌ |
| LOGISTICS_OFFICER | ✅ | ❌ | ❌ | ❌ | ❌ |
| HO_MANAGER | ✅ | ✅ | ✅ | ✅ | ✅ |
| ADMIN | ✅ | ✅ | ✅ | ✅ | ✅ |

---

## API ENDPOINTS SUMMARY

| Method | Endpoint | Role Required | Description |
|--------|----------|---------------|-------------|
| GET | `/api/drivers` | LOGISTICS_OFFICER, RDC_STAFF, HO_MANAGER, ADMIN | Get all drivers |
| GET | `/api/drivers/available/{rdcId}` | LOGISTICS_OFFICER, RDC_STAFF, HO_MANAGER, ADMIN | Get available drivers for RDC |
| GET | `/api/drivers/{driverId}` | DRIVER, LOGISTICS_OFFICER, RDC_STAFF, HO_MANAGER, ADMIN | Get driver by ID |
| GET | `/api/drivers/rdc/{rdcId}` | LOGISTICS_OFFICER, RDC_STAFF, HO_MANAGER, ADMIN | Get drivers by RDC |
| POST | `/api/drivers` | HO_MANAGER, ADMIN | Create new driver |
| PUT | `/api/drivers/{driverId}` | HO_MANAGER, ADMIN | Update driver |
| PUT | `/api/drivers/{driverId}/status` | DRIVER, LOGISTICS_OFFICER, ADMIN | Update driver status |
| PUT | `/api/drivers/{driverId}/location` | DRIVER, ADMIN | Update driver GPS location |
| DELETE | `/api/drivers/{driverId}` | HO_MANAGER, ADMIN | Delete driver (soft delete) |

---

## TESTING FEATURE 5

### Test Steps

**Step 1: Create Driver (as HO_MANAGER/ADMIN)**
1. Login as HO Manager or Admin
2. Go to "Driver Management"
3. Click "Add Driver"
4. Fill in all details:
   - Full Name: John Driver
   - Username: driver1
   - Password: password123
   - Email: driver1@example.com
   - Phone: 0771234567
   - RDC: Western RDC
   - License: DL12345
   - Vehicle Number: CAB-1234
   - Vehicle Type: Van
5. Click "Create Driver"
6. Verify driver appears in list with status "AVAILABLE"

**Step 2: Verify Driver Login**
1. Logout
2. Login with:
   - Username: driver1
   - Password: password123
3. Verify login successful
4. Verify driver dashboard (if implemented)

**Step 3: Test Delivery Assignment (as LOGISTICS_OFFICER)**
1. Login as Logistics Officer
2. Go to "Deliveries"
3. Click "Assign Delivery"
4. Verify dropdown shows:
   - ✅ Confirmed orders
   - ✅ Available drivers (including newly created driver1)
5. Assign delivery successfully

**Step 4: Update Driver Status**
1. Login as HO Manager
2. Go to "Driver Management"
3. Click "Edit" on driver1
4. Change status to "OFF_DUTY"
5. Save
6. Logout and login as Logistics Officer
7. Try to assign delivery
8. Verify driver1 does NOT appear in available drivers

---

## IMPORTANT NOTES FOR BACKEND DEVELOPERS

### 1. Password Encoding
```java
// When creating driver user, MUST encode password
user.setPassword(passwordEncoder.encode(request.getPassword()));
```

### 2. Soft Delete Implementation
```java
// NEVER hard delete - always soft delete
driver.setActive(false);
driver.getUser().setActive(false);
```

### 3. Status Management
```java
// When delivery is assigned, automatically set driver status
driver.setStatus(DriverStatus.ON_DELIVERY);

// When delivery is completed/failed, set back to AVAILABLE
driver.setStatus(DriverStatus.AVAILABLE);
```

### 4. Validation Checks
```java
// Before deleting driver
if (activeDeliveries > 0) {
    throw new BadRequestException("Cannot delete driver with active deliveries");
}

// Before creating driver
if (driverRepository.findByLicenseNumber(licenseNumber).isPresent()) {
    throw new BadRequestException("License number already exists");
}
```

---

## WORKFLOW

```
HO_MANAGER creates driver
    ↓
Driver account created with status = AVAILABLE
    ↓
Driver assigned to RDC (e.g., Western RDC)
    ↓
Driver logs in with credentials
    ↓
LOGISTICS_OFFICER assigns delivery to driver
    ↓
Driver status: AVAILABLE → ON_DELIVERY
    ↓
Driver completes delivery
    ↓
Driver status: ON_DELIVERY → AVAILABLE
```

---

END OF FEATURE 5 IMPLEMENTATION
