package org.kie.kogito.app;

import org.kie.kogito.process.ProcessConfig;
import org.kie.kogito.process.ProcessEventListenerConfig;
import org.kie.kogito.process.WorkItemHandlerConfig;
import org.kie.kogito.rules.RuleConfig;
import org.kie.kogito.rules.RuleEventListenerConfig;
import org.kie.kogito.uow.UnitOfWorkManager;

@javax.inject.Singleton()
public class ApplicationConfig implements org.kie.kogito.Config {

    protected ProcessConfig processConfig;

    protected RuleConfig ruleConfig;

    private org.kie.kogito.process.ProcessEventListenerConfig defaultProcessEventListenerConfig = new org.kie.kogito.process.impl.DefaultProcessEventListenerConfig();

    private org.kie.kogito.process.WorkItemHandlerConfig defaultWorkItemHandlerConfig = new org.kie.kogito.process.impl.DefaultWorkItemHandlerConfig();

    private org.kie.kogito.uow.UnitOfWorkManager defaultUnitOfWorkManager = new org.kie.kogito.services.uow.DefaultUnitOfWorkManager(new org.kie.kogito.services.uow.CollectingUnitOfWorkFactory());

    private org.kie.kogito.jobs.JobsService defaultJobsService = null;

    @javax.inject.Inject()
    javax.enterprise.inject.Instance<org.kie.kogito.process.ProcessEventListenerConfig> processEventListenerConfig;

    @javax.inject.Inject()
    javax.enterprise.inject.Instance<org.kie.kogito.process.WorkItemHandlerConfig> workItemHandlerConfig;

    @javax.inject.Inject()
    javax.enterprise.inject.Instance<org.kie.kogito.uow.UnitOfWorkManager> unitOfWorkManager;

    @javax.inject.Inject()
    javax.enterprise.inject.Instance<org.kie.kogito.jobs.JobsService> jobsService;

    private org.kie.kogito.rules.RuleEventListenerConfig defaultRuleEventListenerConfig = new org.drools.core.config.DefaultRuleEventListenerConfig();

    @javax.inject.Inject()
    javax.enterprise.inject.Instance<org.kie.kogito.rules.RuleEventListenerConfig> ruleEventListenerConfig;

    @Override
    public ProcessConfig process() {
        return processConfig;
    }

    @Override
    public RuleConfig rule() {
        return ruleConfig;
    }

    protected org.kie.kogito.process.WorkItemHandlerConfig extract_workItemHandlerConfig() {
        if (workItemHandlerConfig.isUnsatisfied() == false)
            return workItemHandlerConfig.get();
        else
            return defaultWorkItemHandlerConfig;
    }

    protected org.kie.kogito.process.ProcessEventListenerConfig extract_processEventListenerConfig() {
        if (processEventListenerConfig.isUnsatisfied() == false)
            return processEventListenerConfig.get();
        else
            return defaultProcessEventListenerConfig;
    }

    protected org.kie.kogito.uow.UnitOfWorkManager extract_unitOfWorkManager() {
        if (unitOfWorkManager.isUnsatisfied() == false)
            return unitOfWorkManager.get();
        else
            return defaultUnitOfWorkManager;
    }

    protected org.kie.kogito.jobs.JobsService extract_jobsService() {
        if (jobsService.isUnsatisfied() == false)
            return jobsService.get();
        else
            return defaultJobsService;
    }

    protected org.kie.kogito.rules.RuleEventListenerConfig extract_ruleEventListenerConfig() {
        if (ruleEventListenerConfig.isUnsatisfied() == false)
            return ruleEventListenerConfig.get();
        else
            return defaultRuleEventListenerConfig;
    }

    public org.kie.kogito.Addons addons() {
        return new org.kie.kogito.Addons(java.util.Arrays.asList("infinispan-persistence", "process-management"));
    }

    @javax.annotation.PostConstruct()
    public void init() {
        processConfig = new org.kie.kogito.process.impl.StaticProcessConfig(extract_workItemHandlerConfig(), extract_processEventListenerConfig(), extract_unitOfWorkManager(), extract_jobsService());
        ruleConfig = new org.drools.core.config.StaticRuleConfig(extract_ruleEventListenerConfig());
    }
}
