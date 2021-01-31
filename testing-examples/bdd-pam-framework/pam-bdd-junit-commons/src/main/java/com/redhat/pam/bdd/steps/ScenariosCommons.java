package com.redhat.pam.bdd.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.cucumber.java8.En;
import io.cucumber.java8.Scenario;
import com.redhat.pam.bdd.context.BDDContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScenariosCommons implements En {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScenariosCommons.class);

    private static final Pattern FROM_FILE_OBJECT = Pattern.compile("(?<path>(?:[^;]*\\/))?(?<prefix>(?:[^;]*-))?(?<className>.+?(?=\\.json))");

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Inject
    private BDDContext context;

    public ScenariosCommons() {
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

        Before((Scenario scenario) -> {
            LOGGER.info( "Scenario '{}' started.", scenario.getName());
        });

        After((Scenario scenario) -> {
            LOGGER.info("Scenario '{}' completed with status '{}'.", scenario.getName(), scenario.getStatus().name());
            context.getKieSession().dispose();
            context.getRuntimeManager().disposeRuntimeEngine(context.getRuntimeEngine());
            context.getRuntimeManager().close();
        });
    }
}
