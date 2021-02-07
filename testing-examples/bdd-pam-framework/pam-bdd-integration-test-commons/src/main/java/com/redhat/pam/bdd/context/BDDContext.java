package com.redhat.pam.bdd.context;

import com.redhat.pam.runtime.BDDRuntime;
import io.cucumber.guice.ScenarioScoped;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.KieServerConstants;
import org.kie.server.client.KieServicesClient;

@ScenarioScoped
public class BDDContext {

    private final BDDRuntime bddRuntime;

    private final boolean bypassAuthUser;

    private Long processInstanceId;

    public BDDContext(final BDDRuntime bddRuntime){
        this.bddRuntime = bddRuntime;
        this.bypassAuthUser = Boolean.parseBoolean(System.getProperty(KieServerConstants.CFG_BYPASS_AUTH_USER, "false"));
    }

    public KieServicesClient getKieServerClient() {
        return bddRuntime.getKieServiceAdminClientSupplier().get();
    }

    public KieServicesClient getKieServerClient(final String username) {
        if(bypassAuthUser) {
            return getKieServerClient();
        } else {
            return bddRuntime.getKieServiceUserClientSupplier(username).get();
        }
    }

    public <T> T getServicesClient(final String username, final Class<T> serviceClient) {
        if(bypassAuthUser) {
            return getKieServerClient().getServicesClient(serviceClient);
        } else {
            return getKieServerClient(username).getServicesClient(serviceClient);
        }
    }

    public <T> T getServicesClient(final Class<T> serviceClient) {
        return getKieServerClient().getServicesClient(serviceClient);
    }

    public ReleaseId getReleaseId() {
        return bddRuntime.getReleaseId();
    }

    public boolean isBypassAuthUser() {
        return bypassAuthUser;
    }

    public Long getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(Long processInstanceId) {
        this.processInstanceId = processInstanceId;
    }
}
