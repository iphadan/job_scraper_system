package com.jtech.api_gateway_spring;

import com.jtech.api_gateway_spring.model.TargetSite;
import com.jtech.api_gateway_spring.repository.TargetSiteRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;

@SpringBootApplication
@EnableScheduling
@Slf4j
public class ApiGatewaySpringApplication implements CommandLineRunner {

    public ApiGatewaySpringApplication(TargetSiteRepository targetSiteRepository) {
        this.targetSiteRepository = targetSiteRepository;
    }

    public static void main(String[] args) {
		SpringApplication.run(ApiGatewaySpringApplication.class, args);
	}
private final TargetSiteRepository targetSiteRepository;

    @Override
    public void run(String... args) throws Exception {
        List<TargetSite> targetSiteList = targetSiteRepository.findAll();
                if(targetSiteList.isEmpty()) {
                    targetSiteRepository.save(new TargetSite(null, "REMOTEOK", "RemoteOK API", "https://remoteok.com/api"));
                    targetSiteRepository.save(new TargetSite(null, "WWR", "We Work Remotely", "https://weworkremotely.com"));
                    targetSiteRepository.save(new TargetSite(null, "DJINI", "Djini Job Board", "https://djini.co"));
                }
                else {
                    log.info("There are pre stored target sites");
                }
    }
}
