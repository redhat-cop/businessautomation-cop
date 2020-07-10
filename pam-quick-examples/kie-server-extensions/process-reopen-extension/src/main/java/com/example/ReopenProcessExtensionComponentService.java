package com.example;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.admin.ProcessInstanceAdminService;
import org.kie.server.services.api.KieServerApplicationComponentsService;
import org.kie.server.services.api.SupportedTransports;
import org.kie.server.services.jbpm.JbpmKieServerExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReopenProcessExtensionComponentService implements KieServerApplicationComponentsService {
	private static final String OWNER_EXTENSION = JbpmKieServerExtension.EXTENSION_NAME;
	private static final String PU_NAME = "org.jbpm.domain";
	Logger logger = LoggerFactory.getLogger(ReopenProcessExtensionComponentService.class);

	@Override
	public Collection<Object> getAppComponents(String extension, SupportedTransports supportedTransports,
			Object... services) {

		if (!OWNER_EXTENSION.equals(extension)) {
			return Collections.emptyList();
		}

		logger.info("Registering ReopenProcessExtensionComponentService");
		
		RuntimeDataService runtimeService = null;
		ProcessInstanceAdminService adminService = null;
		ProcessService processService = null;

		for (Object object : services) {
			logger.info("found service {}", object);
			if (RuntimeDataService.class.isAssignableFrom(object.getClass())) {
				runtimeService = (RuntimeDataService) object;
				logger.info("Found runtimeService  service {}", runtimeService);
				continue;
			} else if (ProcessInstanceAdminService.class.isAssignableFrom(object.getClass())) {
				adminService = (ProcessInstanceAdminService) object;
				logger.info("Found admin  service {}", adminService);
				continue;
			}
			
			else if (ProcessService.class.isAssignableFrom(object.getClass())) {
				processService = (ProcessService) object;
				logger.info("Found process  service {}", adminService);
				continue;
			}
		}

		List<Object> components = new ArrayList<Object>(1);
		if (SupportedTransports.REST.equals(supportedTransports)) {
			components.add(new ReopenProcessResource(runtimeService,adminService,processService));
		}
		logger.info("ReopenProcessExtensionComponentService registered");

		return components;
	}
}