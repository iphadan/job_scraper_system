package com.jtech.api_gateway_spring.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "scraped_jobs", indexes = {
    @Index(name = "idx_request_id", columnList = "request_id"),
    @Index(name = "idx_unique_url", columnList = "url", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScrapedJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id", nullable = false)
    private Long requestId; // Links back to the user's Request ID

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 150)
    private String company;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String url; // Unique job link to prevent duplicate entries

    @Column(columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(name = "scraped_at")
    private LocalDateTime scrapedAt;
}