package com.jtech.api_gateway_spring.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

@Value("${spring.mail.username}")
private String emailAddress;

    private final JavaMailSender mailSender;

    public void sendJobAlert(String userEmail, String jobTitle, String company, String jobUrl) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(emailAddress);
            message.setTo(userEmail);
            message.setSubject("🎯 New Job Match: " + jobTitle);
            message.setText(String.format(
                "Hello!\n\nA new job matching your preferences has been scraped:\n\n" +
                "📌 Position: %s\n" +
                "🏢 Company: %s\n" +
                "🔗 Apply Here: %s\n\n" +
                "Best regards,\nYour Job Scraper Team",
                jobTitle, company, jobUrl
            ));

            mailSender.send(message);
            log.info("📧 Job alert email successfully sent to {}", userEmail);
        } catch (Exception e) {
            log.error("❌ Failed to send email to {}: {}", userEmail, e.getMessage());
        }
    }
}