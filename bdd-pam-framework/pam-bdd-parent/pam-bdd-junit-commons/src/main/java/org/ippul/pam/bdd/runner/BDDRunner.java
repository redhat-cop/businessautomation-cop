package org.ippul.pam.bdd.runner;

import io.cucumber.junit.CucumberOptions;

@CucumberOptions(
            glue = {"org.ippul.pam.bdd.steps"},
            plugin = {"json:target/junit-tests-cucumber.json", "pretty", "html:target/cucumber-reports.html"}
        )
public class BDDRunner {

}
