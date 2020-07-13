package com.redhat.services.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/custom")
public class CustomRSApi {

    Logger logger = LoggerFactory.getLogger(CustomRSApi.class);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response ping() {
        logger.info("custom endpoint invoked");
        return Response.ok().entity("Ping!").build();
    }
}