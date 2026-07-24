package com.jtech.api_gateway_spring.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;



    @Column(nullable = false)
    private String password;

    private String fullName;

    // Comma-separated interests or keywords for email alerts
    private String preferredKeywords; 
}