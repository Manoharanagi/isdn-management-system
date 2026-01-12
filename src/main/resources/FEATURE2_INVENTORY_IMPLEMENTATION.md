# FEATURE 2: INVENTORY SYNCHRONISATION - COMPLETE IMPLEMENTATION

---

## BACKEND IMPLEMENTATION

### 1. Additional Entity - StockMovement

**File: `src/main/java/com/isdn/model/StockMovement.java`**
```java
package com.isdn.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_movements")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockMovement {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "movement_id")
    private Long movementId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id", nullable = false)
    private Inventory inventory;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false)
    private MovementType movementType;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(name = "previous_stock", nullable = false)
    private Integer previousStock;
    
    @Column(name = "new_stock", nullable = false)
    private Integer newStock;
    
    @Column(columnDefinition = "TEXT")
    private String reason;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by", nullable = false)
    private User performedBy;
    
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime timestamp;
}
```

**File: `src/main/java/com/isdn/model/MovementType.java`**
```java
package com.isdn.model;

public enum MovementType {
    RECEIVED("Stock Received"),
    SOLD("Sold to Customer"),
    DAMAGED("Damaged/Expired"),
    RETURNED("Customer Return"),
    TRANSFERRED_OUT("Transferred to Another RDC"),
    TRANSFERRED_IN("Received from Another RDC"),
    ADJUSTMENT("Stock Adjustment");
    
    private final String displayName;
    
    MovementType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
```

---

### 2. Repository - StockMovementRepository

**File: `src/main/java/com/isdn/repository/StockMovementRepository.java`**
```java
package com.isdn.repository;

import com.isdn.model.Inventory;
import com.isdn.model.MovementType;
import com.isdn.model.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    
    List<StockMovement> findByInventoryOrderByTimestampDesc(Inventory inventory);
    
    List<StockMovement> findByInventory_InventoryIdOrderByTimestampDesc(Long inventoryId);
    
    @Query("SELECT sm FROM StockMovement sm WHERE sm.inventory.rdc.rdcId = :rdcId ORDER BY sm.timestamp DESC")
    List<StockMovement> findByRdcIdOrderByTimestampDesc(@Param("rdcId") Long rdcId);
    
    @Query("SELECT sm FROM StockMovement sm WHERE sm.timestamp BETWEEN :startDate AND :endDate ORDER BY sm.timestamp DESC")
    List<StockMovement> findMovementsBetweenDates(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    List<StockMovement> findByMovementTypeOrderByTimestampDesc(MovementType movementType);
}
```

---

### 3. DTOs for Inventory

**File: `src/main/java/com/isdn/dto/request/StockUpdateRequest.java`**
```java
package com.isdn.dto.request;

import com.isdn.model.MovementType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockUpdateRequest {
    
    @NotNull(message = "Movement type is required")
    private MovementType movementType;
    
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
    
    private String reason;
}
```

**File: `src/main/java/com/isdn/dto/request/StockTransferRequest.java`**
```java
package com.isdn.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockTransferRequest {
    
    @NotNull(message = "Product ID is required")
    private Long productId;
    
    @NotNull(message = "From RDC ID is required")
    private Long fromRdcId;
    
    @NotNull(message = "To RDC ID is required")
    private Long toRdcId;
    
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
    
    private String reason;
}
```

**File: `src/main/java/com/isdn/dto/response/InventoryResponse.java`**
```java
package com.isdn.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponse {
    private Long inventoryId;
    private Long productId;
    private String productName;
    private String productSku;
    private Long rdcId;
    private String rdcName;
    private Integer quantityOnHand;
    private Integer reorderLevel;
    private String status; // "OK", "LOW_STOCK", "OUT_OF_STOCK"
    private LocalDateTime lastUpdated;
}
```

**File: `src/main/java/com/isdn/dto/response/StockMovementResponse.java`**
```java
package com.isdn.dto.response;

import com.isdn.model.MovementType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockMovementResponse {
    private Long movementId;
    private Long inventoryId;
    private String productName;
    private String rdcName;
    private MovementType movementType;
    private Integer quantity;
    private Integer previousStock;
    private Integer newStock;
    private String reason;
    private String performedBy;
    private LocalDateTime timestamp;
}
```

