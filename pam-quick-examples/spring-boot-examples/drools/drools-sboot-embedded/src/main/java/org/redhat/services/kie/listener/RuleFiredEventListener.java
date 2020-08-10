package org.redhat.services.kie.listener;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.drools.core.event.DefaultAgendaEventListener;
import org.kie.api.event.rule.AfterMatchFiredEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Tracks Rules activations
 *
 * @author ajarrett
 */
@Slf4j
@NoArgsConstructor
public class RuleFiredEventListener extends DefaultAgendaEventListener {

    private String reference;

    public RuleFiredEventListener(String reference) {
        this.reference = reference;
    }

    private List<String> activationList = new ArrayList<String>();

    @Override
    public void afterMatchFired(AfterMatchFiredEvent event) {
        String sessionId = (String) event.getKieRuntime().getGlobal("sessionId");
        log.debug("{} :: Rule fired: {} with ID : {}", reference, sessionId,
                event.getMatch().getRule().getName(), event.getMatch().getRule().getId());
        activationList.add(event.getMatch().getRule().getName());
    }

    public boolean isRuleFired(String ruleName) {
        for (String rule : activationList) {
            if (ruleName.equalsIgnoreCase(rule)) {
                return true;
            }
        }
        return false;
    }

    public void reset() {
        activationList.clear();
    }

    public final List<String> getActivationList() {
        return activationList;
    }
}
