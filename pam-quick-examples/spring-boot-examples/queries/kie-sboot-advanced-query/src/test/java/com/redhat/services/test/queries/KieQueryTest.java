package com.redhat.services.test.queries;

import com.redhat.services.test.util.KieTestUtilsIT;
import org.junit.Test;
import org.kie.server.api.model.definition.QueryDefinition;
import org.kie.server.api.model.instance.TaskSummary;

import java.util.HashMap;
import java.util.Map;

import static com.redhat.services.test.util.TestWorkflowConstant.PROCESS_ID.SAMPLE_PROCESS_ID;
import static com.redhat.services.test.util.TestWorkflowConstant.SAMPLE_PROCESS_DATA.TASK_OWNER;
import static org.junit.Assert.assertNotNull;

public class KieQueryTest extends KieTestUtilsIT {

    @Test
    public void testGetAllTaskInputInstancesWithVariables() {

        // Source File : src/main/resources/query-definitions.json
        QueryDefinition query = queryServicesClient.getQuery("getAllTaskInputInstancesWithVariables");
        assertNotNull(query);

        Map<String, Object> params = this.getParams();
        params.put("myVar", "testing123");

        // start process instance
        Long processInstanceId = processServicesClient.startProcess(containerId, SAMPLE_PROCESS_ID, params);

        assertNotNull(processInstanceId);
        TaskSummary task = this.assertTaskInstance(processInstanceId, 1, TASK_OWNER, "Test Task");

        

    }

    @Test
    public void testGetAllProcessInstancesWithVariables() {

        // Source File : src/main/resources/query-definitions.json
        QueryDefinition query = queryServicesClient.getQuery("getAllProcessInstancesWithVariables");
        assertNotNull(query);

    }

    @Test
    public void testGetTasksForProcessInstance() {

        // Source File : src/main/resources/query-definitions.json
        QueryDefinition query = queryServicesClient.getQuery("getTasksForProcessInstance");
        assertNotNull(query);

    }

    /**
     * Test Payload
     *
     * @param throwException
     * @param persistEntity
     * @return
     */
    private Map<String, Object> getParams() {
        Map<String, Object> params = new HashMap<>();
        params.put("taskOwner", TASK_OWNER);
        return params;
    }
}
