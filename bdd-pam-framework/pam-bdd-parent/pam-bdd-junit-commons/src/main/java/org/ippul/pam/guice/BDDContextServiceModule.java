package org.ippul.pam.guice;

import com.google.inject.AbstractModule;
import io.cucumber.guice.ScenarioScoped;
import org.ippul.pam.bdd.context.BDDContext;

import java.util.Objects;
import java.util.ServiceLoader;

public class BDDContextServiceModule extends AbstractModule {

    private static final String ERROR_MESSAGE = "Impossible to load service 'BDDCustomContextProvider'. \n" +
            "Please add a file named 'META-INF/services/BDDCustomContextProvider' containing the the fully qualified name of BDDCustomContextProvider implementation. \n" +
            "refer to https://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html for more information";

    @Override
    protected void configure() {
        final ServiceLoader<BDDCustomContextProvider> serviceLoader = ServiceLoader.load(BDDCustomContextProvider.class);
        final BDDCustomContextProvider service = Objects.requireNonNull(serviceLoader.iterator().next(), ERROR_MESSAGE);
        bind(BDDContext.class).toProvider(service).in(ScenarioScoped.class);
    }

}