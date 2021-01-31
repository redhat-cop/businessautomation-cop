package com.redhat.pam.bdd.runner;

import io.cucumber.junit.CucumberOptions;
import com.redhat.pam.guice.CustomObjectFactory;

@CucumberOptions(
            glue = {"com.redhat.pam.bdd.steps"},
            plugin = {"json:target/junit-tests-cucumber.json", "pretty", "html:target/cucumber-reports.html"},
            objectFactory = CustomObjectFactory.class
        )
public class BDDRunner {

}