---

### 4. Service - InventoryService

**File: `src/main/java/com/isdn/service/InventoryService.java`**
```java
package com.isdn.service;

import com.isdn.dto.request.StockTransferRequest;
import com.isdn.dto.request.StockUpdateRequest;
import com.isdn.dto.response.InventoryResponse;
import com.isdn.dto.response.StockMovementResponse;
import com.isdn.exception.BadRequestException;
import com.isdn.exception.ResourceNotFoundException;
import com.isdn.model.*;
import com.isdn.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {
    
    private final InventoryRepository inventoryRepository;
    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;
    private final RDCRepository rdcRepository;
    private final UserService userService;
    
    /**
     * Get all inventory for an RDC
     */
    @Transactional(readOnly = true)
    public List<InventoryResponse> getInventoryByRdc(Long rdcId) {
        log.info("Fetching inventory for RDC: {}", rdcId);
        
        RDC rdc = rdcRepository.findById(rdcId)
                .orElseThrow(() -> new ResourceNotFoundException("RDC not found"));
        
        List<Inventory> inventories = inventoryRepository.findByRdc(rdc);
        
        return inventories.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get low stock items for an RDC
     */
    @Transactional(readOnly = true)
    public List<InventoryResponse> getLowStockItems(Long rdcId) {
        log.info("Fetching low stock items for RDC: {}", rdcId);
        
        List<Inventory> lowStockItems = inventoryRepository.findLowStockItemsByRdc(rdcId);
        
        return lowStockItems.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Update stock (add/reduce)
     */
    @Transactional
    public InventoryResponse updateStock(Long inventoryId, StockUpdateRequest request, Long userId) {
        log.info("Updating stock for inventory: {}, type: {}, quantity: {}", 
                 inventoryId, request.getMovementType(), request.getQuantity());
        
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found"));
        
        User user = userService.getUserById(userId);
        
        int previousStock = inventory.getQuantityOnHand();
        int newStock = calculateNewStock(previousStock, request.getMovementType(), request.getQuantity());
        
        if (newStock < 0) {
            throw new BadRequestException("Insufficient stock. Available: " + previousStock);
        }
        
        // Update inventory
        inventory.setQuantityOnHand(newStock);
        inventoryRepository.save(inventory);
        
        // Record movement
        StockMovement movement = StockMovement.builder()
                .inventory(inventory)
                .movementType(request.getMovementType())
                .quantity(request.getQuantity())
                .previousStock(previousStock)
                .newStock(newStock)
                .reason(request.getReason())
                .performedBy(user)
                .build();
        
        stockMovementRepository.save(movement);
        
        log.info("Stock updated successfully. Previous: {}, New: {}", previousStock, newStock);
        
        return mapToResponse(inventory);
    }
    
    /**
     * Transfer stock between RDCs
     */
    @Transactional
    public void transferStock(StockTransferRequest request, Long userId) {
        log.info("Transferring stock: Product {}, From RDC {}, To RDC {}, Quantity: {}", 
                 request.getProductId(), request.getFromRdcId(), request.getToRdcId(), request.getQuantity());
        
        if (request.getFromRdcId().equals(request.getToRdcId())) {
            throw new BadRequestException("Cannot transfer to the same RDC");
        }
        
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        
        RDC fromRdc = rdcRepository.findById(request.getFromRdcId())
                .orElseThrow(() -> new ResourceNotFoundException("Source RDC not found"));
        
        RDC toRdc = rdcRepository.findById(request.getToRdcId())
                .orElseThrow(() -> new ResourceNotFoundException("Destination RDC not found"));
        
        User user = userService.getUserById(userId);
        
        // Get source inventory
        Inventory fromInventory = inventoryRepository.findByProductAndRdc(product, fromRdc)
                .orElseThrow(() -> new ResourceNotFoundException("Source inventory not found"));
        
        if (fromInventory.getQuantityOnHand() < request.getQuantity()) {
            throw new BadRequestException("Insufficient stock at source RDC. Available: " + 
                                        fromInventory.getQuantityOnHand());
        }
        
        // Get or create destination inventory
        Inventory toInventory = inventoryRepository.findByProductAndRdc(product, toRdc)
                .orElseGet(() -> {
                    Inventory newInventory = Inventory.builder()
                            .product(product)
                            .rdc(toRdc)
                            .quantityOnHand(0)
                            .reorderLevel(50)
                            .build();
                    return inventoryRepository.save(newInventory);
                });
        
        // Update stocks
        int fromPreviousStock = fromInventory.getQuantityOnHand();
        int toPreviousStock = toInventory.getQuantityOnHand();
        
        fromInventory.setQuantityOnHand(fromPreviousStock - request.getQuantity());
        toInventory.setQuantityOnHand(toPreviousStock + request.getQuantity());
        
        inventoryRepository.save(fromInventory);
        inventoryRepository.save(toInventory);
        
        // Record movements
        String transferReason = request.getReason() != null ? request.getReason() : 
                               "Transfer to " + toRdc.getName();
        
        StockMovement outMovement = StockMovement.builder()
                .inventory(fromInventory)
                .movementType(MovementType.TRANSFERRED_OUT)
                .quantity(request.getQuantity())
                .previousStock(fromPreviousStock)
                .newStock(fromInventory.getQuantityOnHand())
                .reason(transferReason)
                .performedBy(user)
                .build();
        
        StockMovement inMovement = StockMovement.builder()
                .inventory(toInventory)
                .movementType(MovementType.TRANSFERRED_IN)
                .quantity(request.getQuantity())
                .previousStock(toPreviousStock)
                .newStock(toInventory.getQuantityOnHand())
                .reason("Transfer from " + fromRdc.getName())
                .performedBy(user)
                .build();
        
        stockMovementRepository.save(outMovement);
        stockMovementRepository.save(inMovement);
        
        log.info("Stock transfer completed successfully");
    }
    
    /**
     * Get stock movement history
     */
    @Transactional(readOnly = true)
    public List<StockMovementResponse> getStockMovementHistory(Long inventoryId) {
        log.info("Fetching stock movement history for inventory: {}", inventoryId);
        
        List<StockMovement> movements = stockMovementRepository
                .findByInventory_InventoryIdOrderByTimestampDesc(inventoryId);
        
        return movements.stream()
                .map(this::mapMovementToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get all movements for an RDC
     */
    @Transactional(readOnly = true)
    public List<StockMovementResponse> getRdcMovements(Long rdcId) {
        log.info("Fetching all stock movements for RDC: {}", rdcId);
        
        List<StockMovement> movements = stockMovementRepository.findByRdcIdOrderByTimestampDesc(rdcId);
        
        return movements.stream()
                .map(this::mapMovementToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Calculate new stock based on movement type
     */
    private int calculateNewStock(int currentStock, MovementType movementType, int quantity) {
        return switch (movementType) {
            case RECEIVED, RETURNED, TRANSFERRED_IN, ADJUSTMENT -> currentStock + quantity;
            case SOLD, DAMAGED, TRANSFERRED_OUT -> currentStock - quantity;
        };
    }
    
    /**
     * Get stock status
     */
    private String getStockStatus(Inventory inventory) {
        if (inventory.getQuantityOnHand() == 0) {
            return "OUT_OF_STOCK";
        } else if (inventory.getQuantityOnHand() <= inventory.getReorderLevel()) {
            return "LOW_STOCK";
        } else {
            return "OK";
        }
    }
    
    /**
     * Map Inventory to InventoryResponse
     */
    private InventoryResponse mapToResponse(Inventory inventory) {
        return InventoryResponse.builder()
                .inventoryId(inventory.getInventoryId())
                .productId(inventory.getProduct().getProductId())
                .productName(inventory.getProduct().getName())
                .productSku(inventory.getProduct().getSku())
                .rdcId(inventory.getRdc().getRdcId())
                .rdcName(inventory.getRdc().getName())
                .quantityOnHand(inventory.getQuantityOnHand())
                .reorderLevel(inventory.getReorderLevel())
                .status(getStockStatus(inventory))
                .lastUpdated(inventory.getLastUpdated())
                .build();
    }
    
    /**
     * Map StockMovement to StockMovementResponse
     */
    private StockMovementResponse mapMovementToResponse(StockMovement movement) {
        return StockMovementResponse.builder()
                .movementId(movement.getMovementId())
                .inventoryId(movement.getInventory().getInventoryId())
                .productName(movement.getInventory().getProduct().getName())
                .rdcName(movement.getInventory().getRdc().getName())
                .movementType(movement.getMovementType())
                .quantity(movement.getQuantity())
                .previousStock(movement.getPreviousStock())
                .newStock(movement.getNewStock())
                .reason(movement.getReason())
                .performedBy(movement.getPerformedBy().getUsername())
                .timestamp(movement.getTimestamp())
                .build();
    }
}
```

