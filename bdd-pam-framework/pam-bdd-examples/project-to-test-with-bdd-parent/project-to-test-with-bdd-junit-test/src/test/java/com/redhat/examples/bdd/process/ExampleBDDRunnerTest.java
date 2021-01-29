package com.redhat.examples.bdd.process;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import com.redhat.pam.bdd.runner.BDDRunner;
import com.redhat.pam.guice.CustomObjectFactory;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = {
                "classpath:example-process-with-custom-mocked-wih.feature",
                "classpath:example-process-with-custom-wih.feature",
                "classpath:example-process-with-humantask.feature",
                "classpath:example-process-with-signal-and-custom-object.feature"
        },
        glue = {"com.redhat.examples.bdd.steps"},
        objectFactory = CustomObjectFactory.class
)
public class ExampleBDDRunnerTest extends BDDRunner {
}
