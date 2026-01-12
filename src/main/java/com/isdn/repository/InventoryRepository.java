package com.isdn.repository;

import com.isdn.model.Inventory;
import com.isdn.model.Product;
import com.isdn.model.RDC;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByProductAndRdc(Product product, RDC rdc);

    Optional<Inventory> findByProduct_ProductIdAndRdc_RdcId(Long productId, Long rdcId);

    List<Inventory> findByProduct(Product product);

    List<Inventory> findByProduct_ProductId(Long productId);

    List<Inventory> findByRdc(RDC rdc);

    List<Inventory> findByRdc_RdcId(Long rdcId);

    @Query("SELECT i FROM Inventory i WHERE i.quantityOnHand <= i.reorderLevel")
    List<Inventory> findLowStockItems();

    @Query("SELECT i FROM Inventory i WHERE i.rdc.rdcId = :rdcId AND i.quantityOnHand <= i.reorderLevel")
    List<Inventory> findLowStockItemsByRdc(@Param("rdcId") Long rdcId);

    @Query("SELECT SUM(i.quantityOnHand) FROM Inventory i WHERE i.product.productId = :productId")
    Integer getTotalStockForProduct(@Param("productId") Long productId);
}