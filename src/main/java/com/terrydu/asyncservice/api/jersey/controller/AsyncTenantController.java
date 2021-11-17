package com.terrydu.asyncservice.api.jersey.controller;

import org.apache.hc.core5.http.HttpStatus;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * NOTE: Whenever you add a new Controller class, be sure to update JerseyConfig.java!
 */
// This is relative to http://hostname/api/jersey
@Path("/async/harold")
public class AsyncTenantController {

    private static final String SERVICE_URL_5 = "https://terrydu-wait.azurewebsites.net/api/terrydu-wait5";
    private static final String SERVICE_URL_15 = "https://terrydu-wait.azurewebsites.net/api/terrydu-wait15";
    private static final String SERVICE_URL_60 = "https://terrydu-wait.azurewebsites.net/api/terrydu-wait60";
    private static final String SERVICE_URL_120 = "https://terrydu-wait.azurewebsites.net/api/terrydu-wait120";

    public abstract static class CemInvocationCallback<T, O> implements InvocationCallback<T> {
        private final O tenantObject;

        public CemInvocationCallback(ThreadLocal<O> threadLocal) {
            tenantObject = threadLocal.get();
        }

        @Override
        public void completed(T response) {
            ThreadLocal<O> threadLocalTenantName = new ThreadLocal<>();
            threadLocalTenantName.set(tenantObject);
            completed(threadLocalTenantName, response);
        }

        public abstract void completed(ThreadLocal<O> tenantStorage, T response);

        @Override
        public void failed(Throwable throwable) {
            ThreadLocal<O> threadLocalTenantName = new ThreadLocal<>();
            threadLocalTenantName.set(tenantObject);
            failed(threadLocalTenantName, throwable);
        }

        public abstract void failed(ThreadLocal<O> tenantStorage, Throwable throwable);
    }

    @GET
    @Path("/{tenantName}")
    @Produces({ MediaType.APPLICATION_JSON })
    public void getTenantByName(@PathParam("tenantName") String tenantName, @Suspended final AsyncResponse asyncResponse) {
        System.out.println("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName + ": Handling request for '/api/jersey/async/harold/" + tenantName + "'");
        long startTime = System.currentTimeMillis();

        ThreadLocal<String> threadLocalTenantName = new ThreadLocal<>();
        threadLocalTenantName.set(tenantName);

        // Call external API that takes 2 minutes here.
        ClientBuilder.newClient().target(SERVICE_URL_15).request().async().get(
                new CemInvocationCallback<Response, String>(threadLocalTenantName) {
            @Override
            public void completed(ThreadLocal<String> tenantStorage, Response response) {
                int statusCode = response.getStatus();
                if (statusCode != HttpStatus.SC_OK) {
                    System.err.println("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName + ": ERROR from the external network service call! Status returned: " + statusCode);
                } else {
                    String result = response.readEntity(String.class);
                    if (!tenantName.equals(tenantStorage.get())) {
                        System.err.println("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName + ": ERROR - The value in thread local storage (" + tenantStorage.get() + ") does not match the correct value (" + tenantName + ")");
                    }
                    long endTime = System.currentTimeMillis();
                    long timeElapsed = endTime - startTime;
                    System.out.println("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName + ": Completing request for '/api/jersey/sync/harold/" + tenantName + "' taking " + timeElapsed + " ms");
                    asyncResponse.resume(tenantStorage.get() + "-" +result);
                }
                response.close();
            }

            @Override
            public void failed(ThreadLocal<String> tenantStorage, Throwable throwable) {
                System.out.println("Invocation failed.");
                asyncResponse.resume("Invocation failed.");
                throwable.printStackTrace();
            }
        });
    }
}
