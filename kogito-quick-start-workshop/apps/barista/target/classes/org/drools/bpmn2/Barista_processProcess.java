package org.drools.bpmn2;

import org.jbpm.process.core.datatype.impl.type.ObjectDataType;
import org.jbpm.ruleflow.core.RuleFlowProcessFactory;
import org.drools.core.util.KieFunctions;

@javax.enterprise.context.ApplicationScoped()
@javax.inject.Named("barista_process")
public class Barista_processProcess extends org.kie.kogito.process.impl.AbstractProcess<org.drools.bpmn2.Barista_processModel> {

    @javax.inject.Inject()
    javax.enterprise.inject.Instance<org.kie.api.runtime.process.WorkItemHandler> handlers;

    org.kie.kogito.app.Application app;

    public Barista_processProcess() {
    }

    @javax.inject.Inject()
    public Barista_processProcess(org.kie.kogito.app.Application app) {
        super(app.config().process());
        this.app = app;
    }

    public org.drools.bpmn2.Barista_processProcessInstance createInstance(org.drools.bpmn2.Barista_processModel value) {
        return new org.drools.bpmn2.Barista_processProcessInstance(this, value, this.createLegacyProcessRuntime());
    }

    public org.drools.bpmn2.Barista_processModel createModel() {
        return new org.drools.bpmn2.Barista_processModel();
    }

    public org.drools.bpmn2.Barista_processProcessInstance createInstance(org.kie.kogito.Model value) {
        return this.createInstance((org.drools.bpmn2.Barista_processModel) value);
    }

    public Barista_processProcess configure() {
        super.configure();
        return this;
    }

    protected void registerListeners() {
    }

    public org.kie.api.definition.process.Process legacyProcess() {
        RuleFlowProcessFactory factory = RuleFlowProcessFactory.createProcess("barista_process");
        factory.variable("order", new ObjectDataType("org.bala.drink.coffee.model.DrinkOrder"));
        factory.name("Barista Process");
        factory.packageName("org.drools.bpmn2");
        factory.dynamic(false);
        factory.version("1.0");
        factory.visibility("Public");
        factory.metaData("TargetNamespace", "http://www.omg.org/bpmn20");
        org.jbpm.ruleflow.core.factory.EndNodeFactory endNode1 = factory.endNode(1);
        endNode1.name("End");
        endNode1.terminate(false);
        endNode1.metaData("UniqueId", "_CFCAD78E-BE9B-4BBD-BA91-9CC8F1B2A7D0");
        endNode1.metaData("x", 683);
        endNode1.metaData("width", 56);
        endNode1.metaData("y", 93);
        endNode1.metaData("height", 56);
        endNode1.done();
        org.jbpm.ruleflow.core.factory.HumanTaskNodeFactory humanTaskNode2 = factory.humanTaskNode(2);
        humanTaskNode2.name("Collect Drink");
        humanTaskNode2.workParameter("TaskName", "collect_drink");
        humanTaskNode2.workParameter("Skippable", "false");
        humanTaskNode2.workParameter("GroupId", "customer");
        humanTaskNode2.workParameter("NodeName", "Collect Drink");
        humanTaskNode2.inMapping("order", "order");
        humanTaskNode2.done();
        humanTaskNode2.metaData("UniqueId", "_F42B85F1-C9D1-4157-AA3F-8368B03C3032");
        humanTaskNode2.metaData("elementname", "Collect Drink");
        humanTaskNode2.metaData("x", 449);
        humanTaskNode2.metaData("width", 154);
        humanTaskNode2.metaData("y", 70);
        humanTaskNode2.metaData("height", 102);
        org.jbpm.ruleflow.core.factory.HumanTaskNodeFactory humanTaskNode3 = factory.humanTaskNode(3);
        humanTaskNode3.name("Prepare Drink");
        humanTaskNode3.workParameter("TaskName", "prepare_drink");
        humanTaskNode3.workParameter("Skippable", "false");
        humanTaskNode3.workParameter("GroupId", "barista");
        humanTaskNode3.workParameter("NodeName", "Prepare Drink");
        humanTaskNode3.inMapping("order_in", "order");
        humanTaskNode3.outMapping("order_out", "order");
        humanTaskNode3.done();
        humanTaskNode3.metaData("UniqueId", "_256AC980-38D7-4B17-9B84-D254B0C67D41");
        humanTaskNode3.metaData("elementname", "Prepare Drink");
        humanTaskNode3.metaData("x", 215);
        humanTaskNode3.metaData("width", 154);
        humanTaskNode3.metaData("y", 70);
        humanTaskNode3.metaData("height", 102);
        org.jbpm.ruleflow.core.factory.StartNodeFactory startNode4 = factory.startNode(4);
        startNode4.name("Start");
        startNode4.metaData("TriggerMapping", "order");
        startNode4.metaData("UniqueId", "_3464C926-3426-462C-ADB8-1192527800E6");
        startNode4.metaData("TriggerType", "ConsumeMessage");
        startNode4.metaData("x", 79);
        startNode4.metaData("width", 56);
        startNode4.metaData("y", 93);
        startNode4.metaData("TriggerRef", "barista-process");
        startNode4.metaData("MessageType", "org.bala.drink.coffee.model.DrinkOrder");
        startNode4.metaData("height", 56);
        startNode4.done();
        startNode4.trigger("barista-process", "order");
        factory.connection(2, 1, "_63565076-D9C5-4CE2-A3E4-919F7F21CBB1");
        factory.connection(3, 2, "_9283E5C6-30C0-4BEF-A889-BA62ECE9072A");
        factory.connection(4, 3, "_9BA78565-01F0-443B-BC14-C4E6C2284D8A");
        factory.validate();
        return factory.getProcess();
    }

    @javax.inject.Inject()
    public void setProcessInstancesFactory(org.kie.kogito.process.ProcessInstancesFactory processInstancesFactory) {
        super.setProcessInstancesFactory(processInstancesFactory);
    }

    public void init(@javax.enterprise.event.Observes() io.quarkus.runtime.StartupEvent event) {
        this.configure();
    }
}
