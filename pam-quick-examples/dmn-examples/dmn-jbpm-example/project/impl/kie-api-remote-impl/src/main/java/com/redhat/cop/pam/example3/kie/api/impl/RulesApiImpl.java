package com.redhat.cop.pam.example3.kie.api.impl;

import com.redhat.cop.pam.example3.CanOpenAccountResults;
import com.redhat.cop.pam.example3.Customer;
import com.redhat.cop.pam.example3.kie.api.RulesApi;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNDecisionResult;
import org.kie.dmn.api.core.DMNMessage;
import org.kie.dmn.api.core.DMNResult;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class RulesApiImpl implements RulesApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(RulesApiImpl.class);

    private static final String URL = System.getProperty("com.redhat.cop.pam.kieserver_url");

    private static final String USER = System.getProperty("com.redhat.cop.pam.kieserver_user");

    private static final String PASSWORD = System.getProperty("com.redhat.cop.pam.kieserver_password");

    private static final String CONTAINER_ID = System.getProperty("com.redhat.cop.pam.kieserver_containerid");

    private KieServicesClient kieServicesClient;

    @PostConstruct
    public void init(){
        final KieServicesConfiguration config = KieServicesFactory.newRestConfiguration(URL, USER, PASSWORD);
        final Set<Class<?>> extraClasses = new HashSet<>();
        extraClasses.add(Customer.class);
        config.addExtraClasses(extraClasses);
        config.setTimeout(100000l);
        kieServicesClient = KieServicesFactory.newKieServicesClient(config);
        final String[] gav = CONTAINER_ID.split(":");
        final KieContainerResource containerResource = new KieContainerResource(CONTAINER_ID, new ReleaseId(gav[0], gav[1], gav[2]));
        kieServicesClient.createContainer(CONTAINER_ID, containerResource);
    }

    @PreDestroy
    public void preDestroy(){
        kieServicesClient.disposeContainer(CONTAINER_ID);
    }

    @Override
    public CanOpenAccountResults canOpenAccount(final Customer customer) {
        final ProcessServicesClient processServicesClient = kieServicesClient.getServicesClient(ProcessServicesClient.class);
        final QueryServicesClient queryServicesClient = kieServicesClient.getServicesClient(QueryServicesClient.class);
        final Map<String, Object> processParameters = new HashMap<>();
        processParameters.put("customerProcessVar", customer);
        final Long processInstanceId = processServicesClient.startProcess(CONTAINER_ID,"dmn-example3-kjar.customer-can-open-account", processParameters);
        final ProcessInstance processInstance = queryServicesClient.findProcessInstanceById(processInstanceId, true);
        return CanOpenAccountResults.valueOf((String)processInstance.getVariables().get("canOpenAccountProcessVar"));
    }
}