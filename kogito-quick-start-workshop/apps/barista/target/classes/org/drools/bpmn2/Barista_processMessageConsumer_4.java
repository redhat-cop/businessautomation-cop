package org.drools.bpmn2;

import java.util.TimeZone;
import java.util.Optional;
import javax.annotation.PostConstruct;
import org.kie.kogito.Application;
import org.kie.kogito.event.DataEvent;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.impl.Sig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import org.drools.bpmn2.Barista_processModel;

@javax.enterprise.context.ApplicationScoped()
public class Barista_processMessageConsumer_4 {

    private static final Logger LOGGER = LoggerFactory.getLogger("MessageConsumer");

    @javax.inject.Inject()
    @javax.inject.Named("barista_process")
    Process<Barista_processModel> process;

    @javax.inject.Inject()
    Application application;

    @org.eclipse.microprofile.config.inject.ConfigProperty(name = "kogito.messaging.as-cloudevents")
    Optional<Boolean> useCloudEvents = Optional.of(true);

    private ObjectMapper json = new ObjectMapper();

    @javax.annotation.PostConstruct()
    public void configure() {
        json.setDateFormat(new StdDateFormat().withColonInTimeZone(true).withTimeZone(TimeZone.getDefault()));
    }

    @org.eclipse.microprofile.reactive.messaging.Incoming("barista-process")
    public void consume(String payload) {
        final String trigger = "barista-process";
        try {
            if (useCloudEvents.orElse(true)) {
                final Barista_processMessageDataEvent_4 eventData = json.readValue(payload, Barista_processMessageDataEvent_4.class);
                final Barista_processModel model = new Barista_processModel();
                model.setOrder(eventData.getData());
                org.kie.kogito.services.uow.UnitOfWorkExecutor.executeInUnitOfWork(application.unitOfWorkManager(), () -> {
                    if (eventData.getKogitoReferenceId() != null) {
                        LOGGER.debug("Received message with reference id '{}' going to use it to send signal '{}'", eventData.getKogitoReferenceId(), trigger);
                        process.instances().findById(eventData.getKogitoReferenceId()).ifPresent(pi -> pi.send(Sig.of("Message-" + trigger, eventData.getData(), eventData.getKogitoProcessinstanceId())));
                    } else {
                        LOGGER.debug("Received message without reference id, staring new process instance with trigger '{}'", trigger);
                        ProcessInstance<Barista_processModel> pi = process.createInstance(model);
                        if (eventData.getKogitoStartFromNode() != null) {
                            pi.startFrom(eventData.getKogitoStartFromNode(), eventData.getKogitoProcessinstanceId());
                        } else {
                            pi.start(trigger, eventData.getKogitoProcessinstanceId());
                        }
                    }
                    return null;
                });
            } else {
                final org.bala.drink.coffee.model.DrinkOrder eventData = json.readValue(payload, org.bala.drink.coffee.model.DrinkOrder.class);
                final Barista_processModel model = new Barista_processModel();
                model.setOrder(eventData);
                org.kie.kogito.services.uow.UnitOfWorkExecutor.executeInUnitOfWork(application.unitOfWorkManager(), () -> {
                    LOGGER.debug("Received message without reference id, staring new process instance with trigger '{}'", trigger);
                    ProcessInstance<Barista_processModel> pi = process.createInstance(model);
                    pi.start(trigger, null);
                    return null;
                });
            }
        } catch (Exception e) {
            LOGGER.error("Error when consuming message for process {}", process.id(), e);
        }
    }
}
