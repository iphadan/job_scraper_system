package com.jtech.api_gateway_spring.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
}