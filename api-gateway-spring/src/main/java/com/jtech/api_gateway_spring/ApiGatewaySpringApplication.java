package com.jtech.api_gateway_spring;

import com.jtech.api_gateway_spring.model.TargetSite;
import com.jtech.api_gateway_spring.repository.TargetSiteRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@Slf4j
public class ApiGatewaySpringApplication implements CommandLineRunner {

    private final TargetSiteRepository targetSiteRepository;

    public ApiGatewaySpringApplication(TargetSiteRepository targetSiteRepository) {
        this.targetSiteRepository = targetSiteRepository;
    }

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewaySpringApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        seedTargetSite("REMOTEOK", "RemoteOK API", "https://remoteok.com/api");
        seedTargetSite("WWR", "We Work Remotely", "https://weworkremotely.com");
        seedTargetSite("DJINI", "Djini Job Board", "https://djini.co");
        seedTargetSite("KIFIYA", "Kifiya Job Board", "https://kifiya.com/work-with-us/");
        seedTargetSite("EAGLELION", "EagleLion Job Board", "https://www.eaglelionsystems.com/team");
        seedTargetSite("SAFARICOM_ET", "Safaricom Job Board", "https://egjd.fa.us6.oraclecloud.com/hcmUI/CandidateExperience/en/sites/STEP/jobs");
    }

    private void seedTargetSite(String siteCode, String name, String targetUrl) {
        if (targetSiteRepository.findBySiteCode(siteCode).isEmpty()) {
            targetSiteRepository.save(new TargetSite(null, siteCode, name, targetUrl));
            log.info("✅ {} job site has been added", siteCode);
        } else {
            log.info("ℹ️ {} site already exists", siteCode);
        }
    }
}