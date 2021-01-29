package org.redhat.services.configuration;

import org.redhat.services.model.KJAR;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerResourceFilter;
import org.kie.server.services.api.KieServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Deploys KIE Containers from app properties
 */
@Configuration
@ConfigurationProperties(prefix = "deployment")
public class KieServerDeployer {

    private static final Logger LOGGER = LoggerFactory.getLogger(KieServerDeployer.class);

    @Autowired
    KieServer kieServer;

    private List<KJAR> kjars;

    @Bean
    CommandLineRunner deployAndValidate() {
        return new CommandLineRunner() {

            @Override
            public void run(String... strings) throws Exception {
                LOGGER.info("Kie Containers listed for deployments :: {}", kjars);

                // Check which containers are already deployed
                List<KieContainerResource> result = kieServer.listContainers(KieContainerResourceFilter.ACCEPT_ALL)
                        .getResult().getContainers();
                result.forEach(c -> {
                    LOGGER.info("KIE Containers already deployed {}", c);
                });

                // Deploy containers specified in props
                kjars.forEach(k -> {
                    KieContainerResource resource = new KieContainerResource(k.getContainerId(), k.getReleaseId());
                    resource.setResolvedReleaseId(k.getReleaseId());
                    resource.setContainerAlias(k.getAlias());

                    if (!isDeployed(resource, result)) {
                        LOGGER.info("Deploying KIE Container : {} using custom deployer", k);
                        resource.setResolvedReleaseId(null);
                        kieServer.createContainer(k.getContainerId(), resource);
                    } else {
                        LOGGER.info("Skipping deployment of KIE Container : {} b/c it's already deployed", k);
                    }
                });
            }
        };
    }

    private boolean isDeployed(KieContainerResource resource, List<KieContainerResource> result) {
        return result.contains(resource);
    }

    public List<KJAR> getKjars() {
        return kjars;
    }

    public void setKjars(List<KJAR> kjars) {
        this.kjars = kjars;
    }
}
