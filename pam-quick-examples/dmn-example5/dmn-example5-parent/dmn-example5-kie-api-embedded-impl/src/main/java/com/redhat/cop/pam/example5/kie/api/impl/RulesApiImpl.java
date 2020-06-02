package com.redhat.cop.pam.example5.kie.api.impl;

import com.redhat.cop.pam.example5.CanOpenAccountResults;
import com.redhat.cop.pam.example5.Customer;
import com.redhat.cop.pam.example5.kie.api.RulesApi;
import com.redhat.cop.pam.example5.kie.api.utils.KieContainerUtils;
import org.kie.api.runtime.KieContainer;
import org.kie.dmn.api.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.stream.Collectors;

@ApplicationScoped
public class RulesApiImpl implements RulesApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(RulesApiImpl.class);

    private static final String NAMESPACE = "https://pam.cop.redhat.com/dmn-example-5/dmn";

    private static final String MODEL_NAME = "customer-can-open-account";

    @Inject
    KieContainerUtils kieContainerUtils;

    @Override
    public CanOpenAccountResults canOpenAccount(final Customer customer) {
        final KieContainer kieContainer = kieContainerUtils.getKieContainer();
        final DMNRuntime dmnRuntime = kieContainer.newKieSession().getKieRuntime(DMNRuntime.class);
        final DMNModel dmnModel = dmnRuntime.getModel(NAMESPACE, MODEL_NAME);
        final DMNContext dmnContext = dmnRuntime.newContext();
        dmnContext.set("customer", customer);
        final DMNResult dmnResult = dmnRuntime.evaluateAll(dmnModel, dmnContext);
        return extractResult(dmnResult);
    }

    private CanOpenAccountResults extractResult(final DMNResult dmnResult) {
        final DMNDecisionResult decisionResults = dmnResult.getDecisionResultByName("can open account");
        if(decisionResults.hasErrors()){
            final String errors = dmnResult.getMessages(DMNMessage.Severity.ERROR).stream()
                    .map(message -> message.toString())
                    .collect(Collectors.joining(", "));
            throw new RuntimeException("DMN Error messages {" + errors + "}");
        }
        return CanOpenAccountResults.valueOf(String.valueOf(decisionResults.getResult()));
    }

}
