package com.redhat.pam.bdd.listeners;

import com.redhat.pam.bdd.context.BDDContext;
import org.kie.api.event.process.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BDDProcessListener implements ProcessEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(BDDProcessListener.class);

    private final BDDContext context;

    public BDDProcessListener(final BDDContext context){
        this.context = context;
    }

    @Override
    public void beforeProcessStarted(ProcessStartedEvent processStartedEvent) {
        context.setProcessInstanceId(processStartedEvent.getProcessInstance().getId());
        LOGGER.debug("New instance of process definition {} started with process id {}", processStartedEvent.getProcessInstance().getProcessId(), context.getProcessInstanceId());
    }

    @Override
    public void afterProcessStarted(ProcessStartedEvent processStartedEvent) {
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
        LOGGER.debug("Variable id {} updated old value: {} new value: {}", processVariableChangedEvent.getVariableId(), processVariableChangedEvent.getOldValue(), processVariableChangedEvent.getNewValue());
        context.getProcessParameters().put(processVariableChangedEvent.getVariableId(), processVariableChangedEvent.getNewValue());
    }
}
