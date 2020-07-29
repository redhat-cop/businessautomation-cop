package org.redhat.services.test.queries;

import org.redhat.services.model.Person;
import org.redhat.services.test.util.TestWorkflowConstant;
import org.redhat.services.test.util.KieTestUtilsIT;
import org.apache.commons.lang3.StringUtils;
import org.drools.persistence.jpa.marshaller.MappedVariable;
import org.jbpm.kie.services.impl.query.mapper.ProcessInstanceWithVarsQueryMapper;
import org.jbpm.kie.services.impl.query.mapper.UserTaskInstanceWithVarsQueryMapper;
import org.jbpm.services.api.model.ProcessInstanceWithVarsDesc;
import org.jbpm.services.api.model.UserTaskInstanceWithVarsDesc;
import org.jbpm.services.api.query.QueryService;
import org.jbpm.services.api.query.model.QueryParam;
import org.junit.Test;
import org.kie.internal.query.QueryContext;
import org.kie.server.api.model.definition.QueryDefinition;
import org.kie.server.api.model.definition.QueryFilterSpec;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.TaskInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.api.util.QueryFilterSpecBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.jbpm.services.api.query.QueryResultMapper.COLUMN_STATUS;
import static org.jbpm.services.api.query.QueryResultMapper.COLUMN_TASK_STATUS;
import static org.junit.Assert.*;
import static org.kie.api.runtime.process.ProcessInstance.STATE_ACTIVE;
import static org.kie.server.client.QueryServicesClient.*;

public class KieQueryTest extends KieTestUtilsIT {

    /**
     * NOTE: jBPM Codebase test examples:
     * Examples of TaskSearch Advanced Queries: https://github.com/kiegroup/droolsjbpm-integration/blob/master/kie-server-parent/kie-server-tests/kie-server-integ-tests-jbpm/src/test/java/org/kie/server/integrationtests/jbpm/TaskSearchServiceIntegrationTest.java
     * Examples of ProcessSearch Advanced Queries: https://github.com/kiegroup/droolsjbpm-integration/blob/master/kie-server-parent/kie-server-tests/kie-server-integ-tests-jbpm/src/test/java/org/kie/server/integrationtests/jbpm/ProcessSearchServiceIntegrationTest.java
     */

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    QueryService queryService;

    /**
     * Basic AdvancedQuery Example Finding Tasks with a InputVariable + Value
     */
    @Test
    public void testGetAllTaskInputInstancesWithVariables() {

        // Source File : src/main/resources/query-definitions.json
        QueryDefinition query = queryServicesClient.getQuery(TestWorkflowConstant.QUERIES.ALL_TASKS_WITH_INPUT_VARS);
        assertNotNull(query);
        assertTrue(StringUtils.isNotBlank(query.getExpression()));

        // start process instance
        Long processInstanceId = processServicesClient.startProcess(containerId, TestWorkflowConstant.PROCESS_ID.SAMPLE_PROCESS_ID, this.getParams());
        assertNotNull(processInstanceId);

        TaskSummary task = this.assertTaskInstance(processInstanceId, 1, TestWorkflowConstant.SAMPLE_PROCESS_DATA.TASK_OWNER, "Test Task");


        // Obtain Task Instances via the Internal Java API
        List<UserTaskInstanceWithVarsDesc> tins = queryService.query(TestWorkflowConstant.QUERIES.ALL_TASKS_WITH_INPUT_VARS, UserTaskInstanceWithVarsQueryMapper.get(),
                new QueryContext(), QueryParam.in(COLUMN_TASK_STATUS, TestWorkflowConstant.getActiveTaskStatuses()));
        assertNotNull(tins);
        assertEquals(1, tins.size());
        assertEquals(this.getParams().get("myVar"), tins.get(0).getVariables().get("myTaskInputVar"));

        // User FilterSpec builder to filter SQL results
        QueryFilterSpecBuilder builder = new QueryFilterSpecBuilder();
        builder.in(COLUMN_TASK_STATUS, TestWorkflowConstant.getActiveTaskStatuses());

        // Obtain Task Instances via the External Java API QueryServices
        List<TaskInstance> taskInstanceLogs = queryServicesClient.query(TestWorkflowConstant.QUERIES.ALL_TASKS_WITH_INPUT_VARS, QUERY_MAP_TASK_WITH_VARS, builder.get(), 0, 1000, TaskInstance.class);
        assertNotNull(taskInstanceLogs);
        assertEquals(1, taskInstanceLogs.size());
        assertEquals(this.getParams().get("myVar"), taskInstanceLogs.get(0).getInputData().get("myTaskInputVar"));

    }

