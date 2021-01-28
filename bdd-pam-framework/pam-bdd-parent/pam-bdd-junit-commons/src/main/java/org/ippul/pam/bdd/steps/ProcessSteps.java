package org.ippul.pam.bdd.steps;

import com.google.inject.Inject;
import io.cucumber.datatable.DataTable;
import io.cucumber.java8.En;
import org.ippul.pam.bdd.context.BDDContext;
import org.junit.Assert;
import org.kie.api.runtime.manager.audit.NodeInstanceLog;
import org.kie.api.runtime.manager.audit.ProcessInstanceLog;
import org.kie.api.runtime.process.ProcessInstance;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ProcessSteps implements En {

    @Inject
    private BDDContext context;

    final Function<String, Integer> processStatusMapper = (processStatus) -> {
        Integer status = -1;
        switch (processStatus) {
            case "COMPLETED":
                status =  ProcessInstance.STATE_COMPLETED;
                break;
            case "ACTIVE":
                status =  ProcessInstance.STATE_ACTIVE;
                break;
            case "ABORTED":
                status =  ProcessInstance.STATE_ABORTED;
                break;
            case "PENDING":
                status =  ProcessInstance.STATE_PENDING;
                break;
            case "SUSPENDED":
                status =  ProcessInstance.STATE_SUSPENDED;
                break;
            default:
                fail("Process status must be one of [STATE_COMPLETED, STATE_ACTIVE, STATE_ABORTED, STATE_PENDING, STATE_SUSPENDED]");
        }
        return status;
    };
    final BiConsumer<String, Map<String, Object>> startProcess = (processDefinitionId, parameters) -> context.getKieSession().startProcess(processDefinitionId, parameters);

    public ProcessSteps() {
        Given("an instance of {string} is started using input parameters", //
                (final String processId, final DataTable parameters) ->  startProcess.accept(processId, parameters.asMap(String.class, Object.class)));

        Given("an instance of {string} is started", //
                (final String processId) -> startProcess.accept(processId, null));

        And("the signal {string} is send to the process instance with parameters", (final String signalName, final DataTable dataTable) -> {
            final Map<String, Object> filterMap = dataTable.asMap(String.class, Object.class);
            context.getKieSession()
                    .signalEvent(signalName, filterMap.get("signal.body"),context.getProcessInstanceId());
        });

        And("the signal {string} is send to the kie session with parameters", (final String signalName, final DataTable dataTable) -> {
            final Map<String, Object> filterMap = dataTable.asMap(String.class, Object.class);
            context.getKieSession().signalEvent(signalName, filterMap.get("signal.body"));
        });

        And("the signal {string} is send to the process instance", //
                (final String signalName) -> context.getKieSession().signalEvent(signalName, null, context.getProcessInstanceId()));

        And("the signal {string} is send to the kie session", //
                (final String signalName) -> context.getKieSession().signalEvent(signalName, null));

        And("the process instance status is {string}", (final String expectedStatus) -> {
            final ProcessInstanceLog processInstanceLog = context.getRuntimeEngine().getAuditService().findProcessInstance(context.getProcessInstanceId());
            assertEquals(processStatusMapper.apply(expectedStatus).intValue(), processInstanceLog.getStatus().intValue());
        });

        And("the subprocess {string} status is {string}", //
                (final String processId, final String expectedStatus) -> context.getRuntimeEngine().getAuditService().findSubProcessInstances(context.getProcessInstanceId()) //
                        .stream().filter(processInstance -> processInstance.getParentProcessInstanceId().equals(context.getProcessInstanceId())) //
                        .forEach(processInstance -> assertEquals(processStatusMapper.apply(expectedStatus).intValue(), processInstance.getStatus().intValue())));

        And("the process variables values are", //
                (final DataTable dataTable) -> dataTable.asMap(String.class, Object.class) //
                    .entrySet() //
                    .stream() //
                    .forEach(entry -> assertEquals(entry.getValue(), context.getProcessParameters().get(entry.getKey()))));


            And("the node {string} has been triggered", //
                (String expectedTriggeredNodeName) -> { //
            final Optional<? extends NodeInstanceLog> nodeInstanceLog =
                    context.getRuntimeEngine().getAuditService()
                            .findNodeInstances(context.getProcessInstanceId())
                            .stream()
                            .filter(l -> (l.getType() == NodeInstanceLog.TYPE_ENTER || l.getType() == NodeInstanceLog.TYPE_EXIT))
                            .filter(l -> expectedTriggeredNodeName.equals(l.getNodeName()))
                            .findFirst();
            Assert.assertTrue(nodeInstanceLog.isPresent());

        });

    }
}
