package org.redhat.services.kie.listener;

import lombok.extern.slf4j.Slf4j;
import org.kie.api.event.rule.ObjectDeletedEvent;
import org.kie.api.event.rule.ObjectInsertedEvent;
import org.kie.api.event.rule.ObjectUpdatedEvent;
import org.kie.api.event.rule.RuleRuntimeEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
public class WorkingMemoryListener implements RuleRuntimeEventListener {

    private String reference;

    public WorkingMemoryListener(String reference){
        this.reference = reference;
    }

    @Override
    public void objectInserted(ObjectInsertedEvent event) {

    }

    @Override
    public void objectUpdated(ObjectUpdatedEvent event) {

    }

    @Override
    public void objectDeleted(ObjectDeletedEvent event) {
        // TODO Auto-generated method stub
    }

}
