package com.terrydu.asyncservice.common;

import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class FutureCallbackWrapper implements FutureCallback<SimpleHttpResponse> {

    HttpServletRequest servletRequest;
    CloseableHttpAsyncClient client;
    String tenantName;
    CompletableFuture<String> futureResponse;
    Function<SimpleHttpResponse, String>  callbackFunction;
    long startTime;

    public FutureCallbackWrapper(HttpServletRequest servletRequest,
                                 CloseableHttpAsyncClient client,
                                 CompletableFuture<String> futureResponse,
                                 Function<SimpleHttpResponse, String> fn) {
        this.servletRequest = servletRequest;
        this.client = client;
        this.futureResponse = futureResponse;
        this.callbackFunction = fn;
        startTime = System.currentTimeMillis();

        // Read the context from Thread Local Storage, and remember it for later on.
        this.tenantName = TenantContext.tenantName.get();
    }

    @Override
    public void completed(final SimpleHttpResponse httpResponse) {
        // Set the Thread Local Storage, which is the primary purpose of this class.
        TenantContext.tenantName.set(tenantName);

        // Validate it for this exercise. This is specific to just this exercise, and would not be in production code.
        String[] parts = this.servletRequest.getRequestURI().split("/");
        if (parts.length > 1) {
            String tenantPart = parts[parts.length - 1];
            if (tenantPart != null && !tenantPart.equals(TenantContext.tenantName.get())) {
                System.err.println("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName
                        + ": ERROR - The value in thread local storage (" + TenantContext.tenantName.get()
                        + ") does not match the correct value (" + tenantPart + ")");
            }
        }

        //
        // Call a function to do some more work.
        //
        String textResponse = callbackFunction.apply(httpResponse);

        // When we're done return back to the REST API caller that called CEM.
        long endTime = System.currentTimeMillis();
        long timeElapsed = endTime - startTime;
        servletRequest.getPathInfo();
        servletRequest.getContextPath();
        System.out.println("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName
                + ": Completing request for '" + servletRequest.getRequestURI()
                + "' taking " + timeElapsed + " ms");

        try { client.close(); } catch (IOException ex) { System.err.println("ERROR closing CloseableHttpAsyncClient"); }
        futureResponse.complete(textResponse);
    }

    @Override
    public void failed(final Exception ex) {
        System.err.println("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName
                + ": ERROR - failed case -> " + ex);
        try { client.close(); } catch (IOException ex2) { System.err.println("ERROR closing CloseableHttpAsyncClient"); }
    }

    @Override
    public void cancelled() {
        System.err.println("Thread " + Thread.currentThread().getName() + ", Tenant " + tenantName
                + ": ERROR - cancelled case.");
        try { client.close(); } catch (IOException ex2) { System.err.println("ERROR closing CloseableHttpAsyncClient"); }
    }

}