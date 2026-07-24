package com.jtech.api_gateway_spring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobNotificationRequest {
    private String jobTitle;
    private String company;
    private String jobUrl;
    private String description;
}