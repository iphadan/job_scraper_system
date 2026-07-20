package com.jtech.api_gateway_spring.controllers;

import com.jtech.api_gateway_spring.model.ScrapedJob;
import com.jtech.api_gateway_spring.repository.ScrapedJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ScrapedJobController {

    private final ScrapedJobRepository scrapedJobRepository;

    @GetMapping
    public List<ScrapedJob> getAllJobs() {
        return scrapedJobRepository.findAllByOrderByScrapedAtDesc();
    }
}