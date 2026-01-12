package com.isdn.repository;

import com.isdn.model.Order;
import com.isdn.model.OrderStatus;
import com.isdn.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findByUserOrderByOrderDateDesc(User user);

    List<Order> findByUser_UserIdOrderByOrderDateDesc(Long userId);

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByStatusOrderByOrderDateDesc(OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.user.userId = :userId AND o.status = :status ORDER BY o.orderDate DESC")
    List<Order> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate ORDER BY o.orderDate DESC")
    List<Order> findOrdersBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    Page<Order> findByUser_UserId(Long userId, Pageable pageable);

    Boolean existsByOrderNumber(String orderNumber);

    Long countByStatus(OrderStatus status);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.user.userId = :userId")
    Long countByUserId(@Param("userId") Long userId);

    // Feature 4: Order Management methods
    List<Order> findAllByOrderByOrderDateDesc();

    @Query("SELECT o FROM Order o WHERE o.rdc = :rdc ORDER BY o.orderDate DESC")
    List<Order> findByRdcOrderByOrderDateDesc(@Param("rdc") com.isdn.model.RDC rdc);

    @Query("SELECT o FROM Order o WHERE o.status IN :statuses ORDER BY o.orderDate DESC")
    List<Order> findByStatusIn(@Param("statuses") List<OrderStatus> statuses);
}