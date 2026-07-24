package com.jtech.api_gateway_spring.controllers;

import com.jtech.api_gateway_spring.dto.JwtAuthenticationResponse;
import com.jtech.api_gateway_spring.dto.LoginRequest;
import com.jtech.api_gateway_spring.dto.RegisterRequest;
import com.jtech.api_gateway_spring.model.User;
import com.jtech.api_gateway_spring.repository.UserRepository;
import com.jtech.api_gateway_spring.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email already exists!"));
        }

        User newUser = new User();
        newUser.setEmail(request.getEmail());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setFullName(request.getFullName());
        newUser.setPreferredKeywords(request.getPreferredKeywords());

        userRepository.save(newUser);
        return ResponseEntity.ok(Map.of("message", "User registered successfully!"));
    }
//    @PostMapping("/login")
//    public ResponseEntity<JwtAuthenticationResponse> authenticateUser(@RequestBody LoginRequest loginRequest) {
//
//        // 1. Directory Verification (Stubbed loop for corporate AD verification)
//        // In a real environment, this hooks into your secure LDAP manager.
//        boolean isAuthenticated = mockDirectoryCheck(loginRequest.getUsername(), loginRequest.getPassword());
//
//        if (!isAuthenticated) {
//            return ResponseEntity.status(401).build(); // Unauthorized entry
//        }
//
//        // 2. Cryptographic Minting
//        String jwt = tokenProvider.generateToken(loginRequest.getUsername(), Collections.singletonList("ROLE_USER"));
//
//        // 3. Return payload back across the security boundary to React UI
//        return ResponseEntity.ok(new JwtAuthenticationResponse(jwt));
//    }
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());

        if (userOpt.isPresent() && passwordEncoder.matches(request.getPassword(), userOpt.get().getPassword())) {
            User user = userOpt.get();
            // Simple response payload (you can upgrade to JWT token if needed)
            String jwt = tokenProvider.generateToken(request.getEmail(), Collections.singletonList("ROLE_USER"));

            return ResponseEntity.ok(new JwtAuthenticationResponse(jwt));

        }

        return ResponseEntity.status(401).body(Map.of("message", "Invalid email or password"));
    }
    private boolean mockDirectoryCheck(String username, String password) {
        // Simple mock authentication for local verification
        return "admin".equalsIgnoreCase(username) && "password".equals(password);
    }
}