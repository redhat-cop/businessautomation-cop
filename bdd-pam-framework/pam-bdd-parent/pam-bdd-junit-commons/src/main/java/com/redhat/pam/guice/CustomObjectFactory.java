package com.redhat.pam.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.guice.CucumberModules;
import io.cucumber.guice.ScenarioScope;

public class CustomObjectFactory implements ObjectFactory {

    private final Injector injector;

    public CustomObjectFactory() {
        this.injector = Guice.createInjector(Stage.PRODUCTION, CucumberModules.createScenarioModule(), new BDDContextServiceModule());
    }

    @Override
    public boolean addClass(Class<?> clazz) {
        return true;
    }

    @Override
    public void start() {
        this.injector.getInstance(ScenarioScope.class).enterScope();
    }

    @Override
    public void stop() {
        this.injector.getInstance(ScenarioScope.class).exitScope();
    }

    @Override
    public <T> T getInstance(Class<T> clazz) {
        return this.injector.getInstance(clazz);
    }

}