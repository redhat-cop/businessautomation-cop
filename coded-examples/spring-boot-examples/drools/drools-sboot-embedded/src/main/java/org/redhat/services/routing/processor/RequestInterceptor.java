package org.redhat.services.routing.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RequestInterceptor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
    }
}
