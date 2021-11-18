package com.terrydu.asyncservice.api.mvc.controller;

import com.terrydu.asyncservice.common.TenantContext;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Response;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.CompletableFuture;

import static org.asynchttpclient.Dsl.asyncHttpClient;

@RestController
public class TerryAsyncHttpClient {

    private static final String SERVICE_URL_5 = "https://terrydu-wait.azurewebsites.net/api/terrydu-wait5";
    private static final String SERVICE_URL_15 = "https://terrydu-wait.azurewebsites.net/api/terrydu-wait15";
    private static final String SERVICE_URL_60 = "https://terrydu-wait.azurewebsites.net/api/terrydu-wait60";
    private static final String SERVICE_URL_120 = "https://terrydu-wait.azurewebsites.net/api/terrydu-wait120";

    @GetMapping("/api/mvc/terryasynchttpclient/tenant/{tenantName}")
    @Async
    public CompletableFuture<String> syncTenant(@PathVariable String tenantName,
                                                HttpServletRequest servletRequest,
                                                HttpServletResponse servletResponse) {
        System.out.println("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName
                + ": Handling request for '/api/mvc/terryasynchttpclient/tenant/" + tenantName + "'");
        long startTime = System.currentTimeMillis();

        // Set Thread Local Storage
        TenantContext.tenantName.set(tenantName);

        // Call external API that takes a long time here.
        AsyncHttpClient asyncHttpClient = asyncHttpClient();
        CompletableFuture<String> whenResponse = asyncHttpClient
            .prepareGet(SERVICE_URL_15)
            .execute()
            .toCompletableFuture()
            .exceptionally(t -> {
                // This is a different thread, so set Thread Local Storage.
                TenantContext.tenantName.set(tenantName);

                String errorMsg = "Thread " + Thread.currentThread().getName() + ", Tenant " + TenantContext.tenantName.get()
                        + ": ERROR - failed case for '/api/mvc/terryasynchttpclient/tenant/" + TenantContext.tenantName.get()
                        + " -> " + t.getMessage();
                System.err.println(errorMsg);
                try { asyncHttpClient.close(); } catch (IOException ex) { System.err.println("ERROR closing asyncHttpClient"); }
                throw new RuntimeException(errorMsg, t);
            })
            .thenApply(response -> {
                // This is a different thread, so set Thread Local Storage.
                TenantContext.tenantName.set(tenantName);

                /*  Do something with the Response */
                System.out.println("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName
                        + ": thenApply() for /api/mvc/terryasynchttpclient/tenant/");
                String textResponse = response.getResponseBody();

                // Validate it for this exercise. This is specific to just this exercise, and would not be in production code.
                String[] parts = servletRequest.getRequestURI().split("/");
                if (parts.length > 1) {
                    String tenantPart = parts[parts.length - 1];
                    if (tenantPart != null && !tenantPart.equals(TenantContext.tenantName.get())) {
                        System.err.println("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName
                                + ": ERROR - The value in thread local storage (" + TenantContext.tenantName.get()
                                + ") does not match the correct value (" + tenantPart + ")");
                    }
                }

                long endTime = System.currentTimeMillis();
                long timeElapsed = endTime - startTime;
                System.out.println("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName
                        + ": Completing request for '/api/mvc/terryasynchttpclient/tenant/" + tenantName
                        + "' taking " + timeElapsed + " ms");

                try { asyncHttpClient.close(); } catch (IOException ex) { System.err.println("ERROR closing asyncHttpClient"); }
                return TenantContext.tenantName.get() + "-" + textResponse;
            });
        return whenResponse;
    }
}
