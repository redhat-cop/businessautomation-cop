package org.redhat.services.test.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.kie.api.task.model.Status;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.QueryServicesClient;
import org.kie.server.client.UserTaskServicesClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Configuration
public abstract class KieTestUtilsIT {

    @Value("${test.deployment.kjar.alias:kie-sboot-test-kjar}")
    protected String containerAlias;

    @Value("${test.deployment.kjar.containerId:kie-sboot-test-kjar-1_0-SNAPSHOT}")
    protected String containerId;

    @Autowired
    protected KieServerClientServiceTest kieServerClientService;

    private boolean init = false;

    // Clients
    public KieServicesClient kieServicesClient;
    public QueryServicesClient queryServicesClient;
    public ProcessServicesClient processServicesClient;
    public UserTaskServicesClient userTaskServicesClient;

    @Before
    public void setup() {
        if (!init) {
            // Instantiate Clients
            try {
                kieServicesClient = kieServerClientService.clientProducer();
                queryServicesClient = kieServicesClient.getServicesClient(QueryServicesClient.class);
                processServicesClient = kieServicesClient.getServicesClient(ProcessServicesClient.class);
                userTaskServicesClient = kieServicesClient.getServicesClient(UserTaskServicesClient.class);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    @After
    public void cleanUp() {
        List<ProcessInstance> instances = queryServicesClient.findProcessInstancesByStatus(Arrays.asList(1), 0, 0);
        List<Long> pids = instances.stream().map(ProcessInstance::getId).collect(Collectors.toList());

        try {
            processServicesClient.abortProcessInstances(containerId, pids);
        } catch (Exception e) {

        }
        int total = processServicesClient.findProcessInstances(containerId, 0, 0).size();
        assertEquals(0, total);
    }

    protected TaskSummary assertTaskInstance(Long processInstanceId, int expectedTaskCount, String taskOwner, String taskName) {
        // Assert Interrupt Modification Task
        List<TaskSummary> tasks = userTaskServicesClient.findTasksByStatusByProcessInstanceId(processInstanceId, TestWorkflowConstant.getActiveTaskStatuses(), 0, 100);
        assertNotNull(tasks);
        assertEquals(expectedTaskCount, tasks.size());

        if (expectedTaskCount <= 0){
            return null;
        }

        assertEquals(taskName, tasks.get(0).getName());
        assertEquals(taskOwner, tasks.get(0).getActualOwner());
        assertEquals(Status.Reserved.name(), tasks.get(0).getStatus());
        return tasks.get(0);
    }
}
