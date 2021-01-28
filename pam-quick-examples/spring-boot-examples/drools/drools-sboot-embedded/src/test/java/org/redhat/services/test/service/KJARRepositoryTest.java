package org.redhat.services.test.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.kie.api.builder.KieScanner;
import org.kie.api.runtime.KieContainer;
import org.redhat.services.model.dto.KJAR;
import org.redhat.services.test.AppTestBase;
import org.redhat.services.config.KJARRepositoryConfig;

import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.redhat.services.util.RoutingConstants.CONTAINER_ID;

@Slf4j
public class KJARRepositoryTest extends AppTestBase {

    @Autowired
    private KJARRepositoryConfig kjarRepository;

    /**
     * Test KJAR Props list
     */
    @Test
    public void kjarPropsTest() {
        // Test Expected KJARs list loaded from props
        assertThat(kjarRepository.getKjars(), notNullValue());
        assertThat(kjarRepository.getKjars().isEmpty(), is(false));
        assertThat(kjarRepository.getKjars().size(), equalTo(1)); // KJARs loaded from props

        KJAR kjar = kjarRepository.getKjars().get(0);
        kjar.getArtifactId().equals("mortgages");
        kjar.getVersion().equals("1.0.0-SNAPSHOT");
        kjar.getScanningInterval().equals(600000);
    }


    /**
     * Test Containers Deployed
     */
    @Test
    public void kjarDeploymentTest() {
        // Test Expected KJARs to load exist
        assertThat(kjarRepository.getKjarMap(), notNullValue());
        assertThat(kjarRepository.getKjarMap().entrySet().size(), equalTo(2)); // demo & mortgages
        assertThat(kjarRepository.getKjarMap().containsKey(CONTAINER_ID.DEMO), is(true));
        assertThat(kjarRepository.getKjarMap().containsKey(CONTAINER_ID.MORTGAGES), is(true));
        assertThat(kjarRepository.getKjarMap().get(CONTAINER_ID.DEMO), notNullValue());
        assertThat(kjarRepository.getKjarMap().get(CONTAINER_ID.MORTGAGES), notNullValue());
    }

    /**
     * Test Scanner active
     */
    @Test
    public void scannerTest() {

        // Test Expected KJARs to load exist
        KieScanner demoScanner = kjarRepository.getKieScanner(CONTAINER_ID.DEMO);
        KieScanner mortgagesScanner = kjarRepository.getKieScanner(CONTAINER_ID.MORTGAGES);

        assertThat(demoScanner, nullValue());
        assertThat(mortgagesScanner, notNullValue());

    }

}
