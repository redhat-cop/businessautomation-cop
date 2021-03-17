package org.redhat.services.rules.impl;

import lombok.extern.slf4j.Slf4j;
import org.kie.api.runtime.KieContainer;
import org.redhat.services.config.KJARRepositoryConfig;
import org.redhat.services.model.CarInsuranceRequest;
import org.redhat.services.model.MortgageRequest;
import org.redhat.services.model.RuleResponse;
import org.redhat.services.rules.api.RuleExecutor;
import org.redhat.services.rules.util.KieQueryUtils;
import org.redhat.services.rules.util.KieSessionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.redhat.services.util.RoutingConstants.AGENDA_GROUPS;
import static org.redhat.services.util.RoutingConstants.CONTAINER_ID;

@Slf4j
@Component
public class RuleExecutorImpl implements RuleExecutor {

    @Autowired
    private KieSessionUtils sessionUtils;

    @Autowired
    private KieQueryUtils queryUtils;

    @Autowired
    KJARRepositoryConfig kjarRepository;

    private KieContainer kContainer;

    @Override
    public RuleResponse executeHelloWorldRules() {

        log.debug("Executing executeHelloWorldRules ");

        AtomicReference<List<RuleResponse>> atomicReferenceRuleResponse = new AtomicReference<>();

        //@formatter:off
        sessionUtils.createNewKieSession
        .andThen( kSession -> sessionUtils.setKieSessionGlobal.apply(kSession, new AbstractMap.SimpleEntry<>("reference", executionReference())))
        .andThen( kSession -> sessionUtils.fireAgendaGroupRules.apply( kSession, AGENDA_GROUPS.DEMO_HELLO )) // Fire Rules
        .andThen( kSession -> queryUtils.getRuleResponse.apply( kSession, atomicReferenceRuleResponse ))
        .andThen( kSession -> sessionUtils.tearDown.apply(kSession))
        .apply(  kjarRepository.getKieContainer(CONTAINER_ID.DEMO) );
        // @formatter:on

        RuleResponse response = atomicReferenceRuleResponse.get().get(0);
        log.info("RuleResponse Obtained: {}", response);
        return response;
    }

    @Override
    public RuleResponse executeGoodbyeRules(String name) {

        log.debug("Executing executeGoodbyeRules with value : {}", name);

        AtomicReference<List<RuleResponse>> atomicReferenceRuleResponse = new AtomicReference<>();

        //@formatter:off
        sessionUtils.createNewKieSession
        .andThen( kSession -> sessionUtils.setKieSessionGlobal.apply(kSession, new AbstractMap.SimpleEntry<>("reference", executionReference())))
        .andThen( kSession -> sessionUtils.insertFact.apply(kSession, name )) // Insert Data into Session
        .andThen( kSession -> sessionUtils.fireAgendaGroupRules.apply( kSession, AGENDA_GROUPS.DEMO_GOODBYE )) // Fire Rules
        .andThen( kSession -> queryUtils.getRuleResponse.apply( kSession, atomicReferenceRuleResponse ))
        .andThen( kSession -> sessionUtils.tearDown.apply(kSession))
        .apply(  kjarRepository.getKieContainer(CONTAINER_ID.DEMO) );
        // @formatter:on

        RuleResponse response = atomicReferenceRuleResponse.get().get(0);
        log.info("RuleResponse Obtained: {}", response);
        return response;
    }


    @Override
    public Map<String, Object> executeCarInsuranceRules(CarInsuranceRequest request) {

        log.debug("Executing executeCarInsuranceRules ");

        Object facts[] = {request.getDriver(), request.getPolicy()};
        AtomicReference<List<Object>> atomicReferenceFacts = new AtomicReference<>();
        // AtomicReference<List<RuleResponse>> atomicReferenceRuleResponse = new AtomicReference<>();

        //@formatter:off
        sessionUtils.createNewKieSession
        .andThen( kSession -> sessionUtils.insertFacts.apply(kSession, facts) ) // insert facts into session
        .andThen( kSession -> sessionUtils.fireAllRules.apply( kSession )) // Fire Rules
        .andThen( kSession -> queryUtils.getAllFacts.apply( kSession, atomicReferenceFacts ))
        .andThen( kSession -> sessionUtils.tearDown.apply(kSession))
        .apply(  kjarRepository.getKieContainer(CONTAINER_ID.DECISIONS_SHOWCASE) );
        // @formatter:on

        // RuleResponse response = atomicReferenceRuleResponse.get().get(0);
        log.info("Facts Obtained: {}", atomicReferenceFacts.get());
        Map<String, Object> returnedFacts =  atomicReferenceFacts.get().stream().collect(
            Collectors.toMap(
                fact -> fact.getClass().getSimpleName(), 
                fact -> fact));
        return returnedFacts;
    }

    @Override
    public Map<String, Object> executeMortgageRules(MortgageRequest request) {

        log.debug("Executing executeCarInsuranceRules ");

        Object facts[] = {
            request.getApplicant(), 
            request.getIncomeSource(), 
            request.getLoanApplication(), 
            request.getBankruptcy()};
        AtomicReference<List<Object>> atomicReferenceFacts = new AtomicReference<>();
        // AtomicReference<List<RuleResponse>> atomicReferenceRuleResponse = new AtomicReference<>();

        //@formatter:off
        sessionUtils.createNewKieSession
        .andThen( kSession -> sessionUtils.insertFacts.apply(kSession, facts) ) // insert facts into session
        .andThen( kSession -> sessionUtils.fireAllRules.apply( kSession )) // Fire Rules
        .andThen( kSession -> queryUtils.getAllFacts.apply( kSession, atomicReferenceFacts ))
        .andThen( kSession -> sessionUtils.tearDown.apply(kSession))
        .apply(  kjarRepository.getKieContainer(CONTAINER_ID.MORTGAGES) );
        // @formatter:on

        // RuleResponse response = atomicReferenceRuleResponse.get().get(0);
        log.info("Facts Obtained: {}", atomicReferenceFacts.get());
        Map<String, Object> returnedFacts =  atomicReferenceFacts.get().stream().collect(
            Collectors.toMap(
                fact -> fact.getClass().getSimpleName(), 
                fact -> fact));
        return returnedFacts;
    }    

    public String executionReference(){
        return UUID.randomUUID().toString();
    }

}
