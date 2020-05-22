package com.redhat.cop.pam.example2.springboot;

import com.redhat.cop.pam.example2.kie.api.RulesApi;
import com.redhat.cop.pam.example2.kie.api.impl.RulesApiImpl;
import com.redhat.cop.pam.example2.kie.api.utils.KieContainerUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public KieContainerUtils initKieContainerUtils() {
        return new KieContainerUtils();
    }

    @Bean
    public RulesApi initRulesApi() {
        return new RulesApiImpl();
    }

}
