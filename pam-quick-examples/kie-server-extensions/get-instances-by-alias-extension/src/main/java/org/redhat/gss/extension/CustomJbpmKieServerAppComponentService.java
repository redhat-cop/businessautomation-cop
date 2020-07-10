package org.redhat.gss.extension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jbpm.services.api.RuntimeDataService;
import org.kie.server.services.api.KieServerApplicationComponentsService;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.api.SupportedTransports;
import org.kie.server.services.jbpm.RuntimeDataServiceBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomJbpmKieServerAppComponentService implements KieServerApplicationComponentsService {

	// https://github.com/kiegroup/droolsjbpm-integration/blob/7.11.x/kie-server-parent/kie-server-services/kie-server-services-jbpm/src/main/java/org/kie/server/services/jbpm/JbpmKieServerExtension.java
	private static final String OWNER_EXTENSION = "jBPM";
	private static final Logger logger = LoggerFactory.getLogger(CustomJbpmKieServerAppComponentService.class);

	public Collection<Object> getAppComponents(String extension, SupportedTransports type, Object... services) {
		// skip calls from other than owning extension
		if (!OWNER_EXTENSION.equals(extension)) {
			return Collections.emptyList();
		}

		logger.debug("Creating custom kie server extension CustomJbpmKieServerAppComponentService");

		RuntimeDataService runtimeDataService = null;
		KieServerRegistry context = null;

		for (Object object : services) {
			if (RuntimeDataService.class.isAssignableFrom(object.getClass())) {
				runtimeDataService = (RuntimeDataService) object;
				continue;
			} else if (KieServerRegistry.class.isAssignableFrom(object.getClass())) {

				context = (KieServerRegistry) object;
				continue;
			}
		}

		List<Object> components = new ArrayList<Object>(1);
		if (SupportedTransports.REST.equals(type)) {
			RuntimeDataServiceBase runtimeDataServiceBase = new RuntimeDataServiceBase(runtimeDataService, context);

			components.add(new CustomResource(runtimeDataServiceBase, context));
		}

		return components;
	}

}
