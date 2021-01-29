package org.redhat.services;

import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.awaitility.Awaitility;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

@Slf4j
@QuarkusTest
public class ExampleResourceTest {

    @ConfigProperty(name = "data.input.dir")
    String dir;

    @ConfigProperty(name = "data.processed.dir")
    String processed;

    @ConfigProperty(name = "timer.period")
    long timeInterval;

    @Inject
    CamelContext camelContext;

    @Test
    public void testHelloEndpoint() throws IOException, InterruptedException {

        camelContext.suspend();

        log.info("Temp DIR: {}", dir.toString());

        final Path file = Paths.get(dir).resolve("file.txt");
        Files.write(file, "a test file content".getBytes(StandardCharsets.UTF_8));

        log.info("New file path: " + file.toString());
        Assertions.assertTrue(Files.exists(file));

        camelContext.resume();

        Awaitility.await()
                .atMost(timeInterval, TimeUnit.MILLISECONDS)
                .untilAsserted(() ->
                        Assertions.assertTrue(Files.exists(Paths.get(processed).resolve("file.txt"))));

    }

}