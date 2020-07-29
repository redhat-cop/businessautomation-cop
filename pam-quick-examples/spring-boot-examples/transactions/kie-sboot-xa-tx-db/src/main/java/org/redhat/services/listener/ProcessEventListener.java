package org.redhat.services.listener;

import org.redhat.services.repository.TestRepository;
import org.redhat.services.jpa.TestEntity;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jbpm.ruleflow.instance.RuleFlowProcessInstance;
import org.kie.api.event.process.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProcessEventListener implements org.kie.api.event.process.ProcessEventListener {

    @Autowired
    TestRepository repository;

    @Override
    public void beforeProcessStarted(ProcessStartedEvent processStartedEvent) {

    }

    @SneakyThrows // lombok h4x for demo only
    @Override
    public void afterProcessStarted(ProcessStartedEvent processStartedEvent) {
        if (StringUtils.equals("SampleProcess", processStartedEvent.getProcessInstance().getProcessId())) {

            RuleFlowProcessInstance rfpi = (RuleFlowProcessInstance) processStartedEvent.getProcessInstance();
            boolean persist = (boolean) rfpi.getVariable("persistEntity");

            log.info("Persist Entity via Lister: {}", persist);
            if (persist) {
                boolean throwException = (boolean) rfpi.getVariable("throwException");
                log.info(" 'throwException' status : {}", throwException);

                TestEntity entity = new TestEntity();
                entity.setDescription("Hello World :: " + processStartedEvent.getProcessInstance().getId());
                entity.setProcessInstanceId(processStartedEvent.getProcessInstance().getId());
                log.info(" Saving Test Entity Synchronously ");
                repository.save(entity);

                if (throwException) {
                    throw new Exception("Something went wrong bro...");
                }
                log.info(" XA Transaction Commited");
            }
        }
    }

    @Override
    public void beforeProcessCompleted(ProcessCompletedEvent processCompletedEvent) {

    }

    @Override
    public void afterProcessCompleted(ProcessCompletedEvent processCompletedEvent) {

    }

    @Override
    public void beforeNodeTriggered(ProcessNodeTriggeredEvent processNodeTriggeredEvent) {

    }

    @Override
    public void afterNodeTriggered(ProcessNodeTriggeredEvent processNodeTriggeredEvent) {

    }

    @Override
    public void beforeNodeLeft(ProcessNodeLeftEvent processNodeLeftEvent) {

    }

    @Override
    public void afterNodeLeft(ProcessNodeLeftEvent processNodeLeftEvent) {

    }

    @Override
    public void beforeVariableChanged(ProcessVariableChangedEvent processVariableChangedEvent) {

    }

    @Override
    public void afterVariableChanged(ProcessVariableChangedEvent processVariableChangedEvent) {

    }
}
