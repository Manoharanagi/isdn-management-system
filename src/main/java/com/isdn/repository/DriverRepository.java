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
