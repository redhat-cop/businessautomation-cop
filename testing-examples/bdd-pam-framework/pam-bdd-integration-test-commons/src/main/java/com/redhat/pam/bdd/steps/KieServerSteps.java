package com.redhat.pam.bdd.steps;

import com.google.inject.Inject;
import com.redhat.pam.bdd.context.BDDContext;
import io.cucumber.java8.En;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KieServerSteps implements En {

    private static final Logger LOGGER = LoggerFactory.getLogger(KieServerSteps.class);

    @Inject
    private BDDContext context;
}
