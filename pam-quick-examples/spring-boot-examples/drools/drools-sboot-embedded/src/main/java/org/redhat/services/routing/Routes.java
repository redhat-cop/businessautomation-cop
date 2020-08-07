package org.redhat.services.routing;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.commons.lang3.NotImplementedException;
import org.redhat.services.exception.KieContainerRequestException;
import org.redhat.services.exception.RequestException;
import org.redhat.services.rules.api.RuleExecutor;
import org.redhat.services.service.api.ExecutorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.redhat.services.util.RoutingConstants.CORE;

@Component
public class Routes extends RouteBuilder {

    @Autowired
    ExecutorService executorService;

    @Autowired
    RuleExecutor ruleExecutor;

    @Override
    public void configure() throws Exception {

        // @formatter:off

        /*************************/
        /** Exception Handlers  **/
        /*************************/

        // Request not understood
        onException(RequestException.class)
            .handled(true)
            .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400));

        // KIE Scanner fails to load container
        onException(KieContainerRequestException.class)
            .handled(true)
            .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(409))
            .setBody(constant("Unable to load KIE Container, please check the logs and review the request"));

        // KIE Scanner fails to load container
        onException(NotImplementedException.class)
            .handled(true)
            .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(501))
            .setBody(constant("Requested Method not Implemented yet."));

        /**********************************/
        /**  General/Healthcheck Routes  **/
        /**********************************/

        // Demo HelloWorld Base Rules Impl
        from( CORE.HELLO_RULES )
            .log( LoggingLevel.INFO, "Invoking HelloWorld Rules Route" )
            .bean( ruleExecutor, "executeHelloWorldRules" );

        // Demo GoodbyeWorld Base Rules Impl
        from( CORE.GOODBYE_RULES )
            .log( LoggingLevel.INFO, "Invoking GoodbyeWorld Rules Route" )
            .bean( ruleExecutor, "executeGoodbyeRules( ${header.name} )" );

        /*****************************/
        /**  KIE Management Routes  **/
        /*****************************/

        // Load new Kie Base
        from( CORE.LOAD_KIEBASE )
            .log( LoggingLevel.INFO, "Loading new KIE Base" )
            //.bean(RequestValidator.class, "validateKieLoadRequest")
            .bean( executorService, "loadLatestKIEBase(  ${body},  ${header.containerId} )" );

        // Scan for latest KIE Base
        from( CORE.SCAN_KIEBASE )
            .log( LoggingLevel.INFO, "Scanning KIE Base " )
            //.bean(RequestValidator.class, "validateKieScanRequest")
            .bean( executorService, "scanLatestKIEBase( ${header.containerId} )" );


     // @formatter:on

    }

}