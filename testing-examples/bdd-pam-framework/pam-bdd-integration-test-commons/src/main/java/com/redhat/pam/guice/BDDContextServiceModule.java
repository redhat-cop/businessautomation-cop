package com.redhat.pam.guice;

import com.google.inject.AbstractModule;
import io.cucumber.guice.ScenarioScoped;
import com.redhat.pam.bdd.context.BDDContext;

import java.util.Objects;
import java.util.ServiceLoader;

public class BDDContextServiceModule extends AbstractModule {

    private static final String ERROR_MESSAGE = "Impossible to load service 'BDDCustomContextProvider'. \n" +
            "Please add a file named 'META-INF/services/com.redhat.pam.guice.BDDCustomContextProvider' containing the the fully qualified name of BDDCustomContextProvider implementation.";

    @Override
    protected void configure() {
        final ServiceLoader<BDDCustomContextProvider> serviceLoader = ServiceLoader.load(BDDCustomContextProvider.class);
        final BDDCustomContextProvider service = Objects.requireNonNull(serviceLoader.iterator().next(), ERROR_MESSAGE);
        bind(BDDContext.class).toProvider(service).in(ScenarioScoped.class);
    }

}