---

### 5. Controller - InventoryController

**File: `src/main/java/com/isdn/controller/InventoryController.java`**
```java
package com.isdn.controller;

import com.isdn.dto.request.StockTransferRequest;
import com.isdn.dto.request.StockUpdateRequest;
import com.isdn.dto.response.ApiResponse;
import com.isdn.dto.response.InventoryResponse;
import com.isdn.dto.response.StockMovementResponse;
import com.isdn.model.User;
import com.isdn.repository.UserRepository;
import com.isdn.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class InventoryController {
    
    private final InventoryService inventoryService;
    private final UserRepository userRepository;
    
    /**
     * GET /api/inventory/rdc/{rdcId} - Get all inventory for an RDC
     */
    @GetMapping("/rdc/{rdcId}")
    @PreAuthorize("hasAnyRole('RDC_STAFF', 'LOGISTICS_OFFICER', 'HO_MANAGER', 'ADMIN')")
    public ResponseEntity<List<InventoryResponse>> getInventoryByRdc(@PathVariable Long rdcId) {
        log.info("GET /api/inventory/rdc/{} - Fetch inventory", rdcId);
        List<InventoryResponse> inventory = inventoryService.getInventoryByRdc(rdcId);
        return ResponseEntity.ok(inventory);
    }
    
    /**
     * GET /api/inventory/rdc/{rdcId}/low-stock - Get low stock items
     */
    @GetMapping("/rdc/{rdcId}/low-stock")
    @PreAuthorize("hasAnyRole('RDC_STAFF', 'LOGISTICS_OFFICER', 'HO_MANAGER', 'ADMIN')")
    public ResponseEntity<List<InventoryResponse>> getLowStockItems(@PathVariable Long rdcId) {
        log.info("GET /api/inventory/rdc/{}/low-stock - Fetch low stock items", rdcId);
        List<InventoryResponse> lowStock = inventoryService.getLowStockItems(rdcId);
        return ResponseEntity.ok(lowStock);
    }
    
    /**
     * PUT /api/inventory/{inventoryId}/update - Update stock
     */
    @PutMapping("/{inventoryId}/update")
    @PreAuthorize("hasAnyRole('RDC_STAFF', 'HO_MANAGER', 'ADMIN')")
    public ResponseEntity<InventoryResponse> updateStock(
            @PathVariable Long inventoryId,
            @Valid @RequestBody StockUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("PUT /api/inventory/{}/update - Update stock", inventoryId);
        Long userId = getUserId(userDetails);
        InventoryResponse inventory = inventoryService.updateStock(inventoryId, request, userId);
        return ResponseEntity.ok(inventory);
    }
    
    /**
     * POST /api/inventory/transfer - Transfer stock between RDCs
     */
    @PostMapping("/transfer")
    @PreAuthorize("hasAnyRole('LOGISTICS_OFFICER', 'HO_MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse> transferStock(
            @Valid @RequestBody StockTransferRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("POST /api/inventory/transfer - Transfer stock");
        Long userId = getUserId(userDetails);
        inventoryService.transferStock(request, userId);
        
        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Stock transferred successfully")
                .build();
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/inventory/{inventoryId}/movements - Get stock movement history
     */
    @GetMapping("/{inventoryId}/movements")
    @PreAuthorize("hasAnyRole('RDC_STAFF', 'LOGISTICS_OFFICER', 'HO_MANAGER', 'ADMIN')")
    public ResponseEntity<List<StockMovementResponse>> getStockMovementHistory(
            @PathVariable Long inventoryId) {
        log.info("GET /api/inventory/{}/movements - Fetch movement history", inventoryId);
        List<StockMovementResponse> movements = inventoryService.getStockMovementHistory(inventoryId);
        return ResponseEntity.ok(movements);
    }
    
    /**
     * GET /api/inventory/rdc/{rdcId}/movements - Get all movements for an RDC
     */
    @GetMapping("/rdc/{rdcId}/movements")
    @PreAuthorize("hasAnyRole('RDC_STAFF', 'LOGISTICS_OFFICER', 'HO_MANAGER', 'ADMIN')")
    public ResponseEntity<List<StockMovementResponse>> getRdcMovements(@PathVariable Long rdcId) {
        log.info("GET /api/inventory/rdc/{}/movements - Fetch all movements", rdcId);
        List<StockMovementResponse> movements = inventoryService.getRdcMovements(rdcId);
        return ResponseEntity.ok(movements);
    }
    
    /**
     * Helper method to get user ID
     */
    private Long getUserId(UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getUserId();
    }
}
```

