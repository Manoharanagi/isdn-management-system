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
