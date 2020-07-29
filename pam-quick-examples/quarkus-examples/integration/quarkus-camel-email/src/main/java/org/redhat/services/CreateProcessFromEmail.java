package org.redhat.services;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.Exchange;
import org.apache.camel.http.base.HttpOperationFailedException;
import java.lang.Exception;


public class CreateProcessFromEmail extends RouteBuilder {
    
    private static final String RESPONSE_STRING_FORMAT = "%s\n";

    @Override
    public void configure() throws Exception {

        //Global error handler
        errorHandler(defaultErrorHandler().maximumRedeliveries(0).retryAttemptedLogLevel(LoggingLevel.ERROR));
        
        //Generic expection handler for all routes
        onException(Exception.class)
            .handled(true)
            .setBody(constant("Whoops, something has gone wrong"))
            .to("log:GeneralError?level=ERROR");

        onException(java.net.ConnectException.class)
            .handled(true)
            .setBody(constant("Whoops, for reason we cannot connect to the business process engine"))
            .to("log:ExternalConnectionError?level=ERROR");

        onException(HttpOperationFailedException.class)
            .handled(true)
            .process(this::handleHttpFailure)
            .to("log:ExternalConnectionError?level=ERROR");

        //Starts a case based on a timer. Timer value  is specified in the application.properties file.
//        from("timer:foo?period={{timer.period}}").autoStartup("{{start.timer.poller}}")
//            .log(LoggingLevel.INFO, "Timer event triggered.")
//            .setBody().simple("resource:classpath:default-payload.json")
//            .to("direct:createBusinessProcess");
        
        //Starts a case based on the reciept on an email. The email configuration settings are stored in the application.properties file.
        from("imaps://{{email.imap.address}}?username={{email.username}}&password={{email.password}}" + "&delete=false&unseen=true").autoStartup(false)
            .log(LoggingLevel.INFO, "Email Recieved: ${body}")
            .setBody().simple("")
            .to("direct:createBusinessProcess");

        //Simple route that creates either a process or case using the kie-server rest api.
        from("direct:createBusinessProcess")
            .log(LoggingLevel.INFO, "Create new business process request received: ${body}")
            .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
            .choice()
                .when(simple("'{{kie.server.instance.type}}' == 'CASE'")) //When true a new case instance is created.
                    .to("http://{{kie.server.address}}:{{kie.server.port}}/kie-server/services/rest/server/containers/{{kie.server.container.id}}/cases/{{kie.server.definition.id}}/instances" +
                            "?httpMethod=POST" +
                            "&authMethod=Basic" +
                            "&authUsername={{kie.server.username}}" +
                            "&authPassword={{kie.server.password}}" +
                            "&authenticationPreemptive=true")
                    .log(LoggingLevel.INFO, "New message delivered and case created. Process Instance ID = ${body} ")
                .when(simple("'{{kie.server.instance.type}}' == 'PROCESS'")) //When true a new process instance is created.
                    .to("http://{{kie.server.address}}:{{kie.server.port}}/kie-server/services/rest//server/containers/{{kie.server.container.id}}/processes/{{kie.server.definition.id}}/instances" +
                            "?httpMethod=POST" +
                            "&authMethod=Basic" +
                            "&authUsername={{kie.server.username}}" +
                            "&authPassword={{kie.server.password}}" +
                            "&authenticationPreemptive=true")
                    .log(LoggingLevel.INFO, "New message delivered and process created. Process Instance ID = ${body} ")
                .otherwise()
                    .log(LoggingLevel.WARN, "The kie.server.new.instance.type property needs to be set to either CASE or PROCESS. Currently it is set to : [{{kie.server.instance.type}}] ");
        }

        private void handleHttpFailure(Exchange exchange) {
            HttpOperationFailedException e = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, HttpOperationFailedException.class);
            exchange.getMessage().setHeaders(exchange.getIn().getHeaders());
            exchange.getMessage().setBody(String.format(RESPONSE_STRING_FORMAT,
            String.format("%s %d %s", "Whoops, there's problem with the http operation you've tried to perform.", e.getStatusCode(), e.getResponseBody())
            ));
        }
}

    