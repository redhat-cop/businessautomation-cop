package org.redhat.gss.extension;

import static org.redhat.gss.extension.RESTUtils.createResponse;
import static org.redhat.gss.extension.RESTUtils.getContentType;
import static org.redhat.gss.extension.RESTUtils.getVariant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;

import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.cases.CaseFile;
import org.kie.server.api.model.cases.CaseInstance;
import org.kie.server.api.model.cases.CaseInstanceList;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.casemgmt.CaseManagementRuntimeDataServiceBase;
import org.kie.server.services.casemgmt.CaseManagementServiceBase;
import org.kie.server.services.casemgmt.locator.ByCaseIdContainerLocator;
import org.kie.server.services.impl.marshal.MarshallerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("server/containers/cases")
public class GetCasesWithDataResource {

	private static final Logger logger = LoggerFactory.getLogger(GetCasesWithDataResource.class);

	private KieServerRegistry registry;

	private CaseManagementServiceBase caseManagementServiceBase;
	private CaseManagementRuntimeDataServiceBase caseManagementRuntimeDataService;

	public GetCasesWithDataResource(CaseManagementServiceBase caseManagementServiceBase,
			CaseManagementRuntimeDataServiceBase caseManagementRuntimeDataService, KieServerRegistry registry) {
		this.caseManagementServiceBase = caseManagementServiceBase;
		this.caseManagementRuntimeDataService = caseManagementRuntimeDataService;
		this.registry = registry;

	}

	@SuppressWarnings("unchecked")
	@GET
	@Path("/instancesWithData")
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response getCaseInstancesWithData(@Context HttpHeaders headers,
			@QueryParam("dataItem") List<String> dataItems) {

		Variant v = getVariant(headers);
		String contentType = getContentType(headers);
		MarshallingFormat format = MarshallingFormat.fromType(contentType);
		if (format == null) {
			format = MarshallingFormat.valueOf(contentType);
		}
		MarshallerHelper marshallerHelper = new MarshallerHelper(registry);

		try {
			
			//Fetching all OPEN cases
			CaseInstanceList cases = this.caseManagementRuntimeDataService
					.getCaseInstancesAnyRole(Arrays.asList("open"), 0, 0, "", true);
			List<CaseInstance> finalList = new ArrayList<CaseInstance>();

			//for every case instance found, let's fetch the case data specified in the query parameter 'dateItems'
			for (CaseInstance caseInstance : cases.getItems()) {

				String caseFileData = caseManagementServiceBase.getCaseFileData(caseInstance.getContainerId(),
						caseInstance.getCaseId(), dataItems, format.toString());

				logger.debug("Following data were fetched :" + caseFileData + "\n for case instance id:"
						+ caseInstance.getCaseId());

				Map<String, Object> caseFileDataUnmarshalled = marshallerHelper.unmarshal(caseInstance.getContainerId(),
						caseFileData, format.toString(), Map.class,
						new ByCaseIdContainerLocator(caseInstance.getCaseId()));
				caseInstance.setCaseFile(CaseFile.builder().data(caseFileDataUnmarshalled).build());

				finalList.add(caseInstance);

			}

			cases.setCaseInstances(finalList.toArray(new CaseInstance[finalList.size()]));
			String result = marshallerHelper.marshal(format.toString(), cases);
			logger.debug("Returning OK response with content '{}'", result);

			return createResponse(result, v, Response.Status.OK);
		} catch (Exception e) {

			// backward compatibility
			String response = "Execution failed with error : " + e.getMessage();
			logger.debug("Returning Failure response with content '{}'", response);
			return createResponse(response, v, Response.Status.INTERNAL_SERVER_ERROR);

		}

	}
}
