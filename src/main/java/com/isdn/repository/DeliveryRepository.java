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
