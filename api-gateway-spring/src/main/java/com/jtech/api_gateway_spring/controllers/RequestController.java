package com.jtech.api_gateway_spring.controllers;

import com.jtech.api_gateway_spring.model.Request;
import com.jtech.api_gateway_spring.service.RequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
@Slf4j
public class RequestController {

    private final RequestService requestService;

    /**
     * Read Endpoint: Fetches active tracking strategies belonging strictly to the logged-in user
     */
    @GetMapping
    public ResponseEntity<List<Request>> getAllUserRequests() {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Controller routing fetch request for user '{}'", currentUsername);

        List<Request> userRequests = requestService.getRequestsForUser(currentUsername);
        return ResponseEntity.ok(userRequests);
    }

    /**
     * Create Endpoint: Persists a new tracking strategy bound securely to the active session user
     */
    @PostMapping
    public ResponseEntity<Request> createRequest(@RequestBody Request requestPayload) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Controller routing save request for user '{}'", currentUsername);

        try {
            Request savedRequest = requestService.createRequest(requestPayload, currentUsername);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedRequest);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid target site specified: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}