    /**
     * Test JPA Objects are persisted to the jBPM (local) Schema
     */
    @Test
    public void testLocalProcessVariablePersistence() throws InterruptedException {

        Person person = new Person();
        person.setAge(20);
        person.setName("Joe");
        person.setSurname("Bloggs");

        Map<String, Object> params = this.getParams();
        params.put("person", person);

        // start process instance
        Long processInstanceId = processServicesClient.startProcess(containerId, TestWorkflowConstant.PROCESS_ID.PVP_PROCESS_ID, params);
        assertNotNull(processInstanceId);

        TaskSummary task = this.assertTaskInstance(processInstanceId, 1, TestWorkflowConstant.SAMPLE_PROCESS_DATA.TASK_OWNER, "Task");

        // Obtain Task Instances via the Internal Java API
        List<UserTaskInstanceWithVarsDesc> tins = queryService.query(TestWorkflowConstant.QUERIES.ALL_TASKS_WITH_INPUT_VARS, UserTaskInstanceWithVarsQueryMapper.get(),
                new QueryContext(), QueryParam.in(COLUMN_TASK_STATUS, TestWorkflowConstant.getActiveTaskStatuses()));
        assertNotNull(tins);
        assertEquals(1, tins.size());
        assertEquals(1, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM PERSON", Integer.class).intValue());

        MappedVariable mv = jdbcTemplate.queryForObject(
                "SELECT * FROM MAPPEDVARIABLE WHERE PROCESSINSTANCEID = ? AND TASKID = ?",
                new Object[]{processInstanceId, task.getId()},
                new RowMapper<MappedVariable>() {
                    public MappedVariable mapRow(ResultSet rs, int rowNum) throws SQLException {
                        MappedVariable mv = new MappedVariable();
                        mv.setMappedVarId(rs.getLong("mappedVarId"));
                        mv.setProcessInstanceId(rs.getLong("processInstanceId"));
                        mv.setVariableId(rs.getLong("variableId"));
                        mv.setTaskId(rs.getLong("taskId"));
                        mv.setVariableType(rs.getString("variableType"));
                        mv.setWorkItemId(rs.getLong("workItemId"));
                        return mv;
                    }
                });

        assertNotNull(mv);
        assertEquals(mv.getProcessInstanceId(), processInstanceId);
        assertEquals(mv.getTaskId(), task.getId());
        assertTrue(mv.getVariableId() > 0);

        Person p = jdbcTemplate.queryForObject(
                "SELECT P.* FROM PERSON P WHERE P.ID = ? ",
                new Object[]{mv.getVariableId()},
                new RowMapper<Person>() {
                    public Person mapRow(ResultSet rs, int rowNum) throws SQLException {
                        Person p = new Person();
                        p.setAge(rs.getInt("age"));
                        p.setName(rs.getString("name"));
                        p.setSurname(rs.getString("surname"));
                        return p;
                    }
                });
        assertEquals(person.getAge(), p.getAge());
        assertEquals(person.getName(), p.getName());
        assertEquals(person.getSurname(), p.getSurname());


        Person person2 = new Person();
        person2.setAge(17);
        person2.setName("Joe");
        person2.setSurname("Cloggs");

        params = this.getParams();
        params.put("person", person2);

        // start process instance
        Long processInstanceId2 = processServicesClient.startProcess(containerId, TestWorkflowConstant.PROCESS_ID.PVP_PROCESS_ID, params);
        tins = queryService.query(TestWorkflowConstant.QUERIES.ALL_TASKS_WITH_INPUT_VARS, UserTaskInstanceWithVarsQueryMapper.get(),
                new QueryContext(), QueryParam.in(COLUMN_TASK_STATUS, TestWorkflowConstant.getActiveTaskStatuses()));
        assertNotNull(tins);
        assertEquals(2, tins.size());

        QueryFilterSpec spec = new QueryFilterSpecBuilder()
                .greaterThan("person_age", 18)
                .equalsTo("person_name", "Joe")
                .in("person_surname", Arrays.asList("Bloggs", "Cloggs"))
                .addColumnMapping("PERSON_AGE", "integer")
                .addColumnMapping("PERSON_NAME", "string")
                .addColumnMapping("PERSON_SURNAME", "string")
                .get();

        System.out.println("===================");
        List<TaskInstance> taskInstanceLogs = queryServicesClient.query(TestWorkflowConstant.QUERIES.ALL_TASKS_WITH_PERSON_VAR, QUERY_MAP_TASK_WITH_CUSTOM_VARS, spec, 0, 1000, TaskInstance.class);
        System.out.println("===================");

        assertNotNull(taskInstanceLogs);
        assertEquals(1, taskInstanceLogs.size());

    }

    /**
     * Basic AdvancedQuery Example Finding Processes with a Variable + Value
     */
    @Test
    public void testGetAllProcessInstancesWithVariables() {

        // Source File : src/main/resources/query-definitions.json
        QueryDefinition query = queryServicesClient.getQuery(TestWorkflowConstant.QUERIES.ALL_PROCESSES_WITH_VARS);
        assertNotNull(query);
        assertTrue(StringUtils.isNotBlank(query.getExpression()));

        // start process instance
        Long processInstanceId1 = processServicesClient.startProcess(containerId, TestWorkflowConstant.PROCESS_ID.SAMPLE_PROCESS_ID, this.getParams());

        // Obtain Process Instances via the Internal Java API
        List<ProcessInstanceWithVarsDesc> pils = queryService.query(TestWorkflowConstant.QUERIES.ALL_PROCESSES_WITH_VARS, ProcessInstanceWithVarsQueryMapper.get(), new QueryContext(),
                QueryParam.equalsTo(COLUMN_STATUS, STATE_ACTIVE));
        assertNotNull(pils);
        assertEquals(1, pils.size());
        assertEquals(this.getParams().get("myVar"), pils.get(0).getVariables().get("myVar"));

        QueryFilterSpecBuilder builder = new QueryFilterSpecBuilder();
        builder.equalsTo(COLUMN_STATUS, STATE_ACTIVE);

        // Obtain Process Instances via the External Java API QueryServices
        List<ProcessInstance> processInstanceLogs = queryServicesClient.query(TestWorkflowConstant.QUERIES.ALL_PROCESSES_WITH_VARS, QUERY_MAP_PI_WITH_VARS, builder.get(), 0, 1000, ProcessInstance.class);
        assertNotNull(processInstanceLogs);
        assertEquals(1, processInstanceLogs.size());
        assertEquals(this.getParams().get("myVar"), pils.get(0).getVariables().get("myVar"));
    }


    /**
     * Filtered Advanced Query Finding Processes with a Variable + Value 'likeTo'
     */
    @Test
    public void testGetAllProcessInstancesWithVariablesFiltered() {
        // Source File : src/main/resources/query-definitions.json
        QueryDefinition query = queryServicesClient.getQuery(TestWorkflowConstant.QUERIES.ALL_PROCESSES_WITH_VARS);
        assertNotNull(query);
        assertTrue(StringUtils.isNotBlank(query.getExpression()));

        // start multi process instance
        Map<String, Object> params = this.getParams();
        Long processInstanceId1 = processServicesClient.startProcess(containerId, TestWorkflowConstant.PROCESS_ID.SAMPLE_PROCESS_ID, params);
        params.put("myVar", "hello123");
        Long processInstanceId2 = processServicesClient.startProcess(containerId, TestWorkflowConstant.PROCESS_ID.SAMPLE_PROCESS_ID, params);

        QueryFilterSpecBuilder builder = new QueryFilterSpecBuilder();
        builder.equalsTo(COLUMN_STATUS, STATE_ACTIVE);

        // Obtain Process Instances via the External Java API QueryServices
        List<ProcessInstance> processInstanceLogs = queryServicesClient.query(TestWorkflowConstant.QUERIES.ALL_PROCESSES_WITH_VARS, QUERY_MAP_PI_WITH_VARS, builder.get(), 0, 1000, ProcessInstance.class);
        assertNotNull(processInstanceLogs);
        assertEquals(2, processInstanceLogs.size());

        // User FilterSpec builder to filter SQL results
        builder.in("variableid", Arrays.asList("myVar"));
        builder.equalsTo("value", "hello123");

        processInstanceLogs = queryServicesClient.query(TestWorkflowConstant.QUERIES.ALL_PROCESSES_WITH_VARS, QUERY_MAP_PI_WITH_VARS, builder.get(), 0, 1000, ProcessInstance.class);
        assertNotNull(processInstanceLogs);
        assertEquals(1, processInstanceLogs.size());
        assertEquals(processInstanceId2, processInstanceLogs.get(0).getId());
        assertEquals(params.get("myVar"), processInstanceLogs.get(0).getVariables().get("myVar"));

        // Add new Process
        params.put("myVar", "hello456");
        Long processInstanceId3 = processServicesClient.startProcess(containerId, TestWorkflowConstant.PROCESS_ID.SAMPLE_PROCESS_ID, params);

        /** Filter with Var 'MyVar' and value '*hello*'
         * NOTE: Scenarios with multiple key=value filter criteria may not work
         * e.g. processInstanceId 1 = [myKey1=myValue1 & myKey2=myValue2], processInstanceId 2 = [myKey1=myValue2 & myKey1=myValue2]
         * filtering on value 'myValue2' or 'myValue1' will return both instances. K=V is not enforced.
         */
        builder = new QueryFilterSpecBuilder();
        builder.in("variableid", Arrays.asList("myVar"));
        builder.likeTo("value", false, "hello%");
        builder.equalsTo(COLUMN_STATUS, STATE_ACTIVE);

        processInstanceLogs = queryServicesClient.query(TestWorkflowConstant.QUERIES.ALL_PROCESSES_WITH_VARS, QUERY_MAP_PI_WITH_VARS, builder.get(), 0, 1000, ProcessInstance.class);
        assertNotNull(processInstanceLogs);
        assertEquals(2, processInstanceLogs.size());
        processInstanceLogs.forEach(p -> {
            assertTrue(p.getVariables().get("myVar").toString().contains("hello"));
        });
    }


    /**
     * Test Payload
     *
     * @return
     */
    private Map<String, Object> getParams() {
        Map<String, Object> params = new HashMap<>();
        params.put("taskOwner", TestWorkflowConstant.SAMPLE_PROCESS_DATA.TASK_OWNER);
        params.put("myVar", "testing123");
        return params;
    }
}
