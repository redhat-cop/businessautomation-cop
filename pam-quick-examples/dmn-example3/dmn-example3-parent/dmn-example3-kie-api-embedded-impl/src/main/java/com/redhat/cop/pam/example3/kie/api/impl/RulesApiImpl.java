package com.redhat.cop.pam.example3.kie.api.impl;

import com.redhat.cop.pam.example3.CanOpenAccountResults;
import com.redhat.cop.pam.example3.Customer;
import com.redhat.cop.pam.example3.kie.api.RulesApi;
import com.redhat.cop.pam.example3.kie.api.utils.KieContainerUtils;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.jbpm.workflow.instance.WorkflowProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class RulesApiImpl implements RulesApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(RulesApiImpl.class);

    @Inject
    KieContainerUtils kieContainerUtils;

    @Override
    public CanOpenAccountResults canOpenAccount(final Customer customer) {
        final KieContainer kieContainer = kieContainerUtils.getKieContainer();
        final Map<String, Object> processParameters = new HashMap<>();
        processParameters.put("customerProcessVar", customer);
        final KieSession kieSession = kieContainer.newKieSession();
        final ProcessInstance processInstance  = kieSession.startProcess("dmn-example3-kjar.customer-can-open-account", processParameters);
        final WorkflowProcessInstance workflowProcessInstance = (WorkflowProcessInstance) processInstance;
        return CanOpenAccountResults.valueOf((String)workflowProcessInstance.getVariable("canOpenAccountProcessVar"));
    }

}
