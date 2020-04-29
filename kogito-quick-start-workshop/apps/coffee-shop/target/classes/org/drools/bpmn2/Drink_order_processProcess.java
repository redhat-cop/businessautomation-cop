package org.drools.bpmn2;

import org.jbpm.process.core.datatype.impl.type.ObjectDataType;
import org.jbpm.ruleflow.core.RuleFlowProcessFactory;
import org.drools.core.util.KieFunctions;

@javax.enterprise.context.ApplicationScoped()
@javax.inject.Named("drink_order_process")
public class Drink_order_processProcess extends org.kie.kogito.process.impl.AbstractProcess<org.drools.bpmn2.Drink_order_processModel> {

    @javax.inject.Inject()
    javax.enterprise.inject.Instance<org.kie.api.runtime.process.WorkItemHandler> handlers;

    org.kie.kogito.app.Application app;

    @javax.inject.Inject()
    org.drools.bpmn2.Drink_order_processMessageProducer_2 producer_2;

    public Drink_order_processProcess() {
    }

    @javax.inject.Inject()
    public Drink_order_processProcess(org.kie.kogito.app.Application app) {
        super(app.config().process());
        this.app = app;
    }

    public org.drools.bpmn2.Drink_order_processProcessInstance createInstance(org.drools.bpmn2.Drink_order_processModel value) {
        return new org.drools.bpmn2.Drink_order_processProcessInstance(this, value, this.createLegacyProcessRuntime());
    }

    public org.drools.bpmn2.Drink_order_processModel createModel() {
        return new org.drools.bpmn2.Drink_order_processModel();
    }

    public org.drools.bpmn2.Drink_order_processProcessInstance createInstance(org.kie.kogito.Model value) {
        return this.createInstance((org.drools.bpmn2.Drink_order_processModel) value);
    }

    public Drink_order_processProcess configure() {
        super.configure();
        handlers.forEach(h -> {
            services.getWorkItemManager().registerWorkItemHandler(h.getName(), h);
        });
        return this;
    }

    protected void registerListeners() {
    }

