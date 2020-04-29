package org.kie.kogito.app;

import org.kie.kogito.Config;
import org.kie.kogito.process.Processes;
import org.kie.kogito.uow.UnitOfWorkManager;

@javax.inject.Singleton()
public class Application implements org.kie.kogito.Application {

    @javax.inject.Inject()
    javax.enterprise.inject.Instance<org.kie.kogito.event.EventPublisher> eventPublishers;

    @org.eclipse.microprofile.config.inject.ConfigProperty(name = "kogito.service.url")
    java.util.Optional<java.lang.String> kogitoService;

    @javax.inject.Inject()
    org.kie.kogito.Config config;

    RuleUnits ruleUnits = new RuleUnits();

    Processes processes = new Processes();

    DecisionModels decisionModels = new DecisionModels();

    public Config config() {
        return config;
    }

    public UnitOfWorkManager unitOfWorkManager() {
        return config().process().unitOfWorkManager();
    }

    @javax.annotation.PostConstruct()
    public void setup() {
        if (config().process() != null) {
            if (eventPublishers != null) {
                eventPublishers.forEach(publisher -> unitOfWorkManager().eventManager().addPublisher(publisher));
            }
            unitOfWorkManager().eventManager().setService(kogitoService.orElse(""));
            unitOfWorkManager().eventManager().setAddons(config().addons());
        }
    }

    public RuleUnits ruleUnits() {
        return ruleUnits;
    }

    public class RuleUnits extends org.kie.kogito.rules.impl.AbstractRuleUnits {

        org.kie.kogito.rules.KieRuntimeBuilder ruleRuntimeBuilder = new org.drools.project.model.ProjectRuntime();

        public org.kie.kogito.rules.KieRuntimeBuilder ruleRuntimeBuilder() {
            return this.ruleRuntimeBuilder;
        }

        protected org.kie.kogito.rules.RuleUnit<?> create(String fqcn) {
            switch(fqcn) {
                default:
                    throw new java.lang.UnsupportedOperationException();
            }
        }
    }

    public Processes processes() {
        return processes;
    }

    public class Processes implements org.kie.kogito.process.Processes {

        public org.kie.kogito.process.Process<? extends org.kie.kogito.Model> processById(String processId) {
            if ("drink_order_process".equals(processId))
                return new org.drools.bpmn2.Drink_order_processProcess(Application.this).configure();
            return null;
        }

        public java.util.Collection<String> processIds() {
            return java.util.Arrays.asList("drink_order_process");
        }
    }

    public DecisionModels decisionModels() {
        return decisionModels;
    }

    public static class DecisionModels implements org.kie.kogito.decision.DecisionModels {

        static org.kie.dmn.api.core.DMNRuntime dmnRuntime = org.kie.kogito.dmn.DMNKogito.createGenericDMNRuntime();

        public org.kie.kogito.decision.DecisionModel getDecisionModel(java.lang.String namespace, java.lang.String name) {
            return new org.kie.kogito.decision.DecisionModel() {

                @Override
                public org.kie.dmn.api.core.DMNContext newContext(java.util.Map<String, Object> variables) {
                    return new org.kie.dmn.core.impl.DMNContextImpl(variables);
                }

                @Override
                public org.kie.dmn.api.core.DMNResult evaluateAll(org.kie.dmn.api.core.DMNContext context) {
                    return dmnRuntime.evaluateAll(dmnRuntime.getModel(namespace, name), context);
                }

                @Override
                public org.kie.dmn.api.core.DMNResult evaluateDecisionService(org.kie.dmn.api.core.DMNContext context, java.lang.String decisionServiceName) {
                    return dmnRuntime.evaluateDecisionService(dmnRuntime.getModel(namespace, name), context, decisionServiceName);
                }
            };
        }
    }
}
