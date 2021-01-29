package com.redhat.cop.pam.example5.kie.api.utils;

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
        final ReleaseId releaseId = kieServices.newReleaseId("com.redhat.cop.pam", "dmn-example5-kjar", "1.0");
        this.kieContainer = kieServices.newKieContainer(releaseId);
    }

    public KieContainer getKieContainer() {
        return kieContainer;
    }
}
