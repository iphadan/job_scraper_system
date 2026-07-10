package com.jtech.api_gateway_spring.dto;

import java.util.List;

public record JobScrapePayload(
    Long requestId,
    Long siteId,
    String url,
    List<String> keywords
) {}