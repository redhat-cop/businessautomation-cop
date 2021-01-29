package com.redhat.pam.bdd.context;

import io.cucumber.guice.ScenarioScoped;
import com.redhat.pam.bdd.listeners.BDDProcessListener;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.internal.runtime.manager.context.CaseContext;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;

import java.util.HashMap;
import java.util.Map;

@ScenarioScoped
public class BDDContext {

    private final RuntimeManager runtimeManager;

    private final RuntimeEngine runtimeEngine;

    private final KieSession kieSession;

    private Long processInstanceId;

    private Map<String, Object> processParameters = new HashMap<>();

    public BDDContext(final RuntimeManager runtimeManager, final ProcessInstanceIdContext processInstanceIdContext){
        this.runtimeManager = runtimeManager;
        this.runtimeEngine = this.runtimeManager.getRuntimeEngine(processInstanceIdContext);
        this.kieSession = this.runtimeEngine.getKieSession();
        this.kieSession.addEventListener(new BDDProcessListener(this));
    }

    public BDDContext(final RuntimeManager runtimeManager, final CaseContext caseInstanceIdContext){
        this.runtimeManager = runtimeManager;
        this.runtimeEngine = this.runtimeManager.getRuntimeEngine(caseInstanceIdContext);
        this.kieSession = this.runtimeEngine.getKieSession();
    }

    public RuntimeEngine getRuntimeEngine() {
        return runtimeEngine;
    }

    public KieSession getKieSession() {
        return kieSession;
    }

    public RuntimeManager getRuntimeManager() {
        return runtimeManager;
    }

    public Long getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(Long processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public Map<String, Object> getProcessParameters() {
        return processParameters;
    }

    public void setProcessParameters(Map<String, Object> processParameters) {
        this.processParameters = processParameters;
    }
}
