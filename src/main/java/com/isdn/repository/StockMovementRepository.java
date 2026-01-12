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
