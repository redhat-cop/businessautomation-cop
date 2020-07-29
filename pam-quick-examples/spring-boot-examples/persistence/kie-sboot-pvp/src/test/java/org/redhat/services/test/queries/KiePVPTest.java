package org.redhat.services.test.queries;

import org.drools.persistence.jpa.marshaller.MappedVariable;
import org.junit.Test;
import org.redhat.services.model.Person;
import org.redhat.services.test.util.KieTestUtilsIT;
import org.redhat.services.test.util.TestWorkflowConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class KiePVPTest extends KieTestUtilsIT {

    @Autowired
    JdbcTemplate jdbcTemplate;

    /**
     * Test JPA Objects are persisted to the jBPM (local) Schema
     */
    @Test
    public void testLocalProcessVariablePersistence() throws InterruptedException {

        assertEquals(0, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM PERSON", Integer.class).intValue());

        Person person = new Person();
        person.setAge(20);
        person.setName("Joe");
        person.setSurname("Bloggs");

        Map<String, Object> params = this.getParams();
        params.put("person", person);

        // start process instance
        Long processInstanceId = processServicesClient.startProcess(containerId, TestWorkflowConstant.PROCESS_ID.PVP_PROCESS_ID, params);
        assertNotNull(processInstanceId);

        // Assert Person Obj
        assertEquals(1, jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM PERSON",
                Integer.class).intValue());
        assertEquals(1, jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM MAPPEDVARIABLE WHERE PROCESSINSTANCEID = ?",
                new Object[]{processInstanceId},
                Integer.class).intValue());

        // Assert associated Mapped Variable
        MappedVariable mv = jdbcTemplate.queryForObject(
                "SELECT * FROM MAPPEDVARIABLE WHERE PROCESSINSTANCEID = ?",
                new Object[]{processInstanceId},
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
