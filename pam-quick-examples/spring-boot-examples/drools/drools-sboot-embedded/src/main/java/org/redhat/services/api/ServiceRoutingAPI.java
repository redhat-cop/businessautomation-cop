package org.redhat.services.api;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.model.rest.RestParamType;
import org.redhat.services.routing.processor.RequestInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.MediaType;

import static org.redhat.services.util.RoutingConstants.*;

@Component
public class ServiceRoutingAPI extends RouteBuilder {

    @Autowired
    private Environment env;

    @Value("${camel.component.servlet.mapping.context-path}")
    private String contextPath;

    @Override
    public void configure() throws Exception {

        // @formatter:off
        
        // API Configuration
        restConfiguration()
            .component("servlet")
            .bindingMode(RestBindingMode.json)
            .dataFormatProperty("prettyPrint", "true")
            .enableCORS(true)
            .port(env.getProperty("server.port", "8080"))
            .contextPath(contextPath.substring(0, contextPath.length() - 2))
            .apiContextPath("/api-doc")
            .apiProperty("api.title", "Drools Service API")
            .apiProperty("api.version", "0.0.1")
            .apiProperty("api.description", "APIs that do some stuff")
            .apiProperty("api.contact.name", "BA COP");
        interceptFrom().bean(RequestInterceptor.class);

        // Dedicated HelloWorld API Endpoint
        rest().get(API.DEMO_RULES_HELLOWORLD)
        	.produces( MediaType.APPLICATION_JSON )
        	.id(ROUTE_ID.DEMO_API_ROUTE_HELLO)
        	.to(CORE.HELLO_RULES);

        // Dedicated Goodbye World API Endpoint
        rest().get(API.DEMO_RULES_GOODBYE + "/{name}")
        	.produces( MediaType.APPLICATION_JSON )
            .param()
                .name( "name" )
                .type( RestParamType.path )
                .dataType( "string" )
                .required( true )
            .endParam()
        	.id(ROUTE_ID.DEMO_API_ROUTE_GOODBYE)
        	.to(CORE.GOODBYE_RULES);

        rest().get(API.AUDIT_LOG_BY_REFERENCE + "/{reference}")
        	.produces( MediaType.APPLICATION_JSON )
            .param()
                .name( "reference" )
                .type( RestParamType.path )
                .dataType( "string" )
                .required( true )
            .endParam()
        	.id(ROUTE_ID.AUDIT_API_ROUTE_BYREF)
        	.to(AUDIT.FIND_BY_REF);

        // Update Country KIE Base / KJAR
//        rest("trigger/release/update/{country}")
//            .put().produces( MediaType.APPLICATION_JSON )
//            .param()
//                .name( "country" )
//                .type( RestParamType.path )
//                .dataType( "string" )
//                .required( true )
//                .endParam()
//            .to( CORE.SCAN_KIEBASE );
//
		// @formatter:off

	}

}