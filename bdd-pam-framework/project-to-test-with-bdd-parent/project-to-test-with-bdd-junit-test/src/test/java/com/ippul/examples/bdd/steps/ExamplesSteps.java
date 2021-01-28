package com.ippul.examples.bdd.steps;

import com.google.inject.Inject;
import com.ippul.examples.bdd.mocks.MockWorkItemHandler;
import io.cucumber.java8.En;
import org.ippul.pam.bdd.context.BDDContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExamplesSteps implements En {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExamplesSteps.class);

    @Inject
    private BDDContext context;

    public ExamplesSteps() {
        Before("@MockWorkItemHandler", () -> {
            LOGGER.info("Overriding implementation of work item handler name: {} with {}", "CustomWorkItemHandler", MockWorkItemHandler.class.getName());
            context.getKieSession().getWorkItemManager().registerWorkItemHandler("CustomWorkItemHandler", new MockWorkItemHandler());
        });

        And("the process instance id is printed in the console", () -> LOGGER.info("Process instance Id: {}", context.getProcessInstanceId()));
    }
}
