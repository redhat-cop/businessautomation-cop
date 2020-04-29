package org.drools.bpmn2;

import org.kie.kogito.services.event.AbstractProcessDataEvent;

public class Drink_order_processMessageDataEvent_2 extends AbstractProcessDataEvent<org.bala.drink.coffee.model.DrinkOrder> {

    private String kogitoStartFromNode;

    public Drink_order_processMessageDataEvent_2() {
        super(null, null, null, null, null, null, null, null, null);
    }

    public Drink_order_processMessageDataEvent_2(String source, org.bala.drink.coffee.model.DrinkOrder body, String kogitoProcessinstanceId, String kogitoParentProcessinstanceId, String kogitoRootProcessinstanceId, String kogitoProcessId, String kogitoRootProcessId, String kogitoProcessinstanceState) {
        super(source, body, kogitoProcessinstanceId, kogitoParentProcessinstanceId, kogitoRootProcessinstanceId, kogitoProcessId, kogitoRootProcessId, kogitoProcessinstanceState, null);
    }

    public void setKogitoStartFromNode(String kogitoStartFromNode) {
        this.kogitoStartFromNode = kogitoStartFromNode;
    }

    public String getKogitoStartFromNode() {
        return this.kogitoStartFromNode;
    }
}
