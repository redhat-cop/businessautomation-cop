package com.redhat.cop.pam.example4.services.api;

import com.redhat.cop.pam.example4.Customer;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/services")
public interface ServicesApi {

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/customer")
    public Response canOpenAccount(final Customer customer);

}
