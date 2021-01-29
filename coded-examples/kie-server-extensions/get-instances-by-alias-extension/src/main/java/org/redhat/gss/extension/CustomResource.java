package org.redhat.gss.extension;

import static org.redhat.gss.extension.RESTUtils.createResponse;
import static org.redhat.gss.extension.RESTUtils.getContentType;
import static org.redhat.gss.extension.RESTUtils.getVariant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;

import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.ProcessInstanceList;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.impl.KieContainerInstanceImpl;
import org.kie.server.services.impl.marshal.MarshallerHelper;
import org.kie.server.services.jbpm.RuntimeDataServiceBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("server/containers/processInstancesByAlias")
public class CustomResource {

	private static final Logger logger = LoggerFactory.getLogger(CustomResource.class);

	private RuntimeDataServiceBase runtimeDataService;
	private KieServerRegistry context;

	public CustomResource(RuntimeDataServiceBase runtimeDataService, KieServerRegistry context) {

		this.runtimeDataService = runtimeDataService;
		this.context = context;
	}

	@GET
	@Path("/{aliasId}")
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response insertFireReturn(@Context HttpHeaders headers, @PathParam("aliasId") String alias) {

		Variant v = getVariant(headers);
		String contentType = getContentType(headers);

		MarshallingFormat format = MarshallingFormat.fromType(contentType);
		if (format == null) {
			format = MarshallingFormat.valueOf(contentType);
		}
		MarshallerHelper marshallerHelper = new MarshallerHelper(context);

		try {

			// get all the containers with the given alias
			List<KieContainerInstanceImpl> filteredContainers = context.getContainersForAlias(alias);
			List<ProcessInstance> instances = new ArrayList<ProcessInstance>();

			// for each container obtain a list of active process instances
			filteredContainers.forEach(container -> {
				String containerId = container.getContainerId();

				ProcessInstanceList instanceList = runtimeDataService.getProcessInstancesByDeploymentId(containerId,
						Arrays.asList(1), 0, 0, "", true);
				logger.debug("Found " + instanceList.getItems().size() + " active process instances  in container with id " + containerId);

				instances.addAll(instanceList.getItems());

			});

			// return the list to the client
			ProcessInstanceList finalList = new ProcessInstanceList(instances);
			String result = marshallerHelper.marshal(format.toString(), finalList);
			logger.debug("Returning OK response with content '{}'", result);

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
