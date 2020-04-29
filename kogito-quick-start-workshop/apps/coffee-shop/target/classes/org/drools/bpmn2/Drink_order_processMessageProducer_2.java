package org.drools.bpmn2;

import java.util.Optional;
import java.util.TimeZone;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.kogito.event.DataEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.github.javaparser.ast.body.MethodDeclaration;

@javax.enterprise.context.ApplicationScoped()
public class Drink_order_processMessageProducer_2 {

    @javax.inject.Inject()
    @io.smallrye.reactive.messaging.annotations.Channel("barista-process")
    io.smallrye.reactive.messaging.annotations.Emitter<String> emitter;

    @org.eclipse.microprofile.config.inject.ConfigProperty(name = "kogito.messaging.as-cloudevents")
    Optional<Boolean> useCloudEvents = Optional.of(true);

    private ObjectMapper json = new ObjectMapper();

    @javax.annotation.PostConstruct()
    public void configure() {
        json.setDateFormat(new StdDateFormat().withColonInTimeZone(true).withTimeZone(TimeZone.getDefault()));
    }

    public void produce(ProcessInstance pi, org.bala.drink.coffee.model.DrinkOrder eventData) {
        emitter.send(this.marshall(pi, eventData));
    }

    private String marshall(ProcessInstance pi, org.bala.drink.coffee.model.DrinkOrder eventData) {
        try {
            if (useCloudEvents.orElse(true)) {
                Drink_order_processMessageDataEvent_2 event = new Drink_order_processMessageDataEvent_2("", eventData, pi.getId(), pi.getParentProcessInstanceId(), pi.getRootProcessInstanceId(), pi.getProcessId(), pi.getRootProcessId(), String.valueOf(pi.getState()));
                if (pi.getReferenceId() != null && !pi.getReferenceId().isEmpty()) {
                    event.setKogitoReferenceId(pi.getReferenceId());
                }
                return json.writeValueAsString(event);
            } else {
                return json.writeValueAsString(eventData);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
