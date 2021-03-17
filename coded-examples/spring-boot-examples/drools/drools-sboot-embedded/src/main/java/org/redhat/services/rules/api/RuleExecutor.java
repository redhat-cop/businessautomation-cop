package org.redhat.services.rules.api;

import java.util.Map;

import org.redhat.services.model.CarInsuranceRequest;
import org.redhat.services.model.MortgageRequest;
import org.redhat.services.model.RuleResponse;

public interface RuleExecutor {

    public RuleResponse executeHelloWorldRules();

    public RuleResponse executeGoodbyeRules(String name);

    public Map<String, Object> executeMortgageRules(MortgageRequest request);

    public Map<String, Object> executeCarInsuranceRules(CarInsuranceRequest request);

}
