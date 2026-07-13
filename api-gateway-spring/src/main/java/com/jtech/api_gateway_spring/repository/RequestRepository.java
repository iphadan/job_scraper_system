package com.jtech.api_gateway_spring.repository;

import com.jtech.api_gateway_spring.model.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {

    // 🌟 Clean database-level indexed lookup replacing memory-heavy Java stream filters
    List<Request> findByUsernameIgnoreCase(String username);
}