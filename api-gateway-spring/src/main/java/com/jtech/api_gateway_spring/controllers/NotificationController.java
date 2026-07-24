package com.jtech.api_gateway_spring.controllers;

import com.jtech.api_gateway_spring.dto.JobNotificationRequest;
import com.jtech.api_gateway_spring.model.User;
import com.jtech.api_gateway_spring.repository.UserRepository;
import com.jtech.api_gateway_spring.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class NotificationController {

    private final UserRepository userRepository;
    private final EmailService emailService;

    @PostMapping("/notify-matching-users")
    public ResponseEntity<?> notifyMatchingUsers(@RequestBody JobNotificationRequest request) {
        log.info("📩 Received job notification dispatch request for position: '{}' at '{}'", 
                request.getJobTitle(), request.getCompany());

        List<User> allUsers = userRepository.findAll();
        List<String> notifiedEmails = new ArrayList<>();

        String searchableJobText = (request.getJobTitle() + " " + 
                                   (request.getDescription() != null ? request.getDescription() : "")
                                  ).toLowerCase();

        for (User user : allUsers) {
            String preferences = user.getPreferredKeywords();
            
            // If user has no preferred keywords set, skip or notify by default
            if (preferences == null || preferences.trim().isEmpty()) {
                continue;
            }

            // Split comma-separated keywords (e.g., "Java, DevOps, Integration")
            String[] userKeywords = preferences.split(",");
            boolean isMatch = Arrays.stream(userKeywords)
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .filter(kw -> !kw.isEmpty())
                    .anyMatch(searchableJobText::contains);

            if (isMatch) {
                log.info("🎯 Job match found for user '{}' with preferences '{}'", user.getEmail(), preferences);
                
                emailService.sendJobAlert(
                        user.getEmail(),
                        request.getJobTitle(),
                        request.getCompany(),
                        request.getJobUrl()
                );
                
                notifiedEmails.add(user.getEmail());
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("jobTitle", request.getJobTitle());
        response.put("notifiedUserCount", notifiedEmails.size());
        response.put("notifiedEmails", notifiedEmails);

        return ResponseEntity.ok(response);
    }
}