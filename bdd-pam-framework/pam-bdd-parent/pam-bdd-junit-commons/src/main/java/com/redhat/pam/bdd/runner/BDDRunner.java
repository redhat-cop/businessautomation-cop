package com.redhat.pam.bdd.runner;

import io.cucumber.junit.CucumberOptions;

@CucumberOptions(
            glue = {"com.redhat.steps"},
            plugin = {"json:target/junit-tests-cucumber.json", "pretty", "html:target/cucumber-reports.html"}
        )
public class BDDRunner {

}
