package com.redhat.cop.pam.example5.springboot;

import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions( features = "src/test/resources", //
        plugin = { "html:target/cucumber", "json:target/cucumber.json" } )
public class CucumberTest extends SpringConfiguration {

}
