package org.redhat.gss.extension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.kie.server.services.api.KieServerApplicationComponentsService;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.api.SupportedTransports;
import org.kie.server.services.casemgmt.CaseManagementRuntimeDataServiceBase;
import org.kie.server.services.casemgmt.CaseManagementServiceBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomCaseMgmtKieServerAppComponentService implements KieServerApplicationComponentsService {

	private static final String OWNER_EXTENSION = "Case-Mgmt";
	private static final Logger logger = LoggerFactory.getLogger(CustomCaseMgmtKieServerAppComponentService.class);

	public Collection<Object> getAppComponents(String extension, SupportedTransports type, Object... services) {
		// skip calls from other than owning extension
		if (!OWNER_EXTENSION.equals(extension)) {
			return Collections.emptyList();
		}
		logger.info("Creating custom kie server extension CustomCaseMgmtKieServerAppComponentService");

		KieServerRegistry registry = null;

		CaseManagementServiceBase caseManagementServiceBase = null;
		CaseManagementRuntimeDataServiceBase caseManagementRuntimeDataService = null;

		for (Object object : services) {
			if (CaseManagementServiceBase.class.isAssignableFrom(object.getClass())) {
				caseManagementServiceBase = (CaseManagementServiceBase) object;
				continue;
			} else if (KieServerRegistry.class.isAssignableFrom(object.getClass())) {
				registry = (KieServerRegistry) object;
				continue;
			}

			else if (CaseManagementRuntimeDataServiceBase.class.isAssignableFrom(object.getClass())) {
				caseManagementRuntimeDataService = (CaseManagementRuntimeDataServiceBase) object;
			}
		}

		List<Object> components = new ArrayList<Object>(1);
		if (SupportedTransports.REST.equals(type)) {

			components.add(new GetCasesWithDataResource(caseManagementServiceBase, caseManagementRuntimeDataService,
					registry));
		}

		return components;
	}

}
