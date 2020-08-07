package org.redhat.services.kie.listener;

import java.util.ArrayList;
import java.util.List;

import org.drools.core.event.DefaultAgendaEventListener;
import org.kie.api.event.rule.AfterMatchFiredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tracks Rules activations
 * 
 * @author ajarrett
 */
public class RuleFiredEventListener extends DefaultAgendaEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger( RuleFiredEventListener.class );

    private List<String> activationList = new ArrayList<String>();

    @Override
    public void afterMatchFired( AfterMatchFiredEvent event ) {
        String sessionId = (String) event.getKieRuntime().getGlobal( "sessionId" );
        LOGGER.trace( "Rule fired: {} with ID : {}" , sessionId,
                event.getMatch().getRule().getName(),  event.getMatch().getRule().getId() );
        activationList.add( event.getMatch().getRule().getName() );
    }

    public boolean isRuleFired( String ruleName ) {
        for ( String rule : activationList ) {
            if ( ruleName.equalsIgnoreCase( rule ) ) {
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
