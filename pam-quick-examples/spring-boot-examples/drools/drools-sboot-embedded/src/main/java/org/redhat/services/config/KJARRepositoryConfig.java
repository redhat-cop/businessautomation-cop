package org.redhat.services.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieScanner;
import org.kie.api.builder.Message.Level;
import org.kie.api.builder.ReleaseId;
import org.kie.api.io.Resource;
import org.kie.api.runtime.KieContainer;
import org.kie.internal.io.ResourceFactory;
import org.redhat.services.exception.KieContainerRequestException;
import org.redhat.services.model.dto.KJAR;
import org.redhat.services.model.dto.Release;
import org.redhat.services.rules.util.KieBaseListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
@ConfigurationProperties(prefix = "rules")
// @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class KJARRepositoryConfig {

    @Value("${rules.demo.project}")
    private boolean initDemoProject;

    public static final String demoContainerId = "DEMO_CONTAINER";

    List<KJAR> kjars;
    private Map<String, KJAR> kjarMap = new HashMap<String, KJAR>();

    private static final String kieMavenSettingsProp = "kie.maven.settings.custom";
    private String mavenSettings;

    @Bean(name = "kjarRepository")
    public KJARRepositoryConfig getRepository() {

        if (StringUtils.isNotEmpty(mavenSettings)) {
            log.info("Setting '{}' property : {} ", kieMavenSettingsProp, mavenSettings);
            System.setProperty(kieMavenSettingsProp, mavenSettings);
        }

        if (CollectionUtils.isNotEmpty(kjars)) {

            kjars.stream().forEach(k -> {

                // Define ContainerID if none provided
                if (StringUtils.isEmpty(k.getContainerId())) {
                    k.setContainerId(k.getArtifactId() + "-" + k.getVersion());
                }

                // Handle Duplicates
                if (kjarMap.containsKey(k.getContainerId())) {
                    log.error("KJAR Map already contains multiple kjars with the same containerId :: {}, " +
                            "please review KJAR GAV : {}", k.getContainerId(), k.toString());
                } else {
                    // Invoke KJARs
                    log.info("Initializing {}", k);
                    this.kjarMap.put(k.getContainerId(), k);
                    initKJAR(k);
                }
            });
        } else {
            log.info("No KJARs defined to deploy... automatically deploying demo project.");
            initDemoProject = true;
        }

        if (initDemoProject) {
            log.info("Adding Demo repo to KJAR's to deploy");
            this.initDemoRepo();
        }

        return this;

    }

    public KieContainer getKieContainer(String containerId) {
        return this.kjarMap.get(containerId).getContainer();
    }

    public KieScanner getKieScanner(String containerId) {
        return this.kjarMap.get(containerId).getScanner();
    }

    public void initKJAR(KJAR kjar) {

        KieServices kieServices = KieServices.Factory.get();
        ReleaseId releaseId = kieServices.newReleaseId(kjar.getGroupId(), kjar.getArtifactId(), kjar.getVersion());
        kjar.setContainer(kieServices.newKieContainer(releaseId));

        KieScanner kScanner = KieServices.get().newKieScanner(kjar.getContainer());
        kScanner.addListener(new KieBaseListener(kjar));
        kScanner.start(kjar.getScanningInterval());
        kjar.setScanner(kScanner);

    }

    public void loadLatestKieBase(Release release, String containerId) throws KieContainerRequestException {

        // Check existing deployed containers
        if (kjarMap.containsKey(containerId)) {

            KJAR kjar = kjarMap.get(containerId);

            if (!kjar.getGroupId().equalsIgnoreCase(release.getGroupId()) ||
                    !kjar.getArtifactId().equalsIgnoreCase(release.getArtifactId())) {
                log.error("KJAR with ContainerID {} already exists with GAV of :: groupId={}, artifactId={}, version={}, please try a different containerID ",
                        containerId, kjar.getGroupId(), kjar.getArtifactId(), kjar.getVersion());
                throw new KieContainerRequestException("KJAR with ContainerID='" + containerId + " already exists, please try a different containerID ");
            }

            if (!kjar.getVersion().equalsIgnoreCase(release.getVersion())) {
                log.error("KJAR with ContainerID {} already exists with a different Project Version :: version={}, please try a different containerID ", containerId, kjar.getVersion());
                throw new KieContainerRequestException("KJAR with ContainerID='" + containerId + " with different project version already exists, please try a different containerID ");
            }

            log.info("containerID={} already exists, ");
            kjarMap.get(containerId).getScanner().scanNow();
        }

        KJAR kjar = new KJAR();
        kjar.setGroupId(release.getGroupId());
        kjar.setArtifactId(release.getArtifactId());
        kjar.setVersion(release.getVersion());
        kjar.setVersion(containerId);

        log.info("Initialising new KJAR {} ", kjar);
        this.initKJAR(kjar);

    }

    //@formatter:off
    public KieContainer initDemoRepo() {

        KieServices kieServices = KieServices.Factory.get();

        // Load from Classpath
        Resource dt = ResourceFactory.newClassPathResource("hello.drl", getClass());
        KieFileSystem kieFileSystem = kieServices.newKieFileSystem().write(dt);

        // Load directly from String
//        String sampleDrl = "rule \"sampleRule\""
//                + "\n" + "when"
//                + "\n" + "then"
//                + "\n" + "System.out.println(\"Hello World Rule Fired\");"
//                + "\n" + "org.redhat.services.model.RuleResponse r = new org.redhat.services.model.RuleResponse();"
//                + "\n" + "r.setMessage( \" Hello World \" );"
//                + "\n" + "insert(r);"
//                + "\n" + "end";
//        kieFileSystem.write("src/main/resources/KBase1/ruleSet1.drl", sampleDrl);

        KieBuilder kb = kieServices.newKieBuilder(kieFileSystem);
        kb.buildAll();

        if (kb.getResults().hasMessages(Level.ERROR)) {
            throw new RuntimeException("Build Errors:\n" + kb.getResults().toString());
        }

        kb.getResults().getMessages().forEach(m -> {
            log.debug("build msg: {}", m);
        });

        KieContainer container = kieServices.newKieContainer(kieServices.getRepository().getDefaultReleaseId());
        ReleaseId releaseId = container.getReleaseId();
        log.info("Demo project deployed with default ReleaseID :: groupId={}, artifactId={}, version={}",
                releaseId.getGroupId(), releaseId.getArtifactId(), releaseId.getVersion());

        KJAR demoKJAR = new KJAR(releaseId.getGroupId(), releaseId.getArtifactId(), releaseId.getVersion(), demoContainerId, 0, container, null );
        kjarMap.put(demoContainerId, demoKJAR);
        return container;
    }
    //@formatter:on

    public List<KJAR> getKjars() {
        return kjars;
    }

    public String getMavenSettings() {
        return mavenSettings;
    }

    public void setMavenSettings(String mavenSettings) {
        this.mavenSettings = mavenSettings;
    }

    public void setKjars(List<KJAR> kjars) {
        this.kjars = kjars;
    }

}
