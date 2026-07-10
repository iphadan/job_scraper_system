package com.jtech.api_gateway_spring.security;

import com.jtech.api_gateway_spring.model.Request;
import com.jtech.api_gateway_spring.repository.RequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("resourceSecurity")
@RequiredArgsConstructor
public class ResourceSecurityEvaluator {

    private final RequestRepository requestRepository;

    /**
     * Mathematical Alignment Check: Ensures verified JWT claim matches the DB entity owner
     */
    public boolean isOwner(Long requestId) {
        // Extract identity string securely from local execution Context Thread
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        
        return requestRepository.findById(requestId)
                .map(Request::getUsername)
                .map(owner -> owner.equalsIgnoreCase(currentUsername))
                .orElse(false); // If the entity doesn't exist, reject access immediately
    }
}