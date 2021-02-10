package com.redhat.pam.bdd.runner;

import com.redhat.pam.guice.CustomObjectFactory;
import io.cucumber.junit.CucumberOptions;

@CucumberOptions(
        glue = {"com.redhat.pam.bdd.steps"},
        plugin = {"json:target/integration-tests-cucumber.json", "pretty", "html:target/cucumber-reports.html"},
        objectFactory = CustomObjectFactory.class
)
public class BDDRunner {
}