---

## FRONTEND IMPLEMENTATION

### 1. Inventory Service

**File: `src/services/inventoryService.js`**
```javascript
import api from './api';

/**
 * Get inventory for an RDC
 */
export const getInventoryByRdc = async (rdcId) => {
    const response = await api.get(`/inventory/rdc/${rdcId}`);
    return response.data;
};

/**
 * Get low stock items for an RDC
 */
export const getLowStockItems = async (rdcId) => {
    const response = await api.get(`/inventory/rdc/${rdcId}/low-stock`);
    return response.data;
};

/**
 * Update stock
 */
export const updateStock = async (inventoryId, updateData) => {
    const response = await api.put(`/inventory/${inventoryId}/update`, updateData);
    return response.data;
};

/**
 * Transfer stock between RDCs
 */
export const transferStock = async (transferData) => {
    const response = await api.post('/inventory/transfer', transferData);
    return response.data;
};

/**
 * Get stock movement history
 */
export const getStockMovementHistory = async (inventoryId) => {
    const response = await api.get(`/inventory/${inventoryId}/movements`);
    return response.data;
};

/**
 * Get all movements for an RDC
 */
export const getRdcMovements = async (rdcId) => {
    const response = await api.get(`/inventory/rdc/${rdcId}/movements`);
    return response.data;
};
```

