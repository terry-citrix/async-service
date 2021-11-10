package com.terrydu.asyncservice.api.controller;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * NOTE: Whenever you add a new Controller class, be sure to update JerseyConfig.java!
 */
@Path("/api/async/tenant")
public class AsyncTenantController {

    @GET
    @Path("/{tenantName}")
    @Produces({ MediaType.APPLICATION_JSON })
    public String getTenantByName(@PathParam("tenantName") String tenantName) {
        System.out.println("Handling request for '/api/async/tenant/" + tenantName + "' on thread " + Thread.currentThread().getName());

        ThreadLocal<String> threadLocalTenantName = new ThreadLocal<>();
        threadLocalTenantName.set(tenantName);

        // Call external API that takes 2 seconds here.

        return threadLocalTenantName.get();
    }

}
