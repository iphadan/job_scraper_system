package com.jtech.api_gateway_spring.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "target_sites")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TargetSite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "site_code", nullable = false, unique = true, length = 50)
    private String siteCode; // e.g., "REMOTEOK"

    @Column(name = "short_name", nullable = false, length = 100)
    private String shortName; // e.g., "RemoteOK API"

    @Column(nullable = false, columnDefinition = "TEXT")
    private String url; // base or scraping entry API point
}