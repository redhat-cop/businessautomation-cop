package org.redhat.gss.extension;

import static org.kie.server.remote.rest.common.util.RestUtils.createResponse;
import static org.kie.server.remote.rest.common.util.RestUtils.getContentType;
import static org.kie.server.remote.rest.common.util.RestUtils.getVariant;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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
import org.jbpm.services.api.model.ProcessInstanceDesc;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.impl.marshal.MarshallerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/server/containers/{id}/processes/instances")
public class AbortAllResource {

	private static final Logger logger = LoggerFactory.getLogger(AbortAllResource.class);

	private ProcessService processService;
	private KieServerRegistry registry;
	private RuntimeDataService runtimeDataService;

	public AbortAllResource(ProcessService processService, RuntimeDataService runtimeDataService,
			KieServerRegistry registry) {
		this.processService = processService;
		this.runtimeDataService = runtimeDataService;
		this.registry = registry;
	}

	@DELETE
	@Path("/abortAll")
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response abortAllContainerInstances(@Context HttpHeaders headers, @PathParam("id") String containerId) {

		Variant v = getVariant(headers);
		String contentType = getContentType(headers);

		MarshallingFormat format = MarshallingFormat.fromType(contentType);
		if (format == null) {
			format = MarshallingFormat.valueOf(contentType);
		}
		MarshallerHelper marshallerHelper = new MarshallerHelper(registry);

		try {

			// get active instances
			Collection<ProcessInstanceDesc> activeInstances = runtimeDataService
					.getProcessInstancesByDeploymentId(containerId, Arrays.asList(1) /* active */, null);
			

			// extract their pids
			List<Long> pids = activeInstances.stream().map(activeInstance -> activeInstance.getId())
					.collect(Collectors.toList());

			// abort them all
			processService.abortProcessInstances(containerId, pids);

			String result = marshallerHelper.marshal(format.toString(), pids);
			logger.debug("Aborted following instances:" + result + ", in a following container:" + containerId);
			return createResponse(result, v, Response.Status.OK);

		} catch (Exception e) {
			// in case marshalling failed return the call container response to keep
			// backward compatibility
			String response = "Execution failed with error : " + e.getMessage();
			logger.debug("Returning Failure response with content '{}'", response);
			return createResponse(response, v, Response.Status.INTERNAL_SERVER_ERROR);
		}

	}

}
