package com.terrydu.asyncservice.api.jersey.controller;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * NOTE: Whenever you add a new Controller class, be sure to update JerseyConfig.java!
 */
@Path("/api/jersey/async/tenant")
public class AsyncTenantController {

    private static final String SERVICE_URL_15 = "https://terrydu-wait.azurewebsites.net/api/terrydu-wait15";
    private static final String SERVICE_URL_60 = "https://terrydu-wait.azurewebsites.net/api/terrydu-wait60";
    private static final String SERVICE_URL_120 = "https://terrydu-wait.azurewebsites.net/api/terrydu-wait120";

    @GET
    @Path("/{tenantName}")
    @Produces({ MediaType.APPLICATION_JSON })
    public String getTenantByName(@PathParam("tenantName") String tenantName) {
        System.out.println("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName + ": Handling request for '/api/jersey/async/tenant/" + tenantName + "'");
        long startTime = System.currentTimeMillis();

        ThreadLocal<String> threadLocalTenantName = new ThreadLocal<>();
        threadLocalTenantName.set(tenantName);

        String httpResponse = "";
        // TODO: Call external API that takes 2 seconds here.



        if (!tenantName.equals(threadLocalTenantName.get())) {
            System.err.println("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName + ": ERROR - The value in thread local storage (" + threadLocalTenantName.get() + ") does not match the correct value (" + tenantName + ")");
        }

        long endTime = System.currentTimeMillis();
        long timeElapsed = endTime - startTime;
        System.out.println("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName + ": Completing request for '/api/jersey/async/tenant/" + tenantName + "' taking " + timeElapsed + " ms");
        return threadLocalTenantName.get() + "-" + httpResponse;
    }

}
