package com.example;

import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.impl.ProcessServicesClientImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KieReopenProcessClientImpl extends ProcessServicesClientImpl implements KieReopenProcessClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(KieReopenProcessClientImpl.class);

	private static final String REOPEN_PROCESS = "/server/process/reopen"; // FIX ME

	public KieReopenProcessClientImpl(KieServicesConfiguration config) {
		super(config);
	}

	public KieReopenProcessClientImpl(KieServicesConfiguration config, ClassLoader classLoader) {
		super(config, classLoader);
	}

	@Override
	public Long reopenProcess(Long instanceId) {
		String endpoint = this.loadBalancer.getUrl() + REOPEN_PROCESS + String.valueOf(instanceId);
		return this.makeHttpPostRequestAndCreateCustomResponse(endpoint, "", Long.class);

	}

}