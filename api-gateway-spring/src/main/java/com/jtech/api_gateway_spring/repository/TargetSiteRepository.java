package com.jtech.api_gateway_spring.repository;

import com.jtech.api_gateway_spring.model.TargetSite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TargetSiteRepository extends JpaRepository<TargetSite, Long> {

    // Hand-coded execution check specified in section 5 of documentation [cite: 63, 64]
    @Query(value = "SELECT s.* FROM target_sites s JOIN requests r ON s.request_id = r.id WHERE r.status = 'ACTIVE'", nativeQuery = true)
    List<TargetSite> findAllActiveSubscriptionTargets();
    Optional<TargetSite> findBySiteCode(String siteCode);
}