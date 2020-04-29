package org.drools.project.model;

import org.kie.api.KieBase;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.runtime.KieSession;
import org.drools.modelcompiler.builder.KieBaseBuilder;

@javax.enterprise.context.ApplicationScoped
public class ProjectRuntime implements org.kie.kogito.rules.KieRuntimeBuilder {

    private final ProjectModel model = new ProjectModel();
    private final java.util.Map<String, KieBase> kbases = new java.util.HashMap<>();

    @Override
    public KieBase getKieBase() {
        return getKieBase("defaultKieBase");
    }

    @Override
    public KieBase getKieBase(String name) {
        return kbases.computeIfAbsent(name, n -> KieBaseBuilder.createKieBaseFromModel( model.getModelsForKieBase( n ), model.getKieModuleModel().getKieBaseModels().get( n ) ));
    }

    @Override
    public KieSession newKieSession() {
        return newKieSession("defaultKieSession");
    }

    @Override
    public KieSession newKieSession(String sessionName) {
        return newKieSession(sessionName, new org.drools.core.config.StaticRuleConfig(new org.drools.core.config.DefaultRuleEventListenerConfig()));
    }

    @Override
    public KieSession newKieSession(String sessionName, org.kie.kogito.rules.RuleConfig ruleConfig) {
        return java.util.Optional.ofNullable(getKieBaseForSession(sessionName).newKieSession(getConfForSession(sessionName), null)).map(k -> {
            ruleConfig.ruleEventListeners().agendaListeners().forEach( k::addEventListener );
            ruleConfig.ruleEventListeners().ruleRuntimeListeners().forEach( k::addEventListener );
            return k;
        }).get();
    }

    private KieBase getKieBaseForSession(String sessionName) {
        switch (sessionName) {
            case "defaultStatelessKieSession": return getKieBase("defaultKieBase");
            case "defaultKieSession": return getKieBase("defaultKieBase");
        }
        return null;
    }

    private org.kie.api.runtime.KieSessionConfiguration getConfForSession(String sessionName) {
        org.drools.core.SessionConfigurationImpl conf = new org.drools.core.SessionConfigurationImpl();
        switch (sessionName) {
            case "defaultStatelessKieSession":
{
    conf.setOption(org.kie.api.runtime.conf.ClockTypeOption.get("realtime"));
}                break;
            case "defaultKieSession":
{
    conf.setOption(org.kie.api.runtime.conf.ClockTypeOption.get("realtime"));
}                break;
        }
        return conf;
    }

}