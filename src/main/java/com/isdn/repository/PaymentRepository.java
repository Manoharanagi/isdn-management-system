package com.isdn.repository;

import com.isdn.model.Payment;
import com.isdn.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPaymentReference(String paymentReference);

    Optional<Payment> findByPayhereOrderId(String payhereOrderId);

    @Query("SELECT p FROM Payment p WHERE p.order.orderId = :orderId AND p.status = :status")
    Optional<Payment> findByOrderIdAndStatus(@Param("orderId") Long orderId, @Param("status") PaymentStatus status);

    @Query("SELECT p FROM Payment p WHERE p.order.orderId = :orderId AND p.status = 'SUCCESS'")
    Optional<Payment> findSuccessfulPaymentByOrderId(@Param("orderId") Long orderId);

    @Query("SELECT p FROM Payment p WHERE p.order.orderId = :orderId ORDER BY p.createdAt DESC")
    List<Payment> findByOrderIdOrderByCreatedAtDesc(@Param("orderId") Long orderId);

    @Query("SELECT p FROM Payment p WHERE p.user.userId = :userId ORDER BY p.createdAt DESC")
    List<Payment> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    List<Payment> findByStatus(PaymentStatus status);

    @Query("SELECT COUNT(p) > 0 FROM Payment p WHERE p.order.orderId = :orderId AND p.status = 'SUCCESS'")
    boolean existsSuccessfulPaymentForOrder(@Param("orderId") Long orderId);
}
