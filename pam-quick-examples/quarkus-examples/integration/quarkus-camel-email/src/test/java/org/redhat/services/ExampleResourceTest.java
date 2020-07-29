package org.redhat.services;

import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.io.IOException;

@Slf4j
@QuarkusTest
public class ExampleResourceTest {

    @Inject
    CamelContext camelContext;

    @Test
    public void testHelloEndpoint() throws IOException, InterruptedException {

        log.info("hello world... write some tests!!");

    }

}