package com.isdn.repository;

import com.isdn.model.RDC;
import com.isdn.model.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RDCRepository extends JpaRepository<RDC, Long> {

    Optional<RDC> findByName(String name);

    List<RDC> findByRegion(Region region);

    List<RDC> findByActiveTrue();

    Boolean existsByName(String name);
}