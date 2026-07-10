package com.jtech.api_gateway_spring.service;

import com.jtech.api_gateway_spring.dto.JobScrapePayload;
import com.jtech.api_gateway_spring.model.TargetSite;
import com.jtech.api_gateway_spring.repository.TargetSiteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobOrchestrationScheduler {

    private final TargetSiteRepository targetSiteRepository;
    private final JmsTemplate jmsTemplate;
    
    private static final String QUEUE_NAME = "job.scrape.queue"; // Wire specification queue [cite: 50]

    // Centralized programmatic scheduler loop [cite: 62]
    @Scheduled(cron = "${app.scheduler.cron:0 */15 * * * *}") // Defaults to every 15 minutes
    public void executeCoreTick() {
        log.info("Initiating system core orchestration tick...");

        // 1. Target Filtering [cite: 63]
        List<TargetSite> activeTargets = targetSiteRepository.findAllActiveSubscriptionTargets();
        log.info("Aggregated {} active target sites for queue balancing.", activeTargets.size());

        // 2. Granular Queue Partitioning [cite: 65]
        for (TargetSite target : activeTargets) {
            // Process the comma-delimited targeting criteria array [cite: 39]
            List<String> keywordList = Arrays.asList(target.getRequest().getKeywords().split("\\s*,\\s*"));

            JobScrapePayload payload = new JobScrapePayload(
                target.getRequest().getId(),
                target.getId(),
                target.getUrl(),
                keywordList
            );

            // 3. Dispatch to ActiveMQ Broker Grid
            try {
                jmsTemplate.convertAndSend(QUEUE_NAME, payload);
                log.debug("Fractured and dispatched target task to broker: RequestID -> {}", payload.requestId());
            } catch (Exception e) {
                log.error("Failed to stream orchestration target task to processing grid ID: {}", target.getId(), e);
            }
        }
    }
}