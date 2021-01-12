package org.jbpm.cucumber;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.test.JbpmJUnitBaseTestCase;
import org.kie.api.KieBase;
import org.kie.api.definition.process.Process;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.runtime.manager.context.EmptyContext;

public class JbpmTestUtil extends JbpmJUnitBaseTestCase {
    private RuntimeManager runtimeManager;
    private RuntimeEngine runtimeEngine;
    private KieSession kieSession;
    private ProcessInstance processInstance;
    private String processId;

    private TaskService taskService;

    private Map<String, Object> processParams = new HashMap<>();

    public JbpmTestUtil() {
        super(true, true);
    }

    public void initialize(List<String> bpmnFiles) {
        runtimeManager = createRuntimeManager(bpmnFiles.toArray(new String[bpmnFiles.size()]));
        runtimeEngine = getRuntimeEngine(EmptyContext.get());
        if (runtimeEngine != null) {
            kieSession = runtimeEngine.getKieSession();
            taskService = runtimeEngine.getTaskService();
        }
    }

    public boolean useProcessDefinition(String processId) {
        this.processId = processId;
        if (kieSession == null) {
            return false;
        }
        KieBase kieBase = kieSession.getKieBase();
        if (kieBase == null) {
            return false;
        }
        Process process = kieBase.getProcess(processId);
        if (process == null) {
            return false;
        }
        return true;
    }

    public void startProcess() {
        if (kieSession != null && processId != null) {
            processInstance = kieSession.startProcess(processId, processParams);
        }
    }

    public RuntimeManager getRuntimeManager() {
        return runtimeManager;
    }

    public void setRuntimeManager(RuntimeManager runtimeManager) {
        this.runtimeManager = runtimeManager;
    }

    public RuntimeEngine getRuntimeEngine() {
        return runtimeEngine;
    }

    public void setRuntimeEngine(RuntimeEngine runtimeEngine) {
        this.runtimeEngine = runtimeEngine;
    }

    public KieSession getKieSession() {
        return kieSession;
    }

    public void setKieSession(KieSession kieSession) {
        this.kieSession = kieSession;
    }

    public ProcessInstance getProcessInstance() {
        return processInstance;
    }

    public void setProcessInstance(ProcessInstance processInstance) {
        this.processInstance = processInstance;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public Map<String, Object> getProcessParams() {
        return processParams;
    }

    public void setProcessParams(Map<String, Object> processParams) {
        this.processParams = processParams;
    }

    public void addProcessParam(String name, Object value) {
        this.processParams.putIfAbsent(name, value);
    }
    
    // tasks
    public boolean isHumanTaskAssignedTo (String taskName, String userId) {
        return hasHumanTaskStatus(taskName, userId, Status.Reserved);
    }

    public boolean isHumanTaskClaimableBy (String taskName, String userId) {
        return hasHumanTaskStatus(taskName, userId, Status.Ready);
    }

    public boolean isHumanTaskStarted (String taskName, String userId) {
        return hasHumanTaskStatus(taskName, userId, Status.InProgress);
    }
    
    public boolean completeHumanTask (String taskName, String userId, Map<String, Object> data) {
        List<TaskSummary> tasks = taskService.getTasksAssignedAsPotentialOwner(userId, "en-UK");
        TaskSummary found = tasks.stream().filter(task -> task.getName().equals(taskName)).findAny().orElse(null);
        if (found != null) {
            switch (found.getStatus()) {
                case Ready:
                    taskService.claim(found.getId(), userId);
                case Reserved:
                    taskService.start(found.getId(), userId);
                case InProgress:
                    taskService.complete(found.getId(), userId, data);
                    return true;
                default:
                    return false;
            }
        }
        return false;
    }
    
    private boolean hasHumanTaskStatus (String taskName, String userId, Status status) {
        List<TaskSummary> tasks = taskService.getTasksAssignedAsPotentialOwner(userId, "en-UK");
        TaskSummary found = tasks.stream().filter(task -> task.getName().equals(taskName)).findAny().orElse(null);
        return found != null ? found.getStatus() == status : false;
    }

}
