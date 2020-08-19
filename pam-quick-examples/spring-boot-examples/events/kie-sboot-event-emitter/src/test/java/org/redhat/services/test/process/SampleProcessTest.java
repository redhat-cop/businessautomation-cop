package org.redhat.services.test.process;

import org.redhat.services.test.util.KieTestUtilsIT;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.TaskSummary;

import java.util.HashMap;
import java.util.Map;

import static org.redhat.services.test.util.TestWorkflowConstant.PROCESS_ID.SAMPLE_PROCESS_ID;
import static org.redhat.services.test.util.TestWorkflowConstant.SAMPLE_PROCESS_DATA.TASK_OWNER;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test Various Persistence Scenarios w/Databases
 */
@Slf4j
public class SampleProcessTest extends KieTestUtilsIT {

    /**
     * Create Process Instance
     * 1. Creates new jBPM Instances
     * 2. Complete Task Instance
     * 3. Assert Process Complete
     */
    @Test
    public void testNewSampleProcess() {

        // start process instance
        Long processInstanceId = processServicesClient.startProcess(containerId, SAMPLE_PROCESS_ID, this.getParams(false, true));

        assertNotNull(processInstanceId);
        TaskSummary task = this.assertTaskInstance(processInstanceId, 1, TASK_OWNER, "Test Task");

        // Claim, Start and Complete task in 1 API call
        userTaskServicesClient.completeAutoProgress(containerId, task.getId(), TASK_OWNER, null);

        // Claim, Start and Complete task in 1 API call
        this.assertTaskInstance(processInstanceId, 0, null, null);

        ProcessInstance pi = processServicesClient.getProcessInstance(containerId, processInstanceId);
        assertTrue(pi.getState().equals(org.kie.api.runtime.process.ProcessInstance.STATE_COMPLETED));
    }

    /**
     * Test Payload
     *
     * @param throwException
     * @param persistEntity
     * @return
     */
    private Map<String, Object> getParams(boolean throwException, boolean persistEntity) {
        Map<String, Object> params = new HashMap<>();
        params.put("taskOwner", TASK_OWNER);
        params.put("throwException", throwException); // Throw Exception from Listener?
        params.put("persistEntity", persistEntity); // Invoke Persistence from Listener?
        return params;
    }
}
