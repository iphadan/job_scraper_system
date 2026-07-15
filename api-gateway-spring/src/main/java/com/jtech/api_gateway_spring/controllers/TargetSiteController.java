package com.jtech.api_gateway_spring.controllers;

import com.jtech.api_gateway_spring.model.TargetSite;
import com.jtech.api_gateway_spring.repository.TargetSiteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/target-sites")
@RequiredArgsConstructor
@Slf4j
public class TargetSiteController {

    private final TargetSiteRepository targetSiteRepository;

    /**
     * Public/Authenticated lookup to retrieve all active scrapers in our system catalog
     */
    @GetMapping
    public ResponseEntity<List<TargetSite>> getSupportedSites() {
        log.info("Fetching supported target sites database directory.");
        List<TargetSite> sites = targetSiteRepository.findAll();
        return ResponseEntity.ok(sites);
    }
}