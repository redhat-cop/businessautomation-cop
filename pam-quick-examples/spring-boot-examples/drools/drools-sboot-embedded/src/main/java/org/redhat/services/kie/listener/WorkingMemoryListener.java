package org.redhat.services.kie.listener;

import org.kie.api.event.rule.ObjectDeletedEvent;
import org.kie.api.event.rule.ObjectInsertedEvent;
import org.kie.api.event.rule.ObjectUpdatedEvent;
import org.kie.api.event.rule.RuleRuntimeEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkingMemoryListener implements RuleRuntimeEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkingMemoryListener.class);

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
