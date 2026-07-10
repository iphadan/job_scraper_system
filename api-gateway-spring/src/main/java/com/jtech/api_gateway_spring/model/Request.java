package com.jtech.api_gateway_spring.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "requests", indexes = @Index(name = "idx_username", columnList = "username"))
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String username; // Active Directory owner string mapped from JWT claims [cite: 39]

    @Column(nullable = false, length = 255)
    private String keywords; // Comma-delimited targeting criteria array values [cite: 39]

    @Column(nullable = false, length = 20)
    private String status; // State constraints: ACTIVE or COMPLETED [cite: 39]
@CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TargetSite> targetSites = new ArrayList<>();


}
