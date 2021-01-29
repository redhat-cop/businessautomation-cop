package com.example;

import static org.kie.server.remote.rest.common.util.RestUtils.createResponse;
import static org.kie.server.remote.rest.common.util.RestUtils.getContentType;
import static org.kie.server.remote.rest.common.util.RestUtils.getVariant;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;

import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.admin.ProcessInstanceAdminService;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("server/process")
public class ReopenProcessResource {

	private static final Logger logger = LoggerFactory.getLogger(ReopenProcessResource.class);
	private static final String EMPTY_RESULT = "[ ]";
	private Marshaller marshaller;
	private ReopenProcessService reopenService;

	public ReopenProcessResource(RuntimeDataService runtimeService, ProcessInstanceAdminService adminService,ProcessService processService) {
		this.reopenService = new ReopenProcessService(runtimeService, adminService,processService);
		this.marshaller = MarshallerFactory.getMarshaller(MarshallingFormat.JSON,
				ReopenProcessResource.class.getClassLoader());
	}

	@POST
	@Path("/reopen/{instanceId}")
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response queryCases(@Context HttpHeaders headers, @PathParam("instanceId") Long instanceId,
			List<String> nodes) {

		logger.info("Reopening process instance with id {} and nodes {}", instanceId, nodes);

		Variant v = getVariant(headers);
		String contentType = getContentType(headers);
		MarshallingFormat format = MarshallingFormat.fromType(contentType);
		if (format == null) {
			format = MarshallingFormat.valueOf(contentType);
		}

		try {

			Long newPid = reopenService.reopenProcess(instanceId,nodes);
			return createResponse(newPid, v, Response.Status.OK);

		} catch (Exception e) {

			// in case marshalling failed return the call container response to
			// keep backward compatibility
			e.printStackTrace();
			String response = "Execution failed with error : " + e.getMessage();
			logger.error("Returning Failure response with content '{}'", response);
			return createResponse(response, v, Response.Status.INTERNAL_SERVER_ERROR);

		}
	}

}
