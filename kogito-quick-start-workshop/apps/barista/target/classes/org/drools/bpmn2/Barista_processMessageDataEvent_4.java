package org.drools.bpmn2;

import org.kie.kogito.services.event.AbstractProcessDataEvent;

public class Barista_processMessageDataEvent_4 extends AbstractProcessDataEvent<org.bala.drink.coffee.model.DrinkOrder> {

    private String kogitoStartFromNode;

    public Barista_processMessageDataEvent_4() {
        super(null, null, null, null, null, null, null, null, null);
    }

    public Barista_processMessageDataEvent_4(String source, org.bala.drink.coffee.model.DrinkOrder body, String kogitoProcessinstanceId, String kogitoParentProcessinstanceId, String kogitoRootProcessinstanceId, String kogitoProcessId, String kogitoRootProcessId, String kogitoProcessinstanceState) {
        super(source, body, kogitoProcessinstanceId, kogitoParentProcessinstanceId, kogitoRootProcessinstanceId, kogitoProcessId, kogitoRootProcessId, kogitoProcessinstanceState, null);
    }

    public void setKogitoStartFromNode(String kogitoStartFromNode) {
        this.kogitoStartFromNode = kogitoStartFromNode;
    }

    public String getKogitoStartFromNode() {
        return this.kogitoStartFromNode;
    }
}
