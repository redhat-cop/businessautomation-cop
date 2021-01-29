package org.redhat.services.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Configuration
public class SwaggerConfig {
    @Controller
    class SwaggerWelcome {
        @RequestMapping( "/swagger-ui" )
        public String redirectToUi() {
            return "redirect:/webjars/swagger-ui/index.html?url=/api/api-doc&validatorUrl=";
        }
    }
}