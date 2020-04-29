package org.kie.kogito.handlers;

@javax.enterprise.context.ApplicationScoped()
public class PaymentsService_processHandler implements org.kie.api.runtime.process.WorkItemHandler {

    @javax.inject.Inject()
    org.bala.drink.coffee.service.PaymentsService service;

    public void executeWorkItem(org.kie.api.runtime.process.WorkItem workItem, org.kie.api.runtime.process.WorkItemManager workItemManager) {
        java.lang.Object result = service.process((org.bala.drink.coffee.model.DrinkOrder) workItem.getParameter("order-in"));
        workItemManager.completeWorkItem(workItem.getId(), java.util.Collections.singletonMap("process_state", result));
    }

    public void abortWorkItem(org.kie.api.runtime.process.WorkItem workItem, org.kie.api.runtime.process.WorkItemManager workItemManager) {
    }

    public String getName() {
        return "org.bala.drink.coffee.service.PaymentsService.process";
    }
}
