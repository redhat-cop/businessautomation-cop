package org.drools.project.model;

import org.kie.kogito.rules.KieRuntimeBuilder;
import org.kie.kogito.rules.impl.SessionData;
import org.kie.kogito.rules.impl.SessionUnit;
import org.drools.core.ruleunit.impl.SessionRuleUnitInstance;

@javax.inject.Singleton()
@javax.inject.Named("defaultKieSession")
public class SessionRuleUnit_defaultKieSession extends SessionUnit {

    @javax.inject.Inject()
    KieRuntimeBuilder runtimeBuilder;

    @Override
    public SessionRuleUnitInstance createInstance(SessionData memory, String name) {
        return new SessionRuleUnitInstance(this, memory, runtimeBuilder.newKieSession("defaultKieSession"));
    }
}