---

### 2. Inventory Components

**File: `src/components/inventory/InventoryList.jsx`**
```javascript
import React, { useState, useEffect } from 'react';
import * as inventoryService from '../../services/inventoryService';
import StockUpdateModal from './StockUpdateModal';
import StockMovementModal from './StockMovementModal';
import Loader from '../common/Loader';

export default function InventoryList() {
    const [inventory, setInventory] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [selectedInventory, setSelectedInventory] = useState(null);
    const [showUpdateModal, setShowUpdateModal] = useState(false);
    const [showMovementModal, setShowMovementModal] = useState(false);
    const [filterStatus, setFilterStatus] = useState('ALL');
    
    // Hardcoded RDC ID - In real app, get from user context
    const rdcId = 4; // Western RDC

    useEffect(() => {
        loadInventory();
    }, []);

    const loadInventory = async () => {
        try {
            setLoading(true);
            const data = await inventoryService.getInventoryByRdc(rdcId);
            setInventory(data);
            setError(null);
        } catch (err) {
            setError('Failed to load inventory');
            console.error('Error loading inventory:', err);
        } finally {
            setLoading(false);
        }
    };

    const handleUpdateStock = (item) => {
        setSelectedInventory(item);
        setShowUpdateModal(true);
    };

    const handleViewMovements = (item) => {
        setSelectedInventory(item);
        setShowMovementModal(true);
    };

    const handleUpdateComplete = () => {
        setShowUpdateModal(false);
        loadInventory();
    };

    const getStatusColor = (status) => {
        const colors = {
            'OK': 'bg-green-100 text-green-800',
            'LOW_STOCK': 'bg-yellow-100 text-yellow-800',
            'OUT_OF_STOCK': 'bg-red-100 text-red-800'
        };
        return colors[status] || 'bg-gray-100 text-gray-800';
    };

    const filteredInventory = inventory.filter(item => {
        if (filterStatus === 'ALL') return true;
        return item.status === filterStatus;
    });

    if (loading) return <Loader />;

    return (
        <div className="container mx-auto px-4 py-8">
            <div className="flex items-center justify-between mb-8">
                <div>
                    <h1 className="text-3xl font-bold text-gray-900">Inventory Management</h1>
                    <p className="text-gray-600">Western RDC</p>
                </div>
                <button
                    onClick={loadInventory}
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

            <div className="bg-white rounded-lg shadow-md p-6 mb-6">
                <div className="flex gap-4">
                    <button
                        onClick={() => setFilterStatus('ALL')}
                        className={`px-4 py-2 rounded-lg transition ${
                            filterStatus === 'ALL' 
                                ? 'bg-blue-600 text-white' 
                                : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                        }`}
                    >
                        All Items
                    </button>
                    <button
                        onClick={() => setFilterStatus('LOW_STOCK')}
                        className={`px-4 py-2 rounded-lg transition ${
                            filterStatus === 'LOW_STOCK' 
                                ? 'bg-yellow-600 text-white' 
                                : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                        }`}
                    >
                        Low Stock
                    </button>
                    <button
                        onClick={() => setFilterStatus('OUT_OF_STOCK')}
                        className={`px-4 py-2 rounded-lg transition ${
                            filterStatus === 'OUT_OF_STOCK' 
                                ? 'bg-red-600 text-white' 
                                : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                        }`}
                    >
                        Out of Stock
                    </button>
                </div>
            </div>

            <div className="bg-white rounded-lg shadow-md overflow-hidden">
                <table className="min-w-full divide-y divide-gray-200">
                    <thead className="bg-gray-50">
                        <tr>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                Product
                            </th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                SKU
                            </th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                Stock
                            </th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                Reorder Level
                            </th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                Status
                            </th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                Last Updated
                            </th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                Actions
                            </th>
                        </tr>
                    </thead>
                    <tbody className="bg-white divide-y divide-gray-200">
                        {filteredInventory.map(item => (
                            <tr key={item.inventoryId} className="hover:bg-gray-50">
                                <td className="px-6 py-4 whitespace-nowrap">
                                    <div className="text-sm font-medium text-gray-900">
                                        {item.productName}
                                    </div>
                                </td>
                                <td className="px-6 py-4 whitespace-nowrap">
                                    <div className="text-sm text-gray-500">{item.productSku}</div>
                                </td>
                                <td className="px-6 py-4 whitespace-nowrap">
                                    <div className="text-sm font-semibold text-gray-900">
                                        {item.quantityOnHand} units
                                    </div>
                                </td>
                                <td className="px-6 py-4 whitespace-nowrap">
                                    <div className="text-sm text-gray-500">
                                        {item.reorderLevel} units
                                    </div>
                                </td>
                                <td className="px-6 py-4 whitespace-nowrap">
                                    <span className={`px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full ${getStatusColor(item.status)}`}>
                                        {item.status.replace('_', ' ')}
                                    </span>
                                </td>
                                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                    {new Date(item.lastUpdated).toLocaleString()}
                                </td>
                                <td className="px-6 py-4 whitespace-nowrap text-sm font-medium space-x-2">
                                    <button
                                        onClick={() => handleUpdateStock(item)}
                                        className="text-blue-600 hover:text-blue-900"
                                    >
                                        Update
                                    </button>
                                    <button
                                        onClick={() => handleViewMovements(item)}
                                        className="text-gray-600 hover:text-gray-900"
                                    >
                                        History
                                    </button>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>

                {filteredInventory.length === 0 && (
                    <div className="text-center py-12 text-gray-500">
                        No inventory items found
                    </div>
                )}
            </div>

            {showUpdateModal && selectedInventory && (
                <StockUpdateModal
                    inventory={selectedInventory}
                    onClose={() => setShowUpdateModal(false)}
                    onSuccess={handleUpdateComplete}
                />
            )}

            {showMovementModal && selectedInventory && (
                <StockMovementModal
                    inventory={selectedInventory}
                    onClose={() => setShowMovementModal(false)}
                />
            )}
        </div>
    );
}
```

