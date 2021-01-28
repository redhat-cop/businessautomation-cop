package com.redhat.pam.bdd.steps;

import com.google.inject.Inject;
import io.cucumber.datatable.DataTable;
import io.cucumber.java8.En;
import com.redhat.pam.bdd.context.BDDContext;
import com.redhat.pam.utils.TriFunction;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.TaskSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class HumanTaskSteps implements En {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessSteps.class);

    @Inject
    private BDDContext context;

    final Function<DataTable, Predicate<TaskSummary>> dataTableTaskFilterToPredicate = (parameters) -> parameters.asMap(String.class, Object.class)
            .entrySet()
            .stream()
            .filter(entry -> ((String) entry.getKey()).startsWith("task.filter"))
            .map(entry -> {
                final String key = (String) entry.getKey();
                final String value = (String) entry.getValue();
                LOGGER.debug("Going to create predicate for task property: '{}' value: '{}'", key, value);
                switch (key) {
                    case "task.filter.name":
                        final Predicate<TaskSummary> namePredicate = (taskSummary) -> taskSummary.getName().equals(value);
                        return namePredicate;
                    case "task.filter.subject":
                        final Predicate<TaskSummary> subjectPredicate = (taskSummary) -> taskSummary.getSubject().equals(value);
                        return subjectPredicate;
                    case "task.filter.status":
                        final Predicate<TaskSummary> statusPredicate = (taskSummary) -> taskSummary.getStatus().equals(Status.valueOf(value));
                        return statusPredicate;
                    default:
                        throw new RuntimeException("Cannot build predicate on " + key + " key");
                }
            }).reduce(Predicate::and).orElseGet(() -> Objects::isNull);

    final Function<String, Stream<TaskSummary>> retrieveTaskAsPotentialOwner = (username) -> context.getRuntimeEngine().getTaskService().getTasksAssignedAsPotentialOwner(username, "en-UK").stream();
    final Function<String, Stream<TaskSummary>> retrieveTaskOwned = (username) -> context.getRuntimeEngine().getTaskService().getTasksOwned(username, "en-UK").stream();
    final BiFunction<DataTable, Stream<TaskSummary>, Optional<TaskSummary>> applyScenarioDefinedTaskFilter = (parameters, taskSummaryStream) -> taskSummaryStream.filter(dataTableTaskFilterToPredicate.apply(parameters)).findAny();
    final BiFunction<Optional<TaskSummary>, String, TaskSummary> claimTask = (taskSummaryOpt, username) -> {
        if (taskSummaryOpt.isPresent()) {
            assertEquals(Status.Ready, taskSummaryOpt.get().getStatus());
            LOGGER.debug("Task with id {} matched provided filters", taskSummaryOpt.get().getId());
            context.getRuntimeEngine().getTaskService().claim(taskSummaryOpt.get().getId(), username);
        } else {
            fail("No task found matching provided filters");
        }
        return taskSummaryOpt.get();
    };
    final BiFunction<Optional<TaskSummary>, String, TaskSummary> startTask = (taskSummaryOpt, username) -> {
        if (taskSummaryOpt.isPresent()) {
            assertEquals(Status.Reserved, taskSummaryOpt.get().getStatus());
            LOGGER.debug("Task with id {} matched provided filters", taskSummaryOpt.get().getId());
            context.getRuntimeEngine().getTaskService().start(taskSummaryOpt.get().getId(), username);
        } else {
            fail("No task found matching provided filters");
        }
        return taskSummaryOpt.get();
    };
    final TriFunction<Optional<TaskSummary>, String, DataTable, TaskSummary> completeTask = (taskSummaryOpt, username, parameters) -> {
        if (taskSummaryOpt.isPresent()) {
            final Map<String, Object> taskParameters = parameters.asMap(String.class, Object.class)
                    .entrySet()
                    .stream()
                    .filter(entry -> !((String) entry.getKey()).startsWith("task.filter"))
                    .collect(Collectors.toMap(ent -> (String) ent.getKey(), ent -> ent.getValue()));
            if (taskParameters.isEmpty()) {
                context.getRuntimeEngine().getTaskService().complete(taskSummaryOpt.get().getId(), username, null);
            } else {
                context.getRuntimeEngine().getTaskService().complete(taskSummaryOpt.get().getId(), username, taskParameters);
            }
        } else {
            fail("No task found matching provided filters");
        }
        return taskSummaryOpt.get();
    };

    public HumanTaskSteps() {
        And("the user {string} claims the task", //
                (final String username, final DataTable parameters) -> retrieveTaskAsPotentialOwner //
                        .andThen((stream) -> applyScenarioDefinedTaskFilter.apply(parameters, stream)) //
                        .andThen(taskSummary -> claimTask.apply(taskSummary, username)) //
                        .apply(username));

        And("the user {string} starts the task", //
                (final String username, final DataTable parameters) -> retrieveTaskOwned //
                        .andThen((stream) -> applyScenarioDefinedTaskFilter.apply(parameters, stream)) //
                        .andThen(taskSummary -> startTask.apply(taskSummary, username)) //
                        .apply(username));

        And("the user {string} completes the task", //
                (final String username, final DataTable parameters) -> retrieveTaskOwned //
                        .andThen((stream) -> applyScenarioDefinedTaskFilter.apply(parameters, stream)) //
                        .andThen(taskSummary -> completeTask.apply(taskSummary, username, parameters)) //
                        .apply(username));
    }
}