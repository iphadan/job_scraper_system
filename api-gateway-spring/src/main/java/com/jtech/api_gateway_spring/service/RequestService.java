package com.jtech.api_gateway_spring.service;

import com.jtech.api_gateway_spring.model.Request;
import com.jtech.api_gateway_spring.model.TargetSite;
import com.jtech.api_gateway_spring.repository.RequestRepository;
import com.jtech.api_gateway_spring.repository.TargetSiteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestService {

    private final RequestRepository requestRepository;
    private final TargetSiteRepository targetSiteRepository;

    /**
     * Retrieve requests for a specific user.
     */
    @Transactional(readOnly = true)
    public List<Request> getRequestsForUser(String username) {
        log.debug("Fetching strategies for user: {}", username);
        return requestRepository.findByUsernameIgnoreCase(username);
    }

    /**
     * Creates a new scraper request and maps it to the selected predefined target sites.
     */
    @Transactional
    public Request createRequest(Request incomingRequest, String currentUsername) {
        log.info("Processing new strategy registration for user: {}", currentUsername);

        // 1. Establish ownership & defaults
        incomingRequest.setUsername(currentUsername);
        if (incomingRequest.getStatus() == null || incomingRequest.getStatus().isEmpty()) {
            incomingRequest.setStatus("ACTIVE");
        }

        // 2. Resolve target site payload references to our static DB metadata
        Set<TargetSite> resolvedSites = new HashSet<>();
        if (incomingRequest.getTargetSites() != null) {
            for (TargetSite sitePayload : incomingRequest.getTargetSites()) {
                // Find existing static record so we don't accidentally insert/update the catalog
                TargetSite existingSite = targetSiteRepository.findBySiteCode(sitePayload.getSiteCode())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Target site not supported: " + sitePayload.getSiteCode()
                        ));
                resolvedSites.add(existingSite);
            }
        }

        // 3. Bind clean set to model (this is what populates our 'request_target_sites' join table)
        incomingRequest.setTargetSites(resolvedSites);

        // 4. Persist request (and join records)
        return requestRepository.save(incomingRequest);
    }
}