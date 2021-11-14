package com.terrydu.asyncservice.api.mvc.controller;

import org.apache.hc.client5.http.async.methods.*;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.message.StatusLine;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.CompletableFuture;

@RestController
public class TerryAsyncCompletableFuture {

    private static final String SERVICE_URL_5 = "https://terrydu-wait.azurewebsites.net/api/terrydu-wait5";
    private static final String SERVICE_URL_15 = "https://terrydu-wait.azurewebsites.net/api/terrydu-wait15";
    private static final String SERVICE_URL_60 = "https://terrydu-wait.azurewebsites.net/api/terrydu-wait60";
    private static final String SERVICE_URL_120 = "https://terrydu-wait.azurewebsites.net/api/terrydu-wait120";

    @GetMapping("/api/mvc/terryasynccompletable/tenant/{tenantName}")
    @Async
    public CompletableFuture syncTenant(@PathVariable String tenantName,
                                        HttpServletRequest servletRequest,
                                        HttpServletResponse servletResponse) {
        System.out.println("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName
                + ": Handling request for '/api/mvc/terryasynccompletable/tenant/" + tenantName + "'");
        long startTime = System.currentTimeMillis();

        ThreadLocal<String> threadLocalTenantName = new ThreadLocal<>();
        threadLocalTenantName.set(tenantName);

        // Call external API that takes a long time here.
        try (CloseableHttpAsyncClient client = HttpAsyncClients.custom().build()) {
            client.start();

            final HttpHost target = new HttpHost("terrydu-wait.azurewebsites.net");
            final SimpleHttpRequest request = SimpleRequestBuilder.get()
                    .setHttpHost(target)
                    .setPath("/api/terrydu-wait15")
                    .build();

            final CompletableFuture<SimpleHttpResponse> future = (CompletableFuture<SimpleHttpResponse>) client.execute(
                    SimpleRequestProducer.create(request),
                    SimpleResponseConsumer.create(),
                    new FutureCallback<SimpleHttpResponse>() {

                        @Override
                        public void completed(final SimpleHttpResponse httpResponse) {
                            System.out.println("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName
                                    + ": " + request + "->" + new StatusLine(httpResponse));
                            SimpleBody httpBody = httpResponse.getBody();
                            String textResponse = httpBody.getBodyText();

                            if (!tenantName.equals(threadLocalTenantName.get())) {
                                System.err.println("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName
                                        + ": ERROR - The value in thread local storage (" + threadLocalTenantName.get()
                                        + ") does not match the correct value (" + tenantName + ")");
                            }

                            long endTime = System.currentTimeMillis();
                            long timeElapsed = endTime - startTime;
                            System.out.println("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName
                                    + ": Completing request for '/api/mvc/terryasynccompletable/tenant/" + tenantName
                                    + "' taking " + timeElapsed + " ms");
                            try {
                                PrintWriter printWriter = servletResponse.getWriter();
                                printWriter.println(threadLocalTenantName.get() + "-" + textResponse);
                                printWriter.close();
                            } catch (IOException ex) {
                                System.err.println("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName
                                        + ": ERROR IOException during writing response back to device for " + request + " -> " + ex.getMessage());
                            }
                        }

                        @Override
                        public void failed(final Exception ex) {
                            System.err.println("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName
                                    + ": ERROR - failed case for " + request + "->" + ex);
                        }

                        @Override
                        public void cancelled() {
                            System.out.println("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName
                                    + ": ERROR - cancelled case for " + request);
                        }

                    });

            return future;
        } catch (IOException ex) {
            String errorMsg = "Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName + ": ERROR ->" + ex.getMessage();
            System.err.println(errorMsg);
            throw new RuntimeException(errorMsg, ex);
        }
    }
}
