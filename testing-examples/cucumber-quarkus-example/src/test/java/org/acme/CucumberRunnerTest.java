package org.acme;

import io.quarkiverse.cucumber.CucumberOptions;
import io.quarkiverse.cucumber.CucumberQuarkusTest;

@CucumberOptions(features = "src/test/resources/org/acme", glue = "org.acme")
public class CucumberRunnerTest extends CucumberQuarkusTest {
    public static void main(String[] args) {
        runMain(CucumberRunnerTest.class, args);
    }
}