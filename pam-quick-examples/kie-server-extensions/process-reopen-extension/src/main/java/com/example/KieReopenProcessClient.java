package com.example;

import org.kie.server.client.ProcessServicesClient;

public interface KieReopenProcessClient extends ProcessServicesClient {

	Long reopenProcess(Long instanceId);

}