package org.redhat.services.rules.api;

import org.redhat.services.model.RuleResponse;

public interface RuleExecutor {

    public RuleResponse executeHelloWorldRules();

    public RuleResponse executeGoodbyeRules(String name);

}
