package org.drools.bpmn2;

public class Drink_order_processProcessInstance extends org.kie.kogito.process.impl.AbstractProcessInstance<Drink_order_processModel> {

    public Drink_order_processProcessInstance(org.drools.bpmn2.Drink_order_processProcess process, Drink_order_processModel value, org.kie.api.runtime.process.ProcessRuntime processRuntime) {
        super(process, value, processRuntime);
    }

    protected java.util.Map<String, Object> bind(Drink_order_processModel variables) {
        return variables.toMap();
    }

    protected void unbind(Drink_order_processModel variables, java.util.Map<String, Object> vmap) {
        variables.fromMap(this.id(), vmap);
    }
}