**File: `src/components/inventory/StockUpdateModal.jsx`**
```javascript
import React, { useState } from 'react';
import * as inventoryService from '../../services/inventoryService';

export default function StockUpdateModal({ inventory, onClose, onSuccess }) {
    const [formData, setFormData] = useState({
        movementType: 'RECEIVED',
        quantity: '',
        reason: ''
    });
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

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
            await inventoryService.updateStock(inventory.inventoryId, {
                ...formData,
                quantity: parseInt(formData.quantity)
            });
            onSuccess();
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to update stock');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white rounded-lg shadow-xl p-6 max-w-md w-full mx-4">
                <div className="flex items-center justify-between mb-4">
                    <h2 className="text-xl font-bold text-gray-900">Update Stock</h2>
                    <button
                        onClick={onClose}
                        className="text-gray-400 hover:text-gray-600"
                    >
                        <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                        </svg>
                    </button>
                </div>

                <div className="mb-4 p-4 bg-gray-50 rounded">
                    <p className="text-sm text-gray-600">Product</p>
                    <p className="font-semibold text-gray-900">{inventory.productName}</p>
                    <p className="text-sm text-gray-600 mt-2">Current Stock</p>
                    <p className="font-semibold text-gray-900">{inventory.quantityOnHand} units</p>
                </div>

                {error && (
                    <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
                        {error}
                    </div>
                )}

                <form onSubmit={handleSubmit} className="space-y-4">
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            Movement Type
                        </label>
                        <select
                            name="movementType"
                            value={formData.movementType}
                            onChange={handleChange}
                            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                        >
                            <option value="RECEIVED">Stock Received</option>
                            <option value="SOLD">Sold to Customer</option>
                            <option value="DAMAGED">Damaged/Expired</option>
                            <option value="RETURNED">Customer Return</option>
                            <option value="ADJUSTMENT">Stock Adjustment</option>
                        </select>
                    </div>

                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            Quantity
                        </label>
                        <input
                            type="number"
                            name="quantity"
                            required
                            min="1"
                            value={formData.quantity}
                            onChange={handleChange}
                            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                            placeholder="Enter quantity"
                        />
                    </div>

                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            Reason (Optional)
                        </label>
                        <textarea
                            name="reason"
                            rows={3}
                            value={formData.reason}
                            onChange={handleChange}
                            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                            placeholder="Enter reason for stock update"
                        />
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
                            {loading ? 'Updating...' : 'Update Stock'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}
```

