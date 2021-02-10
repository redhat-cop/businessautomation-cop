package com.redhat.pam.runtime;

import org.kie.server.api.model.ReleaseId;
import org.kie.server.client.KieServicesClient;

import java.util.function.Supplier;

public abstract class BDDRuntime {

    private final ReleaseId releaseId;

    public BDDRuntime(final ReleaseId releaseId){
        this.releaseId = releaseId;
    }

    public ReleaseId getReleaseId() {
        return releaseId;
    }

    public abstract Supplier<KieServicesClient> getKieServiceAdminClientSupplier();

    public abstract Supplier<KieServicesClient> getKieServiceUserClientSupplier(final String username);

}
