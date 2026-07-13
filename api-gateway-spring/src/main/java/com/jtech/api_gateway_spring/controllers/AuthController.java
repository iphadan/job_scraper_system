package com.jtech.api_gateway_spring.controllers;

import com.jtech.api_gateway_spring.dto.JwtAuthenticationResponse;
import com.jtech.api_gateway_spring.dto.LoginRequest;
import com.jtech.api_gateway_spring.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenProvider tokenProvider;

    @PostMapping("/login")
    public ResponseEntity<JwtAuthenticationResponse> authenticateUser(@RequestBody LoginRequest loginRequest) {
        
        // 1. Directory Verification (Stubbed loop for corporate AD verification)
        // In a real environment, this hooks into your secure LDAP manager.
        boolean isAuthenticated = mockDirectoryCheck(loginRequest.getUsername(), loginRequest.getPassword());
        
        if (!isAuthenticated) {
            return ResponseEntity.status(401).build(); // Unauthorized entry
        }

        // 2. Cryptographic Minting
        String jwt = tokenProvider.generateToken(loginRequest.getUsername(), Collections.singletonList("ROLE_USER"));
        
        // 3. Return payload back across the security boundary to React UI
        return ResponseEntity.ok(new JwtAuthenticationResponse(jwt));
    }

    private boolean mockDirectoryCheck(String username, String password) {
        // Simple mock authentication for local verification
        return "admin".equalsIgnoreCase(username) && "password".equals(password);
    }
}