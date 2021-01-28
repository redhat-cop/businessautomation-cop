package com.ippul.examples.bdd.process;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.ippul.pam.bdd.runner.BDDRunner;
import org.ippul.pam.guice.CustomObjectFactory;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = {
                "classpath:example-process-with-custom-mocked-wih.feature",
                "classpath:example-process-with-custom-wih.feature",
                "classpath:example-process-with-humantask.feature",
                "classpath:example-process-with-signal-and-custom-object.feature"
        },
        glue = {"com.ippul.examples.bdd.steps"},
        objectFactory = CustomObjectFactory.class
)
public class ExampleBDDRunnerTest extends BDDRunner {
}
