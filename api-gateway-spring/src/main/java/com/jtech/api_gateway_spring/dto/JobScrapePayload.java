package com.jtech.api_gateway_spring.dto;

import java.io.Serializable;
import java.util.List;

public record JobScrapePayload(
        Long requestId,
        String siteCode, // Matches "REMOTEOK", "WWR", etc.
        String targetUrl,
        List<String> keywords
) implements Serializable {}