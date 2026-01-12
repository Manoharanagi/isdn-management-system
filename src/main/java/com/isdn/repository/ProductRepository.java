package com.isdn.repository;

import com.isdn.model.Category;
import com.isdn.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    List<Product> findByActiveTrueOrderByNameAsc();

    List<Product> findByCategoryAndActiveTrue(Category category);

    List<Product> findByNameContainingIgnoreCaseAndActiveTrue(String keyword);

    @Query("SELECT p FROM Product p WHERE " +
            "p.active = true AND " +
            "(:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            " LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:category IS NULL OR p.category = :category) AND " +
            "(:minPrice IS NULL OR p.unitPrice >= :minPrice) AND " +
            "(:maxPrice IS NULL OR p.unitPrice <= :maxPrice) " +
            "ORDER BY p.name ASC")
    List<Product> searchProducts(
            @Param("keyword") String keyword,
            @Param("category") Category category,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice
    );

    @Query("SELECT DISTINCT p FROM Product p " +
            "JOIN p.inventories i " +
            "WHERE p.active = true " +
            "GROUP BY p " +
            "HAVING SUM(i.quantityOnHand) > 0")
    List<Product> findAvailableProducts();

    @Query("SELECT DISTINCT p FROM Product p " +
            "JOIN Promotion pr ON p MEMBER OF pr.products " +
            "WHERE p.active = true AND pr.active = true " +
            "AND CURRENT_DATE BETWEEN pr.startDate AND pr.endDate")
    List<Product> findProductsOnPromotion();

    Boolean existsBySku(String sku);
}