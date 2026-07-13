package com.jtech.api_gateway_spring.controllers;

import com.jtech.api_gateway_spring.model.Request;
import com.jtech.api_gateway_spring.repository.RequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
@Slf4j
public class RequestController {

    private final RequestRepository requestRepository;

    /**
     * Read Endpoint: Fetches active tracking strategies belonging strictly to the logged-in user
     */
    @GetMapping
    public ResponseEntity<List<Request>> getAllUserRequests() {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("User '{}' is executing high-performance indexed strategy fetch.", currentUsername);

        // 🌟 Direct database filtering using Postgres index execution
        List<Request> userRequests = requestRepository.findByUsernameIgnoreCase(currentUsername);

        return ResponseEntity.ok(userRequests);
    }

    /**
     * Create Endpoint: Persists a new tracking strategy bound securely to the active session user
     */
    @PostMapping
    public ResponseEntity<Request> createRequest(@RequestBody Request requestPayload) {
        // 1. Secure Identity Extraction (Pulling directly from cryptographically verified token context)
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("User '{}' is initializing a new job scraping strategy.", currentUsername);

        // 2. Enforce Data Isolation Boundary
        // Hardcode the extracted username into the model to prevent payload spoofing
        requestPayload.setUsername(currentUsername);

        // 3. Set Default Status if not provided (e.g., PENDING or ACTIVE)
        if (requestPayload.getStatus() == null || requestPayload.getStatus().isEmpty()) {
            requestPayload.setStatus("ACTIVE");
        }

        // 4. Save to PostgreSQL Database
        Request savedRequest = requestRepository.save(requestPayload);

        return ResponseEntity.status(201).body(savedRequest);
    }
}