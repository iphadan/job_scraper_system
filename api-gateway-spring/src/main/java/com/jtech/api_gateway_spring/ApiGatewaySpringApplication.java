package com.jtech.api_gateway_spring;

import com.jtech.api_gateway_spring.model.TargetSite;
import com.jtech.api_gateway_spring.repository.TargetSiteRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.ArrayList;
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

                    TargetSite remoteOk = targetSiteRepository.findBySiteCode("REMOTEOK").isEmpty() == true ? targetSiteRepository.save(new TargetSite(null, "REMOTEOK", "RemoteOK API", "https://remoteok.com/api")) : null;
                    TargetSite wwr = targetSiteRepository.findBySiteCode("WWR").isEmpty() == true ? targetSiteRepository.save(new TargetSite(null, "WWR", "We Work Remotely", "https://weworkremotely.com")) : null ;
                    TargetSite djini = targetSiteRepository.findBySiteCode("DJINI").isEmpty() == true ?    targetSiteRepository.save(new TargetSite(null, "DJINI", "Djini Job Board", "https://djini.co")) : null;
                    TargetSite kifiya = targetSiteRepository.findBySiteCode("KIFIYA").isEmpty() == true ?    targetSiteRepository.save(new TargetSite(null, "KIFIYA", "Kifiya Job Board", "https://kifiya.com/work-with-us/](https://kifiya.com/work-with-us/")) : null;
                    TargetSite eagleLion = targetSiteRepository.findBySiteCode("EAGLELION").isEmpty() == true ?    targetSiteRepository.save(new TargetSite(null, "EAGLELION", "EagleLion Job Board", "https://www.eaglelionsystems.com/team")) : null;
                    TargetSite safaricomEt = targetSiteRepository.findBySiteCode("SAFARICOM_ET").isEmpty() == true ?    targetSiteRepository.save(new TargetSite(null, "SAFARICOM_ET", "Safaricom Job Board", "https://www.safaricom.et/work-with-us/careers/vacancies")) : null;

                    List<String> logs = new ArrayList<>();

                    logs.add( remoteOk == null ? " REMOTEOK site Already Exist" : " REMOTEOK job site has been added");
                    logs.add(  remoteOk == null ? " WWR site Already Exist" : " WWR job site has been added");
                    logs.add( remoteOk == null ? " DJINI site Already Exist" : " DJINI job site has been added");
                    logs.add(  remoteOk == null ? " KIFIYA site Already Exist" : " KIFIYA job site has been added");
                    logs.add(  remoteOk == null ? " EAGLELION site Already Exist" : " EAGLELION job site has been added");
                    logs.add(  remoteOk == null ? " SAFARICOM_ET site Already Exist" : " SAFARICOM_ET job site has been added");

                    for(String message : logs){
                        log.info(message);
                    }





    }
}
