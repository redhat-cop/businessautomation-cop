package com.redhat.pam.bdd.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.redhat.pam.bdd.context.BDDContext;
import io.cucumber.java8.En;
import io.cucumber.java8.Scenario;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.client.KieServicesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScenariosCommons implements En {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScenariosCommons.class);

    private static final Pattern FROM_FILE_OBJECT = Pattern.compile("(?<path>(?:[^;]*\\/))?(?<prefix>(?:[^;]*-))?(?<className>.+?(?=\\.json))");

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Inject
    private BDDContext context;

    private Consumer<ReleaseId> deployContainer = (releaseId) -> context.getKieServerClient().createContainer(releaseId.toExternalForm(), new KieContainerResource(releaseId));

    private Consumer<ReleaseId> undeployContainer = (releaseId) -> {
        final KieServicesClient kieServerClient = context.getKieServerClient();
        kieServerClient.listContainers()
                .getResult()
                .getContainers()
                .stream()
                .filter(container -> container.getReleaseId().equals(releaseId))
                .forEach(container -> kieServerClient.disposeContainer(container.getContainerId()));
    };

    public ScenariosCommons(){
        Before((Scenario scenario) -> {
            LOGGER.info( "Scenario '{}' started.", scenario.getName());
        });

        Before("@BypassAuthUser", () -> {
            LOGGER.info("Setting {} to true", KieServerConstants.CFG_BYPASS_AUTH_USER);
            System.setProperty(KieServerConstants.CFG_BYPASS_AUTH_USER, "true");
        });

        Before("@Deploy", () ->  undeployContainer
                                            .andThen(deployContainer)
                                            .accept(context.getReleaseId()));

        After((Scenario scenario) -> {
            LOGGER.info("Scenario '{}' completed with status '{}'.", scenario.getName(), scenario.getStatus().name());
        });

        After("@undeploy", () -> undeployContainer.accept(context.getReleaseId()));

        DataTableType((String datatableCell) -> {
            if(datatableCell == null) {
                return null;
            }
            final Matcher matcher = FROM_FILE_OBJECT.matcher(datatableCell);
            if(matcher.find()) {
                final String path = matcher.group("path");
                final String prefix = matcher.group("prefix");
                final String className = matcher.group("className");
                LOGGER.debug("Going to replace datatable cell value '{}' with an instance of class '{}' json file expected in path '{}'(prefix: {})", datatableCell, className, path, prefix);
                return objectMapper.readValue(getClass().getClassLoader().getResource(datatableCell), Class.forName(className));
            } else if(datatableCell.equals("com.redhat.property.processId")) {
                return String.valueOf(context.getProcessInstanceId());
            } else {
                return datatableCell;
            }
        });
    }
}
