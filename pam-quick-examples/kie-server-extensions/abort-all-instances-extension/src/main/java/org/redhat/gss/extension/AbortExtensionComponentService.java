package org.redhat.gss.extension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.RuntimeDataService;
import org.kie.server.services.api.KieServerApplicationComponentsService;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.api.SupportedTransports;
import org.kie.server.services.jbpm.JbpmKieServerExtension;

public class AbortExtensionComponentService implements KieServerApplicationComponentsService {

	private final String OWNER_EXTENSION = JbpmKieServerExtension.EXTENSION_NAME;

	public Collection<Object> getAppComponents(String extension, SupportedTransports type, Object... services) {

		// skip calls from other than owning extension
		if (!OWNER_EXTENSION.equals(extension)) {
			return Collections.emptyList();
		}

		ProcessService processService = null;
		RuntimeDataService runtimeDataService = null;
		KieServerRegistry registry = null;

		for (Object object : services) {
			if (ProcessService.class.isAssignableFrom(object.getClass())) {
				processService = (ProcessService) object;
				continue;
			} else if (RuntimeDataService.class.isAssignableFrom(object.getClass())) {
				runtimeDataService = (RuntimeDataService) object;
				continue;
			} else if (KieServerRegistry.class.isAssignableFrom(object.getClass())) {
				registry = (KieServerRegistry) object;
				continue;
			}
		}

		List<Object> components = new ArrayList<Object>(1);
		if (SupportedTransports.REST.equals(type)) {
			components.add(new AbortAllResource(processService, runtimeDataService, registry));
		}

		return components;
	}

}
