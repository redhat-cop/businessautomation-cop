package org.redhat.services.test.containers;

import org.redhat.services.test.util.KieTestUtilsIT;
import org.redhat.services.test.util.TestWorkflowConstant;
import org.junit.Test;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.api.model.KieServiceResponse;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.definition.ProcessDefinition;

import java.util.List;

import static org.junit.Assert.*;

public class KieContainerDeploymentTest extends KieTestUtilsIT {

    @Test
    public void testContainerDeployed() {

        // Test Containers are deployed
        ServiceResponse<KieContainerResourceList> containers = kieServicesClient.listContainers();
        assertNotNull(containers);
        assertNotNull(containers.getResult());
        assertEquals(KieServiceResponse.ResponseType.SUCCESS, containers.getType());
        assertEquals(1, containers.getResult().getContainers().size());

        assertTrue(containers.getResult().getContainers().stream()
                .filter(c -> c.getContainerId().equalsIgnoreCase(containerId)).count() == 1);
    }

    @Test
    public void testDeployedProcessDefinitions() {
        List<ProcessDefinition> definitions = queryServicesClient.findProcesses(0, 100);
        assertNotNull(definitions);
        assertEquals(2, definitions.size());

        definitions.stream().forEach(d -> {
            assertTrue(TestWorkflowConstant.definitions.contains(d.getId()));
        });
    }
}
