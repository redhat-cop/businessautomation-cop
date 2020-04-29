package org.drools.bpmn2;

public class Barista_processProcessInstance extends org.kie.kogito.process.impl.AbstractProcessInstance<Barista_processModel> {

    public Barista_processProcessInstance(org.drools.bpmn2.Barista_processProcess process, Barista_processModel value, org.kie.api.runtime.process.ProcessRuntime processRuntime) {
        super(process, value, processRuntime);
    }

    protected java.util.Map<String, Object> bind(Barista_processModel variables) {
        return variables.toMap();
    }

    protected void unbind(Barista_processModel variables, java.util.Map<String, Object> vmap) {
        variables.fromMap(this.id(), vmap);
    }
}
