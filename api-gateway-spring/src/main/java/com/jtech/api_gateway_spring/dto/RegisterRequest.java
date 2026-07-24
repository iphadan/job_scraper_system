package com.jtech.api_gateway_spring.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String email;
    private String password;
    private String fullName;
    private String preferredKeywords; // e.g. "DevOps, Java, Integration"
}