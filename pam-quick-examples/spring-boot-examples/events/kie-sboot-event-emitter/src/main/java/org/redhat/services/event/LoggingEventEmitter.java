package org.redhat.services.event;

import lombok.extern.slf4j.Slf4j;
import org.jbpm.persistence.api.integration.EventCollection;
import org.jbpm.persistence.api.integration.EventEmitter;
import org.jbpm.persistence.api.integration.InstanceView;
import org.jbpm.persistence.api.integration.base.BaseEventCollection;
import org.jbpm.persistence.api.integration.model.CaseInstanceView;
import org.jbpm.persistence.api.integration.model.ProcessInstanceView;
import org.jbpm.persistence.api.integration.model.TaskInstanceView;
import org.redhat.services.util.BeanUtil;
import org.redhat.services.util.GeneralUtil;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class LoggingEventEmitter implements EventEmitter {

    private GeneralUtil util;
    private ExecutorService executor;

    public LoggingEventEmitter() {
        log.info("Instantiating EventEmitter");
        this.executor = this.buildExecutorService();
        util = BeanUtil.getBean(GeneralUtil.class);
    }

    @Override
    public void deliver(Collection<InstanceView<?>> collection) {
        // no-op
    }

    @Override
    public void apply(Collection<InstanceView<?>> collection) {
        if (!collection.isEmpty()) {

            util.logThisForMe("Spring method from non-spring bean...");
            this.executor.execute(() -> {
                log.info("COLLECTION: {}", collection);

                Iterator iterator = collection.iterator();
                while (iterator.hasNext()) {

                    InstanceView view = (InstanceView) iterator.next();
                    if (view instanceof ProcessInstanceView) {
                        log.info("PROCESS INSTANCE VIEW : {}", view);
                    } else if (view instanceof TaskInstanceView) {
                        log.info("TASK INSTANCE VIEW : {}", view);
                    } else if (view instanceof CaseInstanceView) {
                        log.info("CASE INSTANCE VIEW : {}", view);
                    }
                }
            });
        }
        return;
    }

    @Override
    public void drop(Collection<InstanceView<?>> collection) {
        // no-op
    }

    @Override
    public EventCollection newCollection() {
        return new BaseEventCollection();
    }

    @Override
    public void close() {
        this.executor.shutdown();
        log.info("Logging event emitter closed successfully");
    }

    protected ExecutorService buildExecutorService() {
        return Executors.newCachedThreadPool();
    }
}
