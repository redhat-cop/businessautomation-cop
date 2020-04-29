package org.drools.project.model;

import org.drools.model.Model;
import org.kie.api.builder.ReleaseId;
import org.drools.compiler.kproject.ReleaseIdImpl;

public class ProjectModel implements org.drools.modelcompiler.CanonicalKieModuleModel {

    @Override
    public String getVersion() {
        return "0.6.1";
    }

    @Override
    public java.util.List<Model> getModels() {
        return java.util.Arrays.asList();
    }

    @Override
    public java.util.List<Model> getModelsForKieBase(String kieBaseName) {
        switch (kieBaseName) {
            case "defaultKieBase": return getModels();
        }
        throw new IllegalArgumentException("Unknown KieBase: " + kieBaseName);
    }

    @Override
    public ReleaseId getReleaseId() {
        return new ReleaseIdImpl("dummy", "dummy", "0.0.0");
    }

@Override()
public org.kie.api.builder.model.KieModuleModel getKieModuleModel() {
    org.kie.api.builder.model.KieModuleModel kModuleModel = org.kie.api.KieServices.get().newKieModuleModel();
    org.kie.api.builder.model.KieBaseModel kieBaseModel_defaultKieBase = kModuleModel.newKieBaseModel("defaultKieBase");
    kieBaseModel_defaultKieBase.setDefault(true);
    kieBaseModel_defaultKieBase.setEventProcessingMode(org.kie.api.conf.EventProcessingOption.CLOUD);
    kieBaseModel_defaultKieBase.addPackage("*");
    org.kie.api.builder.model.KieSessionModel kieSessionModel_defaultStatelessKieSession = kieBaseModel_defaultKieBase.newKieSessionModel("defaultStatelessKieSession");
    kieSessionModel_defaultStatelessKieSession.setDefault(true);
    kieSessionModel_defaultStatelessKieSession.setType(org.kie.api.builder.model.KieSessionModel.KieSessionType.STATELESS);
    kieSessionModel_defaultStatelessKieSession.setClockType(org.kie.api.runtime.conf.ClockTypeOption.get("realtime"));
    org.kie.api.builder.model.KieSessionModel kieSessionModel_defaultKieSession = kieBaseModel_defaultKieBase.newKieSessionModel("defaultKieSession");
    kieSessionModel_defaultKieSession.setDefault(true);
    kieSessionModel_defaultKieSession.setType(org.kie.api.builder.model.KieSessionModel.KieSessionType.STATEFUL);
    kieSessionModel_defaultKieSession.setClockType(org.kie.api.runtime.conf.ClockTypeOption.get("realtime"));
    return kModuleModel;
}
}