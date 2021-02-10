package com.redhat.pam.bdd.steps;

import com.google.inject.Inject;
import com.redhat.pam.bdd.context.BDDContext;
import io.cucumber.datatable.DataTable;
import io.cucumber.java8.En;
import org.junit.Assert;
import org.kie.server.api.model.instance.NodeInstance;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.QueryServicesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ProcessSteps implements En {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessSteps.class);

    @Inject
    private BDDContext context;

    final Function<String, Integer> processStatusMapper = (processStatus) -> {
        Integer status = -1;
        switch (processStatus) {
            case "COMPLETED":
                status =  org.kie.api.runtime.process.ProcessInstance.STATE_COMPLETED;
                break;
            case "ACTIVE":
                status =  org.kie.api.runtime.process.ProcessInstance.STATE_ACTIVE;
                break;
            case "ABORTED":
                status =  org.kie.api.runtime.process.ProcessInstance.STATE_ABORTED;
                break;
            case "PENDING":
                status =  org.kie.api.runtime.process.ProcessInstance.STATE_PENDING;
                break;
            case "SUSPENDED":
                status =  org.kie.api.runtime.process.ProcessInstance.STATE_SUSPENDED;
                break;
            default:
                fail("Process status must be one of [COMPLETED, ACTIVE, ABORTED, PENDING, SUSPENDED]");
        }
        return status;
    };
    final BiConsumer<String, Map<String, Object>> startProcess = (processDefinitionId, parameters) -> context.getServicesClient(ProcessServicesClient.class).startProcess(context.getReleaseId().toExternalForm(), processDefinitionId, parameters);

    public ProcessSteps() {
        Given("an instance of {string} is started using input parameters", //
                (final String processId, final DataTable parameters) ->  startProcess.accept(processId, parameters.asMap(String.class, Object.class)));

        Given("an instance of {string} is started", //
                (final String processId) -> startProcess.accept(processId, null));

        And("the signal {string} is send to the process instance with parameters", (final String signalName, final DataTable dataTable) -> {
            final Map<String, Object> filterMap = dataTable.asMap(String.class, Object.class);
            context.getServicesClient(ProcessServicesClient.class)
                    .signalProcessInstance(context.getReleaseId().toExternalForm(), context.getProcessInstanceId(), signalName, filterMap.get("signal.body"));
        });

        And("the signal {string} is send to the kie session with parameters", (final String signalName, final DataTable dataTable) -> {
            final Map<String, Object> filterMap = dataTable.asMap(String.class, Object.class);
            context.getServicesClient(ProcessServicesClient.class).signal(context.getReleaseId().toExternalForm(), signalName, filterMap.get("signal.body"));
        });

        And("the signal {string} is send to the process instance", //
                (final String signalName) ->
                        context.getServicesClient(ProcessServicesClient.class)
                                .signalProcessInstance(context.getReleaseId().toExternalForm(), context.getProcessInstanceId(), signalName, null));

        And("the signal {string} is send to the kie session", //
                (final String signalName) -> context.getServicesClient(ProcessServicesClient.class).signal(context.getReleaseId().toExternalForm(), signalName, null));

        And("the process instance status is {string}", (final String expectedStatus) -> {
            final ProcessInstance processInstance = context.getServicesClient(QueryServicesClient.class).findProcessInstanceById(context.getProcessInstanceId());
            assertEquals(processStatusMapper.apply(expectedStatus).intValue(), processInstance.getState().intValue());
        });

        And("the subprocess {string} status is {string}", //
                (final String processId, final String expectedStatus) -> context.getServicesClient(ProcessServicesClient.class) //
                                .findProcessInstancesByParent(context.getReleaseId().toExternalForm(), context.getProcessInstanceId(), 0, Integer.MAX_VALUE) //
                                .forEach(processInstance -> assertEquals(processStatusMapper.apply(expectedStatus).intValue(), processInstance.getState().intValue())));

        And("the process variables values are", //
                (final DataTable dataTable) -> {
                    final ProcessInstance processInstance = context.getServicesClient(QueryServicesClient.class).findProcessInstanceById(context.getProcessInstanceId());
                    dataTable.asMap(String.class, Object.class) //
                                    .entrySet() //
                                    .stream() //
                                    .forEach(entry -> assertEquals(entry.getValue(), processInstance.getVariables().get(entry.getKey())));
                });

        And("the node {string} has been triggered", //
                (String expectedTriggeredNodeName) -> { //
                    final Optional<? extends NodeInstance> nodeInstanceLog =
                            context.getServicesClient(QueryServicesClient.class).findNodeInstances(context.getProcessInstanceId(), 0, Integer.MAX_VALUE)
                                    .stream()
                                    .filter(l -> expectedTriggeredNodeName.equals(l.getName()))
                                    .findFirst();
                    Assert.assertTrue(nodeInstanceLog.isPresent());
                });
    }
}
