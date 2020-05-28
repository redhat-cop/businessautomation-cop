package com.redhat.cop.pam.example4.kie.api.utils;

import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieContainer;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;


@ApplicationScoped
public class KieContainerUtils {

    private KieContainer kieContainer;

    @PostConstruct
    public void initKieContainer() {
        final KieServices kieServices = KieServices.Factory.get();
        final ReleaseId dmnKjarReleaseId = kieServices.newReleaseId("com.redhat.cop.pam", "dmn-example4-dmn-kjar", "1.0");
        this.kieContainer = kieServices.newKieContainer(dmnKjarReleaseId);
        final ReleaseId processKjarReleaseId = kieServices.newReleaseId("com.redhat.cop.pam", "dmn-example4-process-kjar", "1.0");
        this.kieContainer = kieServices.newKieContainer(processKjarReleaseId);
    }

    public KieContainer getKieContainer() {
        return kieContainer;
    }
}
