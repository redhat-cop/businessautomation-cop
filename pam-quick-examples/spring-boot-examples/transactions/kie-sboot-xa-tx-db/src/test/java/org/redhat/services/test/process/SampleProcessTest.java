package org.redhat.services.test.process;

import org.redhat.services.component.XATransactions;
import org.redhat.services.repository.TestRepository;
import org.redhat.services.test.util.KieTestUtilsIT;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.kie.server.api.model.instance.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.redhat.services.test.util.TestWorkflowConstant.PROCESS_ID.SAMPLE_PROCESS_ID;
import static org.redhat.services.test.util.TestWorkflowConstant.SAMPLE_PROCESS_DATA.TASK_OWNER;
import static org.junit.Assert.*;

/**
 * Test Various Persistence Scenarios w/Databases
 */
@Slf4j
public class SampleProcessTest extends KieTestUtilsIT {

    @Autowired
    TestRepository repository; // Spring DAO Repository

    @Autowired
    XATransactions transactionTest; // Service to orchestrate multiple Transactions events

    /**
     * Create Disclosure Process Instance with CreatorID Passed
     * 1. Creates new jBPM Instances
     * 2. Listener persists new record into database
     * 3. Records successfully created/persisted
     */
    @Test
    public void testNewSampleProcess() {

        // start process instance
        Long processInstanceId = processServicesClient.startProcess(containerId, SAMPLE_PROCESS_ID, this.getParams(false, true));

        assertNotNull(processInstanceId);
        this.assertTaskInstance(processInstanceId, 1, TASK_OWNER, "Test Task");
    }

    /**
     * 1. Creates new jBPM Instances
     * 2. Listener persists new record into database
     * 3. Exception thrown from Listener
     * 4. Everything Rolls Back
     */
    @Test
    public void testNewSampleProcessFailedXA() {

        long persistedEntities = repository.count();

        // start process instance
        try {
            Long processInstanceId = processServicesClient.startProcess(containerId, SAMPLE_PROCESS_ID, this.getParams(true, true));
        } catch (Exception e) {
            log.info("Exception thrown mid executions");
        }

        List<ProcessInstance> instances = queryServicesClient.findProcessInstancesByStatus(Arrays.asList(1), 0, 0);
        assertTrue(instances.isEmpty());
        assertEquals(persistedEntities, repository.count());
    }

    /**
     * 1. Persists to local database
     * 2. Creates new jBPM Instances
     * 3. Records successfully created/persisted
     */
    @Test
    public void testNewSampleProcessXAOutsideNoException() {

        long persistedEntities = repository.count();

        // start process instance
        try {
            transactionTest.transactionInitiatedBeforeOutsideJBPM(false, this.getParams(false, false));
        } catch (Exception e) {
            log.info("Exception thrown mid execution");
        }

        // Assert Increments on all sources
        List<ProcessInstance> instances = queryServicesClient.findProcessInstancesByStatus(Arrays.asList(1), 0, 0);
        assertEquals(instances.size(), 1);
        assertEquals(persistedEntities + 1, repository.count());
    }

    /**
     * 1. Persists to local database
     * 2. Creates new jBPM Instances
     * 3. Exception is thrown from method outside jBPM
     * 4. Everything rollsback
     */
    @Test
    public void testNewSampleProcessXAOutsideBeforeFail() {

        long persistedEntities = repository.count();

        // start process instance
        try {
            transactionTest.transactionInitiatedBeforeOutsideJBPM(true, this.getParams(false, false));
        } catch (Exception e) {
            log.info("Exception thrown mid execution");
        }

        // Assert Increments on all sources
        List<ProcessInstance> instances = queryServicesClient.findProcessInstancesByStatus(Arrays.asList(1), 0, 0);
        log.info(String.valueOf(Arrays.asList(instances)));
        assertTrue(instances.isEmpty());
        assertEquals(persistedEntities, repository.count());
    }

    /**
     * 1. Creates new jBPM Instances
     * 2. Persists to local database
     * 3. Exception is thrown from method outside jBPM
     * 4. Everything rollsback
     */
    @Test
    public void testNewSampleProcessXAOutsideAfterFail() {

        long persistedEntities = repository.count();

        // start process instance
        try {
            transactionTest.transactionInitiatedOutsideAfterJBPM(true, this.getParams(false, false));
        } catch (Exception e) {
            log.info("Exception thrown mid execution");
        }

        // Assert Increments on all sources
        List<ProcessInstance> instances = queryServicesClient.findProcessInstancesByStatus(Arrays.asList(1), 0, 0);
        log.info(String.valueOf(Arrays.asList(instances)));
        assertTrue(instances.isEmpty());
        assertEquals(persistedEntities, repository.count());
    }

    /**
     * 1. Persists to local database
     * 2. Creates new jBPM Instances
     * 2.1. Listener persists new record into database
     * 3. Exception is thrown from listener
     * 4. Everything rollsback
     */
    @Test
    public void testNewSampleProcessXABeforeWithListenerFail() {

        long persistedEntities = repository.count();

        // start process instance
        try {
            transactionTest.transactionInitiatedBeforeOutsideJBPM(false, this.getParams(true, true));
        } catch (Exception e) {
            log.info("Exception thrown mid execution");
        }

        // Assert Increments on all sources
        List<ProcessInstance> instances = queryServicesClient.findProcessInstancesByStatus(Arrays.asList(1), 0, 0);
        log.info(String.valueOf(Arrays.asList(instances)));
        assertTrue(instances.isEmpty());
        assertEquals(persistedEntities, repository.count());
    }

    /**
     * Test Payload
     * @param throwException
     * @param persistEntity
     * @return
     */
    private Map<String, Object> getParams(boolean throwException, boolean persistEntity){
        Map<String, Object> params = new HashMap<>();
        params.put("taskOwner", TASK_OWNER);
        params.put("throwException", throwException); // Throw Exception from Listener?
        params.put("persistEntity", persistEntity); // Invoke Persistence from Listener?
        return params;
    }
}
