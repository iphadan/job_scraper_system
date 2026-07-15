package com.jtech.api_gateway_spring.repository;

import com.jtech.api_gateway_spring.model.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {

    // 🌟 Clean database-level indexed lookup replacing memory-heavy Java stream filters
    List<Request> findByUsernameIgnoreCase(String username);

    // 🌟 Optimized fetch join to get requests and their associated static target sites in one query
    @Query("SELECT DISTINCT r FROM Request r LEFT JOIN FETCH r.targetSites WHERE r.status = 'ACTIVE'")
    List<Request> findAllActiveRequestsWithSites();
}