package com.jtech.api_gateway_spring.service;

import com.jtech.api_gateway_spring.dto.JobScrapePayload;
import com.jtech.api_gateway_spring.model.Request;
import com.jtech.api_gateway_spring.model.TargetSite;
import com.jtech.api_gateway_spring.repository.RequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobOrchestrationScheduler {

    // 🌟 Switch dependency to RequestRepository
    private final RequestRepository requestRepository;
    private final JmsTemplate jmsTemplate;

    private static final String QUEUE_NAME = "job.scrape.queue";

    @Scheduled(cron = "${app.scheduler.cron:0 */15 * * * *}") // Defaults to every 15 minutes
    @Transactional(readOnly = true) // Keeps Hibernate session open to lazy-load collections if needed
    public void executeCoreTick() {
        log.info("Initiating system core orchestration tick...");

        // 1. Pull active subscription requests
        List<Request> activeRequests = requestRepository.findAllActiveRequestsWithSites();
        log.info("Aggregated {} active user requests for processing.", activeRequests.size());

        // 2. Map and split workloads
        for (Request request : activeRequests) {
            // Split comma-separated keywords
            List<String> keywordList = Arrays.asList(request.getKeywords().split("\\s*,\\s*"));

            // Dispatch a payload for every targeted site linked to this request
            for (TargetSite target : request.getTargetSites()) {

                // Construct the updated payload structure
                JobScrapePayload payload = new JobScrapePayload(
                        request.getId(),
                        target.getSiteCode(), // Use clean site code (e.g. "REMOTEOK") instead of auto-incremented target IDs
                        target.getUrl(),      // Base URL/API endpoint
                        keywordList
                );

                // 3. Stream to ActiveMQ
                try {
                    jmsTemplate.convertAndSend(QUEUE_NAME, payload);
                    log.debug("Dispatched task to broker: RequestID -> {}, SiteCode -> {}",
                            payload.requestId(), payload.siteCode());
                } catch (Exception e) {
                    log.error("Failed to stream target task to broker for Request ID: {}, Site: {}",
                            request.getId(), target.getSiteCode(), e);
                }
            }
        }
    }
}