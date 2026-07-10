package com.jtech.api_gateway_spring.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "target_sites")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TargetSite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false, foreignKey = @ForeignKey(name = "fk_target_sites_requests"))
    private Request request;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String url; // The explicit web address pointer targeted for scraping [cite: 44]

    @Column(name = "last_scraped")
    private LocalDateTime lastScraped; // Tracking previous grid evaluation [cite: 44]
}