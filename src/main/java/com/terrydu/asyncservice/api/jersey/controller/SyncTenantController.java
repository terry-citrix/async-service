package com.terrydu.asyncservice.api.jersey.controller;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

/**
 * NOTE: Whenever you add a new Controller class, be sure to update JerseyConfig.java!
 */
@Path("/api/jersey/sync/tenant")
public class SyncTenantController {

    private static final String SERVICE_URL_15 = "https://terrydu-wait.azurewebsites.net/api/terrydu-wait15";
    private static final String SERVICE_URL_60 = "https://terrydu-wait.azurewebsites.net/api/terrydu-wait60";
    private static final String SERVICE_URL_120 = "https://terrydu-wait.azurewebsites.net/api/terrydu-wait120";

    @GET
    @Path("/{tenantName}")
    @Produces({ MediaType.APPLICATION_JSON })
    public String getTenantByName(@PathParam("tenantName") String tenantName) {
        System.out.println("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName + ": Handling request for '/api/jersey/sync/tenant/" + tenantName + "'");
        long startTime = System.currentTimeMillis();

        ThreadLocal<String> threadLocalTenantName = new ThreadLocal<>();
        threadLocalTenantName.set(tenantName);

        // Call external API that takes 2 minutes here.
        final HttpUriRequest request = new HttpGet(SERVICE_URL_15);

        try {
            final HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                System.err.println("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName + ": ERROR from the external network service call! Status returned: " + statusCode);
            }
        } catch (IOException ex) {
            System.err.println("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName + ": ERROR waiting for the external network service call!");
            // Other than logging the error we are ignoring this exception.
        }

        if (!tenantName.equals(threadLocalTenantName.get())) {
            System.err.println("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName + ": ERROR - The value in thread local storage (" + threadLocalTenantName.get() + ") does not match the correct value (" + tenantName + ")");
        }

        long endTime = System.currentTimeMillis();
        long timeElapsed = endTime - startTime;
        System.out.println("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName + ": Completing request for '/api/jersey/sync/tenant/" + tenantName + "' taking " + timeElapsed + " ms");
        return threadLocalTenantName.get();
    }

}
