package org.jbpm.cucumber;

import org.junit.runner.RunWith;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

@RunWith(Cucumber.class)
@CucumberOptions (
    features = "src/test/resources/features", 
    glue = "org.jbpm.cucumber"
)
public class RunCucumberTest {

}
