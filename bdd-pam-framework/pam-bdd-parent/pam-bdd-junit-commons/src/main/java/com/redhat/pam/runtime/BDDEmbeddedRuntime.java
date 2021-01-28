package com.redhat.pam.runtime;

import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.drools.compiler.kie.builder.impl.KieContainerImpl;
import org.drools.compiler.kie.builder.impl.KieModuleKieProject;
import org.jbpm.test.services.TestIdentityProvider;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.manager.RuntimeEnvironment;
import org.kie.api.runtime.manager.RuntimeEnvironmentBuilder;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.manager.RuntimeManagerFactory;
import org.kie.api.task.UserGroupCallback;
import org.kie.internal.identity.IdentityProvider;
import org.kie.internal.runtime.conf.DeploymentDescriptor;
import org.kie.internal.runtime.conf.RuntimeStrategy;
import org.kie.internal.runtime.manager.InternalRuntimeManager;
import org.kie.internal.runtime.manager.deploy.DeploymentDescriptorIO;
import org.kie.test.util.db.PersistenceUtil;

import javax.persistence.EntityManagerFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

public abstract class BDDEmbeddedRuntime {

    private DeploymentDescriptor deploymentDescriptor;

    private KieContainer kieContainer;

    private String kieBase;

    private String kieSession;

    public RuntimeManager getRuntimeManager(final ReleaseId releaseId) {
        return getRuntimeManager(releaseId, null, null);
    }

    public RuntimeManager getRuntimeManager(final ReleaseId releaseId, final String kieBase, final String kieSession) {
        IdentityProvider c;
        this.kieContainer = KieServices.get().newKieContainer(releaseId);
        this.kieBase = kieBase;
        this.kieSession = kieSession;
        this.deploymentDescriptor = getDescriptorFromKModule(releaseId);
        final RuntimeEnvironment runtimeEnvironment = buildRuntimeEnvironment(releaseId, kieBase, kieSession);
        final RuntimeStrategy runtimeStrategy = deploymentDescriptor.getRuntimeStrategy();
        return getRuntimeManager(runtimeStrategy, runtimeEnvironment);
    }

    private RuntimeEnvironment buildRuntimeEnvironment(final ReleaseId releaseId, final String kieBase, final String kieSession) {
        final Map<String, Object> context = PersistenceUtil.setupWithPoolingDataSource("org.jbpm.domain", "java:jboss/datasources/ExampleDS", true);
        final EntityManagerFactory emf = (EntityManagerFactory) context.get("org.kie.api.persistence.jpa.EntityManagerFactory");
        return RuntimeEnvironmentBuilder.Factory.get()
                .newDefaultBuilder(releaseId, kieBase, kieSession)
                .entityManagerFactory(emf)
                .addEnvironmentEntry(EnvironmentName.IDENTITY_PROVIDER, new TestIdentityProvider())
                .userGroupCallback(getUserGroupCallback()).get();
    }

    private RuntimeManager getRuntimeManager(final RuntimeStrategy strategy, final RuntimeEnvironment runtimeEnvironment) {
        final RuntimeManager runtimeManager;
        switch (strategy) {
            case PER_CASE:
                runtimeManager = RuntimeManagerFactory.Factory.get().newPerCaseRuntimeManager(runtimeEnvironment);
                break;
            case SINGLETON:
                runtimeManager = RuntimeManagerFactory.Factory.get().newSingletonRuntimeManager(runtimeEnvironment);
                break;
            case PER_REQUEST:
                runtimeManager = RuntimeManagerFactory.Factory.get().newPerRequestRuntimeManager(runtimeEnvironment);
                break;
            case PER_PROCESS_INSTANCE:
                runtimeManager = RuntimeManagerFactory.Factory.get().newPerProcessInstanceRuntimeManager(runtimeEnvironment);
                break;
            default:
                throw new NotSupportedRuntimeStrategyException("Runtime strategy " + strategy + " not recognized");

        }
        ((InternalRuntimeManager)runtimeManager).setKieContainer(kieContainer);
        return runtimeManager;
    }

    public abstract UserGroupCallback getUserGroupCallback();

    private DeploymentDescriptor getDescriptorFromKModule(final ReleaseId releaseId) {
        InternalKieModule kieModule = ((KieModuleKieProject) ((KieContainerImpl) kieContainer).getKieProject()).getInternalKieModule();
        DeploymentDescriptor desc = null;
        if (kieModule.isAvailable("META-INF/kie-deployment-descriptor.xml")) {
            byte[] content = kieModule.getBytes("META-INF/kie-deployment-descriptor.xml");
            ByteArrayInputStream input = new ByteArrayInputStream(content);
            try {
                desc = DeploymentDescriptorIO.fromXml(input);
            } finally {
                try {
                    input.close();
                } catch (IOException var10) {
                    throw new RuntimeException(var10);
                }
            }
        }
        return desc;
    }
}
 