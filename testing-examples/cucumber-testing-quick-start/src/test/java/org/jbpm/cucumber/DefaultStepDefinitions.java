package org.jbpm.cucumber;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class DefaultStepDefinitions {
    
    private JbpmTestUtil testUtil;

    public DefaultStepDefinitions(JbpmTestUtil testUtil) {
        assertNotNull(testUtil);
        this.testUtil = testUtil;
    }

    @Before
    public void cucumberBefore() throws Exception {
        testUtil.setUp();
    }

    @After
    public void cucumberAfter() throws Exception {
        testUtil.tearDown();
    }

    @Given("the following process files:")
    public void addBPMNs(List<String> bpmns) {
        assertTrue(bpmns.size() > 0);
        testUtil.initialize(bpmns);
        assertNotNull(testUtil.getKieSession());
    }

    @Given("the process definition ID {string}")
    public void useProcessDefinition(String processId) {
        boolean hasDefinition = testUtil.useProcessDefinition(processId);
        assertTrue(hasDefinition);
    }

    @Given("a process parameter {string} with value {string}")
    public void useStringParameter(String name, String value) {
        testUtil.addProcessParam(name, value);
    }

    @When("the process is started")
    public void startProcess() {
        assertNotNull(testUtil.getKieSession());
        assertNotNull(testUtil.getProcessId());
        testUtil.startProcess();
        assertNotNull(testUtil.getProcessInstance());
    }

    @Then("the current node is {string}")
    public void currentNode(String nodeName) {
        assertNotNull(testUtil.getKieSession());
        assertNotNull(testUtil.getProcessInstance());
        testUtil.assertNodeActive(testUtil.getProcessInstance().getId(), testUtil.getKieSession(), nodeName);
    }

    @Then("the following nodes were triggered:")
    public void nodesTriggered(List<String> nodeNames) {
        assertNotNull(testUtil.getProcessInstance());
        testUtil.assertNodeTriggered(testUtil.getProcessInstance().getId(), nodeNames.toArray(new String[nodeNames.size()]));
    }

    @Then("the process completed")
    public void processCompleted() {
        assertNotNull(testUtil.getProcessInstance());
        testUtil.assertProcessInstanceCompleted(testUtil.getProcessInstance().getId());
    }
    
    // tasks
    @When("user {string} completes the human task {string}")
    public void completeHumanTask (String userId, String taskName) {
        assertNotNull(testUtil.getProcessInstance());
        assertEquals(true, testUtil.completeHumanTask(taskName, userId, new HashMap<>()));
    }

    @Then("user {string} can claim the human task {string}")
    public void isHumanTaskClaimableByTrue (String userId, String taskName) {
        assertNotNull(testUtil.getProcessInstance());
        assertEquals(true, testUtil.isHumanTaskClaimableBy(taskName, userId));
    }

    @Then("user {string} cannot claim the human task {string}")
    public void isHumanTaskClaimableByFalse (String userId, String taskName) {
        assertNotNull(testUtil.getProcessInstance());
        assertEquals(false, testUtil.isHumanTaskClaimableBy(taskName, userId));
    }

    @Then("user {string} is assigned the human task {string}")
    public void isHumanTaskAssignedToTrue (String userId, String taskName) {
        assertNotNull(testUtil.getProcessInstance());
        assertEquals(true, testUtil.isHumanTaskAssignedTo(taskName, userId));
    }

    @Then("user {string} is not assigned the human task {string}")
    public void isHumanTaskAssignedToFalse (String userId, String taskName) {
        assertNotNull(testUtil.getProcessInstance());
        assertEquals(false, testUtil.isHumanTaskAssignedTo(taskName, userId));
    }
}
