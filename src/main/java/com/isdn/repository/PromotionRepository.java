package com.isdn.repository;

import com.isdn.model.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    List<Promotion> findByActiveTrue();

    @Query("SELECT p FROM Promotion p WHERE p.active = true AND CURRENT_DATE BETWEEN p.startDate AND p.endDate")
    List<Promotion> findActivePromotions();

    @Query("SELECT p FROM Promotion p WHERE p.endDate < :date")
    List<Promotion> findExpiredPromotions(LocalDate date);
}
