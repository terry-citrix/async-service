package com.terrydu.asyncservice.api.jersey.controller;

import org.apache.hc.client5.http.async.methods.*;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.message.StatusLine;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.util.Timeout;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * NOTE: Whenever you add a new Controller class, be sure to update JerseyConfig.java!
 */
// This is relative to http://hostname/api/jersey
@Path("/terryapachehttpasyncclient/tenant")
public class TerryApacheHttpAsyncClient {

    private static final String SERVICE_URL_5 = "https://terrydu-wait.azurewebsites.net/api/terrydu-wait5";
    private static final String SERVICE_URL_15 = "https://terrydu-wait.azurewebsites.net/api/terrydu-wait15";
    private static final String SERVICE_URL_60 = "https://terrydu-wait.azurewebsites.net/api/terrydu-wait60";
    private static final String SERVICE_URL_120 = "https://terrydu-wait.azurewebsites.net/api/terrydu-wait120";

    @GET
    @Path("/{tenantName}")
    @Produces({ MediaType.APPLICATION_JSON })
    public String getTenantByName(@PathParam("tenantName") String tenantName) {
        System.out.println("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName + ": Handling request for '/api/jersey/terryapachehttpasyncclient/tenant/" + tenantName + "'");
        long startTime = System.currentTimeMillis();

        ThreadLocal<String> threadLocalTenantName = new ThreadLocal<>();
        threadLocalTenantName.set(tenantName);

        // Call external API that takes 2 minutes here.
        final IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
                .setSoTimeout(Timeout.ofSeconds(180))
                .build();

        final CloseableHttpAsyncClient client = HttpAsyncClients.custom()
                .setIOReactorConfig(ioReactorConfig)
                .build();

        try {
            client.start();

            final HttpHost target = new HttpHost("terrydu-wait.azurewebsites.net");
            final SimpleHttpRequest request = SimpleRequestBuilder.get()
                    .setHttpHost(target)
                    .setPath("/api/terrydu-wait15")
                    .build();

            final Future<SimpleHttpResponse> future = client.execute(
                    SimpleRequestProducer.create(request),
                    SimpleResponseConsumer.create(),
                    new FutureCallback<SimpleHttpResponse>() {

                        @Override
                        public void completed(final SimpleHttpResponse response) {
                            System.out.println("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName + ": " + request + "->" + new StatusLine(response));
                        }

                        @Override
                        public void failed(final Exception ex) {
                            System.err.println("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName + ": " + request + "->" + ex);
                        }

                        @Override
                        public void cancelled() {
                            System.out.println("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName + ": ERROR for " + request + " - cancelled");
                        }

                    });

            boolean isDone = false;
            int sleepTime = 5 * 1000;

            // Wait at most 3 minutes
            for (int i = 0; i < 3 * 60 * 1000 / sleepTime; i++) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException ex) {
                    System.err.println("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName + ": ERROR - was interrupted during the sleep!");
                }
                if (future.isDone()) {
                    try {
                        SimpleHttpResponse response = future.get();
                    } catch (InterruptedException ex) {
                        System.err.println("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName + ": ERROR - was interrupted during the Future.get!");
                    } catch (ExecutionException ex) {
                        System.err.println("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName + ": ERROR - got an execution exception during the Future.get!");
                    }
                    isDone = true;
                    break;
                } else if (future.isCancelled()) {
                    isDone = true;
                    break;
                }
            }
        } finally {
            client.close(CloseMode.GRACEFUL);
        }

        if (!tenantName.equals(threadLocalTenantName.get())) {
            System.err.println("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName + ": ERROR - The value in thread local storage (" + threadLocalTenantName.get() + ") does not match the correct value (" + tenantName + ")");
        }

        long endTime = System.currentTimeMillis();
        long timeElapsed = endTime - startTime;
        System.out.println("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName + ": Completing request for '/api/jersey/terryapachehttpasyncclient/tenant/" + tenantName + "' taking " + timeElapsed + " ms");
        return threadLocalTenantName.get();
    }

}