**File: `src/components/inventory/StockMovementModal.jsx`**
```javascript
import React, { useState, useEffect } from 'react';
import * as inventoryService from '../../services/inventoryService';
import Loader from '../common/Loader';

export default function StockMovementModal({ inventory, onClose }) {
    const [movements, setMovements] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        loadMovements();
    }, []);

    const loadMovements = async () => {
        try {
            setLoading(true);
            const data = await inventoryService.getStockMovementHistory(inventory.inventoryId);
            setMovements(data);
        } catch (err) {
            console.error('Error loading movements:', err);
        } finally {
            setLoading(false);
        }
    };

    const getMovementTypeColor = (type) => {
        const colors = {
            'RECEIVED': 'text-green-600',
            'SOLD': 'text-blue-600',
            'DAMAGED': 'text-red-600',
            'RETURNED': 'text-purple-600',
            'TRANSFERRED_OUT': 'text-orange-600',
            'TRANSFERRED_IN': 'text-indigo-600',
            'ADJUSTMENT': 'text-gray-600'
        };
        return colors[type] || 'text-gray-600';
    };

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white rounded-lg shadow-xl p-6 max-w-4xl w-full mx-4 max-h-[80vh] overflow-y-auto">
                <div className="flex items-center justify-between mb-4">
                    <div>
                        <h2 className="text-xl font-bold text-gray-900">Stock Movement History</h2>
                        <p className="text-gray-600">{inventory.productName}</p>
                    </div>
                    <button
                        onClick={onClose}
                        className="text-gray-400 hover:text-gray-600"
                    >
                        <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                        </svg>
                    </button>
                </div>

                {loading ? (
                    <div className="flex justify-center py-12">
                        <div className="animate-spin rounded-full h-12 w-12 border-b-4 border-blue-600"></div>
                    </div>
                ) : movements.length === 0 ? (
                    <div className="text-center py-12 text-gray-500">
                        No movement history found
                    </div>
                ) : (
                    <div className="space-y-4">
                        {movements.map(movement => (
                            <div key={movement.movementId} className="border border-gray-200 rounded-lg p-4">
                                <div className="flex justify-between items-start mb-2">
                                    <div>
                                        <span className={`font-semibold ${getMovementTypeColor(movement.movementType)}`}>
                                            {movement.movementType.replace('_', ' ')}
                                        </span>
                                        <p className="text-sm text-gray-600 mt-1">
                                            {new Date(movement.timestamp).toLocaleString()}
                                        </p>
                                    </div>
                                    <div className="text-right">
                                        <p className="text-sm text-gray-600">Quantity</p>
                                        <p className="font-semibold text-lg">{movement.quantity}</p>
                                    </div>
                                </div>

                                <div className="grid grid-cols-2 gap-4 mt-3">
                                    <div>
                                        <p className="text-sm text-gray-600">Previous Stock</p>
                                        <p className="font-medium">{movement.previousStock} units</p>
                                    </div>
                                    <div>
                                        <p className="text-sm text-gray-600">New Stock</p>
                                        <p className="font-medium">{movement.newStock} units</p>
                                    </div>
                                </div>

                                {movement.reason && (
                                    <div className="mt-3">
                                        <p className="text-sm text-gray-600">Reason</p>
                                        <p className="text-gray-900">{movement.reason}</p>
                                    </div>
                                )}

                                <div className="mt-3 pt-3 border-t border-gray-200">
                                    <p className="text-sm text-gray-600">
                                        Performed by: <span className="font-medium">{movement.performedBy}</span>
                                    </p>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
}
```

---

### 3. Add Route to App.jsx

**Update: `src/App.jsx`**
Add this import:
```javascript
import InventoryList from './components/inventory/InventoryList';
```

Add this route:
```javascript
<Route path="/inventory" element={
    <ProtectedRoute>
        <InventoryList />
    </ProtectedRoute>
} />
```

---

### 4. Add Link to Navbar

**Update: `src/components/common/Navbar.jsx`**
Add inventory link for RDC staff:
```javascript
{(user?.role === 'RDC_STAFF' || user?.role === 'HO_MANAGER' || user?.role === 'ADMIN') && (
    <Link to="/inventory" className="hover:text-blue-200 transition">
        Inventory
    </Link>
)}
```

---

## TESTING FEATURE 2

### Test Users
```
Username: rdc_staff1
Password: password123
Role: RDC_STAFF

Username: admin
Password: password123
Role: ADMIN
```

### Test Steps
1. Login as RDC staff
2. Navigate to Inventory
3. View all inventory items
4. Filter by Low Stock
5. Click "Update" on any item
6. Add stock (Movement Type: RECEIVED)
7. View History to see movement recorded
8. Try different movement types

---

END OF FEATURE 2 IMPLEMENTATION