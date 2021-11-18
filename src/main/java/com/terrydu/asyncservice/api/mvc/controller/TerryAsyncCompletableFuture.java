package com.terrydu.asyncservice.api.mvc.controller;

import com.terrydu.asyncservice.common.FutureCallbackWrapper;
import com.terrydu.asyncservice.common.TenantContext;
import org.apache.hc.client5.http.async.methods.*;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.core5.http.HttpHost;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.CompletableFuture;

@RestController
public class TerryAsyncCompletableFuture {

    private static final String SERVICE_URL_5 = "https://terrydu-wait.azurewebsites.net/api/terrydu-wait5";
    private static final String SERVICE_URL_15 = "https://terrydu-wait.azurewebsites.net/api/terrydu-wait15";
    private static final String SERVICE_URL_60 = "https://terrydu-wait.azurewebsites.net/api/terrydu-wait60";
    private static final String SERVICE_URL_120 = "https://terrydu-wait.azurewebsites.net/api/terrydu-wait120";

    @GetMapping("/api/mvc/terryasynccompletable/tenant/{tenantName}")
    @Async
    public CompletableFuture<String> syncTenant(@PathVariable String tenantName,
                                        HttpServletRequest servletRequest,
                                        HttpServletResponse servletResponse) {
        System.out.println("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName
                + ": Handling request for '/api/mvc/terryasynccompletable/tenant/" + tenantName + "'");

        // Set Thread Local Storage
        TenantContext.tenantName.set(tenantName);

        //
        // Call external API that takes a long time here.
        //
        CloseableHttpAsyncClient client = HttpAsyncClients.custom().build();
        client.start();

        final HttpHost target = new HttpHost("terrydu-wait.azurewebsites.net");
        final SimpleHttpRequest request = SimpleRequestBuilder.get()
                .setHttpHost(target)
                .setPath("/api/terrydu-wait15")
                .build();

        // We need a CompletableFuture, but client.execute() returns a Future. So we create another async mechanism.
        // See https://stackoverflow.com/questions/23301598/transform-java-future-into-a-completablefuture
        CompletableFuture<String> completableFuture = new CompletableFuture<>();

        FutureCallbackWrapper wrapper = new FutureCallbackWrapper(
                servletRequest,             // Not actually needed, but useful for debugging.
                client,                     // The async client to the external service call, which needs to be closed.
                completableFuture,          // This CompletableFuture needs to be completed later on.
                this::callbackFunction);    // Gives us an option to do more work after the external service call.
        client.execute(
                SimpleRequestProducer.create(request),
                SimpleResponseConsumer.create(),
                wrapper);

        return completableFuture;
    }

    /**
     * Allows us to do more work after the external service call.
     */
    public String callbackFunction(SimpleHttpResponse httpResponse) {
        String tenantName = TenantContext.tenantName.get();

        System.out.println("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName
                + ": Got external service reply from " + SERVICE_URL_15);
        String textResponse = httpResponse.getBody().getBodyText();

        return tenantName + "-" + textResponse;
    }
}
