package org.redhat.services.rules.impl;

import lombok.extern.slf4j.Slf4j;
import org.kie.api.runtime.KieContainer;
import org.redhat.services.config.KJARRepositoryConfig;
import org.redhat.services.model.RuleResponse;
import org.redhat.services.rules.api.RuleExecutor;
import org.redhat.services.rules.util.KieQueryUtils;
import org.redhat.services.rules.util.KieSessionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.redhat.services.config.KJARRepositoryConfig.demoContainerId;
import static org.redhat.services.util.RoutingConstants.AGENDA_GROUPS;

@Slf4j
@Component
public class RuleExecutorImpl implements RuleExecutor {

    @Autowired
    private KieSessionUtils sessionUtils;

    @Autowired
    private KieQueryUtils queryUtils;

    @Autowired
    @Qualifier("kjarRepository")
    KJARRepositoryConfig kjarRepository;

    private KieContainer kContainer;

    @Override
    public RuleResponse executeHelloWorldRules() {

        log.debug("Executing executeHelloWorldRules ");

        AtomicReference<List<RuleResponse>> atomicReferenceRuleResponse = new AtomicReference<>();

        //@formatter:off
        sessionUtils.createNewKieSession
        .andThen( kSession -> sessionUtils.fireAgendaGroupRules.apply( kSession, AGENDA_GROUPS.DEMO_HELLO )) // Fire Rules
        .andThen( kSession -> queryUtils.getRuleResponse.apply( kSession, atomicReferenceRuleResponse ))
        .andThen( kSession -> sessionUtils.tearDown.apply(kSession))
        .apply(  kjarRepository.getKieContainer(demoContainerId) );
        // @formatter:on

        return atomicReferenceRuleResponse.get().get(0);
    }

    @Override
    public RuleResponse executeGoodbyeRules(String name) {

        log.debug("Executing executeGoodbyeRules with value : {}", name);

        AtomicReference<List<RuleResponse>> atomicReferenceRuleResponse = new AtomicReference<>();

        //@formatter:off
        sessionUtils.createNewKieSession
        .andThen( kSession -> sessionUtils.insertFact.apply(kSession, name )) // Insert Data into Session
        .andThen( kSession -> sessionUtils.fireAgendaGroupRules.apply( kSession, AGENDA_GROUPS.DEMO_GOODBYE )) // Fire Rules
        .andThen( kSession -> queryUtils.getRuleResponse.apply( kSession, atomicReferenceRuleResponse ))
        .andThen( kSession -> sessionUtils.tearDown.apply(kSession))
        .apply(  kjarRepository.getKieContainer(demoContainerId) );
        // @formatter:on

        return atomicReferenceRuleResponse.get().get(0);
    }

}
