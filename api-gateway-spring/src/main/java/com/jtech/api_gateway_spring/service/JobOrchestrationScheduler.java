package com.jtech.api_gateway_spring.service;

import com.fasterxml.jackson.databind.ObjectMapper; // 👈 1. Import Jackson ObjectMapper
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

    private final RequestRepository requestRepository;
    private final JmsTemplate jmsTemplate;
    private final ObjectMapper objectMapper; // 👈 2. Inject ObjectMapper

    private static final String QUEUE_NAME = "job.scrape.queue";

    @Scheduled(fixedRate = 600000/5)
    @Transactional(readOnly = true)
    public void executeCoreTick() {
        log.info("Initiating system core orchestration tick...");

        List<Request> activeRequests = requestRepository.findAllActiveRequestsWithSites();
        log.info("Aggregated {} active user requests for processing.", activeRequests.size());

        for (Request request : activeRequests) {
            List<String> keywordList = Arrays.asList(request.getKeywords().split("\\s*,\\s*"));

            for (TargetSite target : request.getTargetSites()) {

                JobScrapePayload payload = new JobScrapePayload(
                        request.getId(),
                        target.getSiteCode(),
                        target.getUrl(),
                        keywordList
                );

                try {
                    // 👈 3. Convert payload record to a JSON string
                    String jsonPayload = objectMapper.writeValueAsString(payload);

                    // 👈 4. Send pure JSON string to queue
                    jmsTemplate.convertAndSend(QUEUE_NAME, jsonPayload);

                    log.info("Dispatched task to broker: RequestID -> {}, SiteCode -> {}",
                            payload.requestId(), payload.siteCode());
                } catch (Exception e) {
                    log.error("Failed to stream target task to broker for Request ID: {}, Site: {}",
                            request.getId(), target.getSiteCode(), e);
                }
            }
        }
    }
}