    public org.kie.api.definition.process.Process legacyProcess() {
        RuleFlowProcessFactory factory = RuleFlowProcessFactory.createProcess("drink_order_process");
        factory.variable("order", new ObjectDataType("org.bala.drink.coffee.model.DrinkOrder"));
        factory.variable("process_state", new ObjectDataType("java.lang.Boolean"));
        factory.name("Drink Order Process");
        factory.packageName("org.drools.bpmn2");
        factory.dynamic(false);
        factory.version("1.0");
        factory.visibility("Public");
        factory.metaData("TargetNamespace", "http://www.omg.org/bpmn20");
        org.jbpm.ruleflow.core.factory.JoinFactory joinNode1 = factory.joinNode(1);
        joinNode1.name("Join");
        joinNode1.type(2);
        joinNode1.metaData("UniqueId", "_B9944B1B-FB77-4D0D-8EBF-97DE201FC267");
        joinNode1.metaData("x", 819);
        joinNode1.metaData("width", 56);
        joinNode1.metaData("y", 243);
        joinNode1.metaData("height", 56);
        joinNode1.done();
        org.jbpm.ruleflow.core.factory.EndNodeFactory endNode2 = factory.endNode(2);
        endNode2.name("Inform Barista");
        endNode2.terminate(false);
        endNode2.action(kcontext -> {
            producer_2.produce(kcontext.getProcessInstance(), (org.bala.drink.coffee.model.DrinkOrder) kcontext.getVariable("order"));
        });
        endNode2.metaData("UniqueId", "_F5372E8C-7723-493E-A523-B4B9FB4AA2AF");
        endNode2.metaData("elementname", "Inform Barista");
        endNode2.metaData("TriggerType", "ProduceMessage");
        endNode2.metaData("x", 659);
        endNode2.metaData("width", 56);
        endNode2.metaData("y", 243);
        endNode2.metaData("TriggerRef", "barista-process");
        endNode2.metaData("MappingVariable", "order");
        endNode2.metaData("MessageType", "org.bala.drink.coffee.model.DrinkOrder");
        endNode2.metaData("height", 56);
        endNode2.done();
        org.jbpm.ruleflow.core.factory.HumanTaskNodeFactory humanTaskNode3 = factory.humanTaskNode(3);
        humanTaskNode3.name("Request payment type change");
        humanTaskNode3.workParameter("TaskName", "payment_type_change");
        humanTaskNode3.workParameter("Skippable", "false");
        humanTaskNode3.workParameter("GroupId", "vendor");
        humanTaskNode3.workParameter("NodeName", "Request payment type change");
        humanTaskNode3.inMapping("order_in", "order");
        humanTaskNode3.outMapping("order_out", "order");
        humanTaskNode3.done();
        humanTaskNode3.metaData("UniqueId", "_7C47F387-49A0-4EAA-BF50-631F092D4623");
        humanTaskNode3.metaData("elementname", "Request payment type change");
        humanTaskNode3.metaData("x", 743);
        humanTaskNode3.metaData("width", 154);
        humanTaskNode3.metaData("y", 358);
        humanTaskNode3.metaData("height", 102);
        org.jbpm.ruleflow.core.factory.SplitFactory splitNode4 = factory.splitNode(4);
        splitNode4.name("Split");
        splitNode4.type(2);
        splitNode4.metaData("UniqueId", "_8644C4EC-BCCA-4718-B8A1-6D6C66413074");
        splitNode4.metaData("x", 1004);
        splitNode4.metaData("width", 56);
        splitNode4.metaData("y", 243);
        splitNode4.metaData("height", 56);
        splitNode4.constraint(3, "_C3B0365C-8F2F-42DB-8D46-3D4D411F527F", "DROOLS_DEFAULT", "java", kcontext -> {
            org.bala.drink.coffee.model.DrinkOrder order = (org.bala.drink.coffee.model.DrinkOrder) kcontext.getVariable("order");
            java.lang.Boolean process_state = (java.lang.Boolean) kcontext.getVariable("process_state");
            {
                return !process_state;
            }
        }, 0);
        splitNode4.constraint(1, "_65A0127F-D354-4B24-BF29-993E3469E1CB", "DROOLS_DEFAULT", "java", kcontext -> {
            org.bala.drink.coffee.model.DrinkOrder order = (org.bala.drink.coffee.model.DrinkOrder) kcontext.getVariable("order");
            java.lang.Boolean process_state = (java.lang.Boolean) kcontext.getVariable("process_state");
            {
                return process_state;
            }
        }, 0);
        splitNode4.done();
        org.jbpm.ruleflow.core.factory.WorkItemNodeFactory workItemNode5 = factory.workItemNode(5);
        workItemNode5.name("Process payment");
        workItemNode5.workName("org.bala.drink.coffee.service.PaymentsService.process");
        workItemNode5.workParameter("Interface", "org.bala.drink.coffee.service.PaymentsService");
        workItemNode5.workParameter("Operation", "process");
        workItemNode5.workParameter("interfaceImplementationRef", "org.bala.drink.coffee.service.PaymentsService");
        workItemNode5.workParameter("operationImplementationRef", "process");
        workItemNode5.workParameter("implementation", "Java");
        workItemNode5.inMapping("order-in", "order");
        workItemNode5.outMapping("process_state", "process_state");
        workItemNode5.done();
        workItemNode5.metaData("UniqueId", "_A710B96A-6EE1-43BB-A2EE-ACEE59F7479E");
        workItemNode5.metaData("Implementation", "Java");
        workItemNode5.metaData("elementname", "Process payment");
        workItemNode5.metaData("Type", "Service Task");
        workItemNode5.metaData("OperationRef", "_A710B96A-6EE1-43BB-A2EE-ACEE59F7479E_ServiceOperation");
        workItemNode5.metaData("x", 955);
        workItemNode5.metaData("width", 154);
        workItemNode5.metaData("y", 70);
        workItemNode5.metaData("height", 102);
        org.jbpm.ruleflow.core.factory.SplitFactory splitNode6 = factory.splitNode(6);
        splitNode6.name("Split");
        splitNode6.type(2);
        splitNode6.metaData("UniqueId", "_DE7D069C-0E57-4CDF-872A-4D1266D7A6C9");
        splitNode6.metaData("x", 819);
        splitNode6.metaData("width", 56);
        splitNode6.metaData("y", 93);
        splitNode6.metaData("height", 56);
        splitNode6.constraint(1, "_1A3E63E4-8E36-4043-9F7D-6BFFA8DAA8D5", "DROOLS_DEFAULT", "java", kcontext -> {
            org.bala.drink.coffee.model.DrinkOrder order = (org.bala.drink.coffee.model.DrinkOrder) kcontext.getVariable("order");
            java.lang.Boolean process_state = (java.lang.Boolean) kcontext.getVariable("process_state");
            {
                return (null == order.getCardPayment());
            }
        }, 0);
        splitNode6.constraint(5, "_649B777C-1EAF-4655-A0CA-4B6D787AE7DD", "DROOLS_DEFAULT", "java", kcontext -> {
            org.bala.drink.coffee.model.DrinkOrder order = (org.bala.drink.coffee.model.DrinkOrder) kcontext.getVariable("order");
            java.lang.Boolean process_state = (java.lang.Boolean) kcontext.getVariable("process_state");
            {
                return order.getCardPayment() != null;
            }
        }, 0);
        splitNode6.done();
        org.jbpm.ruleflow.core.factory.HumanTaskNodeFactory humanTaskNode7 = factory.humanTaskNode(7);
        humanTaskNode7.name("Make Payment");
        humanTaskNode7.workParameter("TaskName", "make_payment");
        humanTaskNode7.workParameter("Skippable", "true");
        humanTaskNode7.workParameter("GroupId", "managers");
        humanTaskNode7.workParameter("Priority", "1");
        humanTaskNode7.workParameter("NodeName", "Make Payment");
        humanTaskNode7.inMapping("order_in", "order");
        humanTaskNode7.outMapping("order_out", "order");
        humanTaskNode7.done();
        humanTaskNode7.metaData("UniqueId", "_EF90EB52-7E8D-434C-BA36-AE5A80668DC1");
        humanTaskNode7.metaData("elementname", "Make Payment");
        humanTaskNode7.metaData("x", 585);
        humanTaskNode7.metaData("width", 154);
        humanTaskNode7.metaData("y", 70);
        humanTaskNode7.metaData("height", 102);
        org.jbpm.ruleflow.core.factory.JoinFactory joinNode8 = factory.joinNode(8);
        joinNode8.name("Join");
        joinNode8.type(2);
        joinNode8.metaData("UniqueId", "_2D13DA15-96E7-4B76-9E10-ACD74AEF4854");
        joinNode8.metaData("x", 449);
        joinNode8.metaData("width", 56);
        joinNode8.metaData("y", 93);
        joinNode8.metaData("height", 56);
        joinNode8.done();
        org.jbpm.ruleflow.core.factory.HumanTaskNodeFactory humanTaskNode9 = factory.humanTaskNode(9);
        humanTaskNode9.name("Place Order");
        humanTaskNode9.workParameter("TaskName", "place_order");
        humanTaskNode9.workParameter("Skippable", "true");
        humanTaskNode9.workParameter("GroupId", "managers");
        humanTaskNode9.workParameter("Priority", "1");
        humanTaskNode9.workParameter("NodeName", "Place Order");
        humanTaskNode9.inMapping("order_in", "order");
        humanTaskNode9.outMapping("order_out", "order");
        humanTaskNode9.done();
        humanTaskNode9.metaData("UniqueId", "_256AC980-38D7-4B17-9B84-D254B0C67D41");
        humanTaskNode9.metaData("elementname", "Place Order");
        humanTaskNode9.metaData("x", 215);
        humanTaskNode9.metaData("width", 154);
        humanTaskNode9.metaData("y", 70);
        humanTaskNode9.metaData("height", 102);
        org.jbpm.ruleflow.core.factory.StartNodeFactory startNode10 = factory.startNode(10);
        startNode10.name("Start");
        startNode10.metaData("UniqueId", "_3464C926-3426-462C-ADB8-1192527800E6");
        startNode10.metaData("x", 79);
        startNode10.metaData("width", 56);
        startNode10.metaData("y", 93);
        startNode10.metaData("height", 56);
        startNode10.done();
        factory.connection(4, 1, "_65A0127F-D354-4B24-BF29-993E3469E1CB");
        factory.connection(6, 1, "_1A3E63E4-8E36-4043-9F7D-6BFFA8DAA8D5");
        factory.connection(1, 2, "_3E47DB22-AE08-4E2C-A52E-B416DE029DAD");
        factory.connection(4, 3, "_C3B0365C-8F2F-42DB-8D46-3D4D411F527F");
        factory.connection(5, 4, "_D8373713-DC38-4C4B-9EF7-BEE1E0D9B9DC");
        factory.connection(6, 5, "_649B777C-1EAF-4655-A0CA-4B6D787AE7DD");
        factory.connection(7, 6, "_F25A336A-E44C-449D-B33D-48052A861DE1");
        factory.connection(8, 7, "_08482519-0571-486D-8CA8-55D27187AA33");
        factory.connection(3, 8, "_34762314-EB4F-419B-954B-58B4943E92B3");
        factory.connection(9, 8, "_94672067-D0D9-4A61-9703-02FBC1FFE0FE");
        factory.connection(10, 9, "_9BA78565-01F0-443B-BC14-C4E6C2284D8A");
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
