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
